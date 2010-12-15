package com.ongraphdb;

public class StoreConfig {
	private String dataFileName;
	private int dataMappedMemorySize;
	private int initialFileSize;
	public String getDataFileName() {
		return dataFileName;
	}
	public void setDataFileName(String dataFileName) {
		this.dataFileName = dataFileName;
	}
	public int getDataMappedMemorySize() {
		return dataMappedMemorySize;
	}
	public void setDataMappedMemorySize(int dataMappedMemorySize) {
		this.dataMappedMemorySize = dataMappedMemorySize;
	}
	public int getInitialFileSize() {
		return initialFileSize;
	}
	public void setInitialFileSize(int initialFileSize) {
		this.initialFileSize = initialFileSize;
	}
	 
	

}
