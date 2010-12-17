package com.ongraphdb.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class DiskStore {
	private final static int NEXT_BYTES = 4;
	private String dataFileName;
	private long dataMappedMemorySize;
	private FileChannel dataChannel;
	private FileLock dataLock;
	private MappedByteBuffer mappedDataBuffer;
	private int nextPosition;
	private long initialFileSize;
	private Thread shutdownHookThread;

	public DiskStore(String dataFileName, int dataMappedMemorySize, int initialFileSize) {
		this.dataFileName = dataFileName;
		this.dataMappedMemorySize = dataMappedMemorySize;
		this.initialFileSize = initialFileSize;
	}

	public void start() throws IOException {
		File file = new File(dataFileName);
		boolean newStore = file.exists() ? false : true;

		RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "rw");
		dataFile.setLength(initialFileSize);
		dataChannel = dataFile.getChannel();
		dataLock = dataChannel.lock();
		mappedDataBuffer = dataChannel.map(MapMode.READ_WRITE, 0, dataMappedMemorySize);

		if (newStore) {
			nextPosition = NEXT_BYTES;
			mappedDataBuffer.putInt(nextPosition);
		} else {
			nextPosition = mappedDataBuffer.getInt();
		}
		shutdownHookThread = new Thread() {
			public void run() {
				try {
					mappedDataBuffer.force();
					dataLock.release();
					dataChannel.close();
					unmap(mappedDataBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHookThread);
	}

	public void shutdown() throws IOException {
		mappedDataBuffer.force();
		mappedDataBuffer.clear();
		dataLock.release();
		dataChannel.close();
		unmap(mappedDataBuffer);
		Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void unmap(final Object buffer) {
		AccessController.doPrivileged(new PrivilegedAction() {
			@SuppressWarnings("restriction")
			public Object run() {
				Method getCleanerMethod;
				try {
					getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
					getCleanerMethod.setAccessible(true);
					sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
					cleaner.clean();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;

			}
		});
	}

	private RecordHeader readRecordHeaderFromMappedBuffer(int pos) {
		RecordHeader header = new RecordHeader();
		mappedDataBuffer.position(pos);
		header.setPosition(pos);
		header.setDataSize(mappedDataBuffer.getInt());
		header.setFragmented(mappedDataBuffer.get());
		if (header.isFragmented()) {
			header.setNextPos(mappedDataBuffer.getInt());
		}
		return header;
	}

	private RecordHeader readRecordHeaderFromChannel(int pos) throws IOException {
		RecordHeader header = new RecordHeader();
		header.setPosition(pos);
		ByteBuffer buffer = ByteBuffer.allocate(RecordHeader.HEADER_BYTES);
		dataChannel.position(pos);
		dataChannel.read(buffer);
		buffer.flip();
		header.setDataSize(buffer.getInt());
		header.setFragmented(buffer.get());
		if (header.isFragmented()) {
			header.setNextPos(buffer.getInt());
		}
		return header;
	}

	private byte[] readRecordDataFromChannel(RecordHeader header) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(header.getDataSize());
		dataChannel.position(header.getPosition() + header.getSize());
		dataChannel.read(buffer);
		return buffer.array();
	}

	private byte[] readRecordDataFromMappedBuffer(RecordHeader header) {
		byte[] data = new byte[header.getDataSize()];
		mappedDataBuffer.position(header.getPosition() + header.getSize());
		mappedDataBuffer.get(data);
		return data;
	}

	public byte[] readData(int pos) throws IOException {
		RecordHeader header = readRecordHeader(pos);
		byte[] data = readRecordData(header);

		if (header.isFragmented()) {
			byte[] result, tailData;
			tailData = readData(header.getNextPos());
			result = new byte[data.length + tailData.length];
			System.arraycopy(data, 0, result, 0, data.length);
			System.arraycopy(tailData, 0, result, data.length, tailData.length);
			return result;
		}
		return data;
	}

	private byte[] readRecordData(RecordHeader header) throws IOException {
		if (isMapped(header)) {
			return readRecordDataFromMappedBuffer(header);
		} else {
			return readRecordDataFromChannel(header);
		}
	}

	private boolean isMapped(int lastPos) {
		return lastPos < dataMappedMemorySize;
	}

	private boolean isMapped(RecordHeader header) {
		return dataMappedMemorySize > (header.getPosition() + header.getSize() + header.getDataSize());
	}

	private void writeRecordHeaderToMappedBuffer(RecordHeader header) {
		mappedDataBuffer.position(header.getPosition());
		mappedDataBuffer.putInt(header.getDataSize());
		mappedDataBuffer.put(header.getFragmented());
		if (header.isFragmented()) {
			mappedDataBuffer.putInt(header.getNextPos());
		}
	}

	private void writeRecordHeaderToChannel(RecordHeader header) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(header.getSize());
		buffer.putInt(header.getDataSize());
		buffer.put(header.getFragmented());
		if (header.isFragmented()) {
			buffer.putInt(header.getNextPos());
		}
		buffer.flip();
		dataChannel.position(header.getPosition());
		dataChannel.write(buffer);
	}

	private void writeRecordDataToMappedBuffer(RecordHeader header, byte[] data) {
		mappedDataBuffer.position(header.getPosition() + header.getSize());
		mappedDataBuffer.put(data);
	}

	private void writeRecordDataToChannel(RecordHeader header, byte[] data) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		dataChannel.position(header.getPosition() + header.getSize());
		dataChannel.write(buffer);
		dataChannel.force(false);
	}

	private void writeRecordData(RecordHeader header, byte[] data) throws IOException {
		if (isMapped(header)) {
			writeRecordDataToMappedBuffer(header, data);
		} else {
			writeRecordDataToChannel(header, data);
		}
	}

	private void writeRecordHeader(RecordHeader header) throws IOException {
		if (isMapped(header)) {
			writeRecordHeaderToMappedBuffer(header);
		} else {
			writeRecordHeaderToChannel(header);
		}
	}

	public int writeData(byte[] data, int blockSize) throws IOException {
		RecordHeader header = new RecordHeader().setDataSize(data.length).setFragmented(false);
		int recordSize = getRecordSize(blockSize, header);
		header.setPosition(getNextPosition(recordSize));
		try {
			writeRecordHeader(header);
			writeRecordData(header, data);
		} catch (IOException e) {
			// write to hole
			throw e;
		}
		return header.getPosition();
	}

	private int getNextPosition(int increment) {
		int result = nextPosition;
		nextPosition = nextPosition + increment;
		mappedDataBuffer.position(0);
		mappedDataBuffer.putInt(nextPosition);
		return result;
	}

	public void updateData(int pos, byte[] data, int blockSize) throws IOException {
		RecordHeader oldHeader = readRecordHeader(pos);
		RecordHeader newHeader = new RecordHeader().setPosition(pos).setDataSize(data.length).setFragmented(false);
		int oldRecordSize = getRecordSize(blockSize, oldHeader);
		int newRecordSize = getRecordSize(blockSize, newHeader);
		if (oldRecordSize >= newRecordSize) {
			if (oldHeader.isFragmented() || (oldRecordSize - newRecordSize > blockSize)) {
				// write tail to hole
				// write rest blocks to hole
			}
			writeRecordHeader(newHeader);
			writeRecordData(newHeader, data);
		} else {
			newHeader.setFragmented(true);
			newHeader.setDataSize(oldRecordSize - newHeader.getSize());
			byte[] headData = new byte[newHeader.getDataSize()];
			byte[] tailData = new byte[data.length - newHeader.getDataSize()];
			System.arraycopy(data, 0, headData, 0, newHeader.getDataSize());
			System.arraycopy(data, headData.length, tailData, 0, tailData.length);
			RecordHeader newTailHeader = new RecordHeader().setDataSize(tailData.length).setFragmented(false);
			int newRecordTailSize = getRecordSize(blockSize, newTailHeader);
			if (oldHeader.isFragmented()) {
				RecordHeader oldHeaderTail = readRecordHeader(oldHeader.getNextPos());
				int oldRecordTailSize = getRecordSize(blockSize, oldHeaderTail);
				if (oldRecordTailSize >= newRecordSize) {
					newTailHeader.setPosition(oldHeaderTail.getPosition());
					// write rest blocks to hole
				} else {
					// write tail to hole
					newTailHeader.setPosition(getNextPosition(newRecordTailSize));
				}
			} else {
				newTailHeader.setPosition(getNextPosition(newRecordTailSize));
			}
			newHeader.setNextPos(newTailHeader.getPosition());
			writeRecordHeader(newHeader);
			writeRecordData(newHeader, headData);
			writeRecordHeader(newTailHeader);
			writeRecordData(newTailHeader, tailData);
		}
	}

	private RecordHeader readRecordHeader(int pos) throws IOException {
		RecordHeader header;
		if (isMapped(pos + RecordHeader.HEADER_BYTES)) {
			header = readRecordHeaderFromMappedBuffer(pos);
		} else {
			header = readRecordHeaderFromChannel(pos);
		}
		return header;
	}

	protected int getRecordSize(int blockSize, RecordHeader header) {
		return ((header.getSize() + header.getDataSize() + blockSize - 1) / blockSize) * blockSize;
	}

}