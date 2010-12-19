package com.ongraphdb.store;

import java.io.IOException;

public interface DiskStore {

	public abstract void start() throws IOException;

	public abstract void shutdown() throws IOException;

	public abstract byte[] readData(int pos) throws IOException;

	public abstract int writeData(byte[] data, int blockSize) throws IOException;

	public abstract void updateData(int pos, byte[] data, int blockSize) throws IOException;
	
	public abstract void removeData(int pos) throws IOException;

}