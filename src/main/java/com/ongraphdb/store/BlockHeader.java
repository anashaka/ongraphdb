package com.ongraphdb.store;

public class BlockHeader {
	final static int SIZE_BYTES = 4;
	private static final int TYPE_BYTE = 1;
	static final int FRAGMENTED_BYTE = 1;
	static final int POINTER_BYTES = 4;
	private static final int HEADER_BYTES = SIZE_BYTES + /*TYPE_BYTE +*/ FRAGMENTED_BYTE + POINTER_BYTES;
	
	private int dataSize;
	private byte fragmented;
	private int nextPos;

	public BlockHeader() {
	}

	public BlockHeader(int dataSize, byte fragmented, int nextPos) {
		super();
		this.dataSize = dataSize;
		this.fragmented = fragmented;
		this.nextPos = nextPos;
	}

	public int getDataSize() {
		return dataSize;
	}

	public BlockHeader setDataSize(int dataSize) {
		this.dataSize = dataSize;
		return this;
	}

	public byte getFragmented() {
		return fragmented;
	}

	public BlockHeader setFragmented(byte fragmented) {
		this.fragmented = fragmented;
		return this;
	}

	public boolean isFragmented() {
		return this.fragmented == 1;
	}

	public int getNextPos() {
		return nextPos;
	}

	public BlockHeader setNextPos(int nextPos) {
		this.nextPos = nextPos;
		return this;
	}

	public BlockHeader setFragmented(boolean fragmented) {
		if (fragmented)
			this.fragmented = (byte) 1;
		else
			this.fragmented = (byte) 0;
		return this;
	}

	public int getSize() {
		return HEADER_BYTES +(isFragmented() ? 0 : -POINTER_BYTES);
	}

}
