package com.ongraphdb;

public class GraphConfig {
	public final static String STORE_DIR = "store.dir";
	public final static String EDGES_FILENAME = "edges.filename";
	public final static String VERTEXES_FILENAME = "vertexes.filename";
	
	private String storeDir;
	private String edgesFileName;
	private String vertexesFileName;
	private StoreConfig vertexConfig;
	private StoreConfig edgeConfig;
	
	
	public static GraphConfig getDefaultConfig(){
		GraphConfig config = new GraphConfig();
		config.setStoreDir("c://temp//ongraphdb");
		config.setEdgesFileName("edges.ogf");
		config.setVertexesFileName("vertex.ogf");
		config.setVertexesFileName("vertex.ogf");
		
		return config;
	}

	public String getStoreDir() {
		return storeDir;
	}

	public String getEdgesFileName() {
		return edgesFileName;
	}

	public String getVertexesFileName() {
		return vertexesFileName;
	}

	public void setStoreDir(String storeDir) {
		this.storeDir = storeDir;
	}

	public void setEdgesFileName(String edgesFileName) {
		this.edgesFileName = edgesFileName;
	}

	public void setVertexesFileName(String vertexesFileName) {
		this.vertexesFileName = vertexesFileName;
	}
	
	
	

}
