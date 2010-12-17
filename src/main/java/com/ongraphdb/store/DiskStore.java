package com.ongraphdb.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

public class DiskStore {
	private final static int NEXT_BYTES = 4;
	private static final int TYPE_BYTE = 1;
	private static final int FRAGMENTED_BYTE = 1;
	private static final int POINTER_BYTES = 4;
	private static final int HEADER_BYTES = 9;
	private String dataFileName;
	private long dataMappedMemorySize;
	private FileChannel dataChannel;
	private FileLock dataLock;
	private MappedByteBuffer mappedDataBuffer;
	private int nextPosition;
	private long initialFileSize;
	private Thread shutdownHookThread;

	public DiskStore(String dataFileName, int dataMappedMemorySize,
			int initialFileSize) {
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
		mappedDataBuffer = dataChannel.map(MapMode.READ_WRITE, 0,
				dataMappedMemorySize);

		if (newStore) {
			mappedDataBuffer.putInt(0);
			nextPosition = NEXT_BYTES + 1;
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
					getCleanerMethod = buffer.getClass().getMethod("cleaner",
							new Class[0]);
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

	
	private BlockHeader readMappedBlockHeader(int pos){
		BlockHeader header = new BlockHeader();
		mappedDataBuffer.position(pos);		
		header.setDataSize(mappedDataBuffer.getInt());
		header.setFragmented(mappedDataBuffer.get());
		if(header.isFragmented()){
			header.setNextPos(mappedDataBuffer.getInt());
		}
		return header;
	}
	
	private BlockHeader readBlockHeader(int pos) throws IOException {
		BlockHeader header = new BlockHeader();
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES);
		dataChannel.position(pos);
		dataChannel.read(buffer);
		buffer.flip();
		header.setDataSize(buffer.getInt());
		header.setFragmented(buffer.get());
		if(header.isFragmented()){
			header.setNextPos(buffer.getInt());
		}
		return header;
	}
	
	private void writeMappedBlockHeader(int pos, BlockHeader header){		
		mappedDataBuffer.position(pos);		
		mappedDataBuffer.putInt(header.getDataSize());
		mappedDataBuffer.put(header.getFragmented());
		if(header.isFragmented()){
			mappedDataBuffer.putInt(header.getNextPos());
		}
	}
	
	private void writeBlockHeader(int pos, BlockHeader header) throws IOException {		
		ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES);
		buffer.putInt(header.getDataSize());
		buffer.put(header.getFragmented());
		if(header.isFragmented()){
			buffer.putInt(header.getNextPos());
		}
		buffer.flip();
		dataChannel.position(pos);
		dataChannel.write(buffer);
	}
	
	private byte[] readBlock(int pos, BlockHeader header) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(header.getDataSize());
		dataChannel.position(pos + HEADER_BYTES + (header.isFragmented() ? 0 : -POINTER_BYTES));
		dataChannel.read(buffer);
		return buffer.array();
	}
	
	private byte[] readMappedBlock(int pos, BlockHeader header){
		byte[] data = new byte[header.getDataSize()];
		mappedDataBuffer.position(pos + HEADER_BYTES +(header.isFragmented() ? 0 : -POINTER_BYTES));
		mappedDataBuffer.get(data);
		return data;
	}

	public byte[] readData(int pos) throws IOException {
		byte[] data;
		BlockHeader header;
		if (isMapped(pos + HEADER_BYTES)) {
			header = readMappedBlockHeader(pos);
		} else {
			header = readBlockHeader(pos);
		}
		
		if (isMapped(pos + HEADER_BYTES + header.getDataSize())) {
			data = readMappedBlock(pos, header);
		} else {
			data = readBlock(pos, header);
		}	
		
		if(header.isFragmented()){
			byte[] result, tailData;
			tailData = readData(header.getNextPos());
			result = new byte[data.length + tailData.length];
			System.arraycopy(data, 0, result, 0, data.length);
			System.arraycopy(tailData, 0, result, data.length , tailData.length);
			return result;
		}
		return data;
	}

	private boolean isMapped(int lastPos) {
		return lastPos<dataMappedMemorySize;
	}
	
	private void writeMappedBlock(int pos, BlockHeader header, byte[] data){		
		mappedDataBuffer.position(pos + HEADER_BYTES +(header.isFragmented() ? 0 : -POINTER_BYTES));
		mappedDataBuffer.put(data);
	}
	
	private void writeBlock(int pos, BlockHeader header, byte[] data) throws IOException{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		dataChannel.position(pos + HEADER_BYTES + (header.isFragmented() ? 0 : -POINTER_BYTES));
		dataChannel.write(buffer);
	}
	
	public int writeData(byte[] data, int blockSize) throws IOException {
		int recordSize = getRecordSize(data.length, blockSize);
		int pos = nextPosition;
		moveNextPosition(recordSize);
		try {
			if (isMapped(pos + recordSize)) {
				writeMappedBlockHeader(pos, new BlockHeader().setDataSize(data.length).setFragmented(false));
				writeMappedBlock(pos, new BlockHeader().setDataSize(data.length).setFragmented(false), data);
			} else {
				writeBlockHeader(pos, new BlockHeader().setDataSize(data.length).setFragmented(false));
				writeBlock(pos, new BlockHeader().setDataSize(data.length).setFragmented(false), data);
			}
		} catch (IOException e) {
			// write to hole
			throw e;
			
		}
		return pos;
	}

	private void moveNextPosition(int increment) {
		nextPosition = nextPosition + increment;
		mappedDataBuffer.position(0);
		mappedDataBuffer.putInt(nextPosition);
	}

	public byte[] updateData(int pos, byte[] data, int blockSize) throws IOException {
		BlockHeader header;
		if (isMapped(pos + HEADER_BYTES)) {
			header = readMappedBlockHeader(pos);
		} else {
			header = readBlockHeader(pos);
		}
		if(getRecordSize(header.getDataSize(), blockSize)>= data.length + header.getSize() ){
			if(header.isFragmented()) {
				// write hole
			}
			writeBlockHeader(pos, header.setDataSize(data.length).setFragmented(false));
			writeBlock(pos, header,data);
		} 

		return data;
	}

	protected int getRecordSize(int dataSize, int blockSize) {
		return ((BlockHeader.SIZE_BYTES + /*TYPE_BYTE +*/ BlockHeader.FRAGMENTED_BYTE + dataSize + blockSize - 1) / blockSize)
				* blockSize;
	}

	

}