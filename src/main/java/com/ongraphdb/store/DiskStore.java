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
	private final static int SIZE_BYTES = 4;
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
			mappedDataBuffer.putInt(SIZE_BYTES);
			nextPosition = SIZE_BYTES + 1;
		} else {
			nextPosition = mappedDataBuffer.getInt();
		}
		shutdownHookThread = new Thread() {
			public void run() {
				try {
					dataLock.release();
					dataChannel.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};	
		Runtime.getRuntime().addShutdownHook(shutdownHookThread);
		

	}
	
	public void shoutdown() throws IOException {
		mappedDataBuffer.force();
		mappedDataBuffer.clear();
		dataLock.release();
		dataChannel.close();
		dataChannel = null;
		unmap(mappedDataBuffer);
		mappedDataBuffer = null;
		System.gc();
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

	public byte[] readData(int pos, int blockSize) throws IOException {
		byte[] data;
		if (pos + blockSize < dataMappedMemorySize) {
			mappedDataBuffer.position(pos);
			int dataSize = mappedDataBuffer.getInt();
			if(pos + SIZE_BYTES + dataSize < dataMappedMemorySize){
				data = new byte[dataSize];
				mappedDataBuffer.get(data);
			} else {	
				ByteBuffer buffer = ByteBuffer.allocate(dataSize);
				dataChannel.position(pos+SIZE_BYTES);
				dataChannel.read(buffer);
				data = buffer.array();
				
			}
		} else {
			ByteBuffer buffer = ByteBuffer.allocate(SIZE_BYTES);
			dataChannel.position(pos);
			dataChannel.read(buffer);
			buffer.flip();
			int dataSize = buffer.getInt();
			buffer = ByteBuffer.allocate(dataSize);
			dataChannel.read(buffer);
			data = buffer.array();
		}
		return data;
	}

	public int writeData(byte[] data, int blockSize) {
		// check that pos in mapped buffer
		int recordSize = getRecordSize(data.length, blockSize);
		int pos = nextPosition;
		nextPosition = nextPosition + recordSize;
		mappedDataBuffer.position(0);
		mappedDataBuffer.putInt(nextPosition);
		try {
			// check that pos in mapped buffer
			if (pos + recordSize < mappedDataBuffer.limit()) {
				mappedDataBuffer.position(pos);
				mappedDataBuffer.putInt(data.length);
				mappedDataBuffer.put(data);
			} else {
				ByteBuffer buffer = ByteBuffer.allocate(data.length
						+ SIZE_BYTES);
				buffer.putInt(data.length);
				buffer.put(data);
				buffer.flip();
				dataChannel.write(buffer, pos);
			}
		} catch (Exception e) {
			e.printStackTrace();
			/*if (!nextPosition.compareAndSet(pos + recordSize, pos)) {
				// writehole();
			}*/
		}
		return pos;
	}

	public byte[] rewriteData(long pos, byte[] data, int blockSize) {
		return data;
	}

	protected int getRecordSize(int dataSize, int blockSize) {
		return ((SIZE_BYTES + dataSize + blockSize - 1) / blockSize)
				* blockSize;
	}

	

}