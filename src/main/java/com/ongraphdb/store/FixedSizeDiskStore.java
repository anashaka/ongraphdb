package com.ongraphdb.store;

import java.io.IOException;

public class FixedSizeDiskStore implements DiskStore{

	public void start() throws IOException {
	}

	public void shutdown() throws IOException {
	}

	public byte[] readData(int pos) throws IOException {
		return null;
	}

	public int writeData(byte[] data, int blockSize) throws IOException {
		return 0;
	}

	public void updateData(int pos, byte[] data, int blockSize) throws IOException {
	}

	public void removeData(int pos) throws IOException {
	}

}
