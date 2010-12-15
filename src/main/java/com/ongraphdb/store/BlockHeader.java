package com.ongraphdb.store;

public class BlockHeader {
	private int dataSize;
	private byte fragmented;
	private int nextPos;

	public int getDataSize() {
		return dataSize;
	}

	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	public byte getFragmented() {
		return fragmented;
	}

	public void setFragmented(byte fragmented) {
		this.fragmented = fragmented;
	}
	
	public boolean isFragmented() {
		return this.fragmented == 1;
	}


	public int getNextPos() {
		return nextPos;
	}

	public void setNextPos(int nextPos) {
		this.nextPos = nextPos;
	}

}
