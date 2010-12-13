package com.ongraphdb.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public abstract class DiskStore {
	private String dataFile;
	private String holesFile;
	private long dataMappedMemorySize;
	
	public DiskStore(String dataFile, String holesFile, long memBufferSize) {
		this.dataFile = dataFile;
		this.holesFile = holesFile;
		dataMappedMemorySize =memBufferSize;
		
	}
	
	public void init() throws IOException{
		RandomAccessFile raf = new RandomAccessFile(dataFile, "rw" );
		FileChannel channel = raf.getChannel();
		channel.lock();
		channel.map(MapMode.READ_WRITE, 0, dataMappedMemorySize);
	}
	
	public byte[] readData(long pos, int blockSize){
		return null;
		
	}
	// maybe one method
	//public byte[] rewriteData(long pos, byte[] data, int blockSize)
	public byte[] writeData(long pos, byte[] data, int recordSize){
		
		return data;
		
	}
	
	public byte[] rewriteData(long pos, byte[] data, int blockSize){
		
		return data;
		
	}
	
}
