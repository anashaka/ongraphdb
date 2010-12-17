package com.ongraphdb.store;

public class RecordHeader {
	private final static int SIZE_BYTES = 4;
	private static final int TYPE_BYTE = 1;
	private static final int FRAGMENTED_BYTE = 1;
	private static final int POINTER_BYTES = 4;
	static final int HEADER_BYTES = SIZE_BYTES + /*TYPE_BYTE +*/ FRAGMENTED_BYTE + POINTER_BYTES;
	
	private int dataSize;
	private byte fragmented;
	private int nextPosition;
	private int position;

	public RecordHeader() {
	}

	public RecordHeader(int postion, int dataSize, boolean fragmented, int nextPos) {
		this.position = postion;
		this.dataSize = dataSize;
		setFragmented(fragmented);
		this.nextPosition = nextPos;		
	}

	public int getDataSize() {
		return dataSize;
	}

	public RecordHeader setDataSize(int dataSize) {
		this.dataSize = dataSize;
		return this;
	}

	public byte getFragmented() {
		return fragmented;
	}

	public RecordHeader setFragmented(byte fragmented) {
		this.fragmented = fragmented;
		return this;
	}

	public boolean isFragmented() {
		return this.fragmented == 1;
	}

	public int getNextPos() {
		return nextPosition;
	}

	public RecordHeader setNextPos(int nextPos) {
		this.nextPosition = nextPos;
		return this;
	}

	public RecordHeader setFragmented(boolean fragmented) {
		if (fragmented)
			this.fragmented = (byte) 1;
		else
			this.fragmented = (byte) 0;
		return this;
	}

	public int getSize() {
		return HEADER_BYTES +(isFragmented() ? 0 : -POINTER_BYTES);
	}

	public int getPosition() {
		return position;
	}

	public RecordHeader setPosition(int position) {
		this.position = position;
		return this;
	}

	

}
