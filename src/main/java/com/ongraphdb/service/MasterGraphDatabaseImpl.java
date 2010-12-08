package com.ongraphdb.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import com.ongraphdb.Edge;
import com.ongraphdb.Graph;
import com.ongraphdb.MasterGraphDatabase;
import com.ongraphdb.Vertex;

public class MasterGraphDatabaseImpl implements MasterGraphDatabase {
	private Map<String, String> propertyMap;
	private Map<String, Graph> graphsByUserId;
	
	public MasterGraphDatabaseImpl(String fileName){
		Properties props = new Properties();
		try {
			InputStream stream = new BufferedInputStream(new FileInputStream(new File(fileName)));
			try {
				props.load(stream);
			} finally {
				stream.close();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to load " + fileName, e);
		}
		Set<Entry<Object, Object>> entries = props.entrySet();
		Map<String, String> propertyMap = new HashMap<String, String>();
		for (Entry<Object, Object> entry : entries) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			propertyMap.put(key, value);
		}
		this.propertyMap = propertyMap;
	}
	
	public MasterGraphDatabaseImpl(Map<String, String> propertieMap){
		this.propertyMap = propertieMap;
	}
	
	public void addUserGraph(final String userId, final Graph graph) {
	}
	
	public Graph getUserGraph(String userId) {
		if(graphsByUserId.containsKey(userId)){
			return graphsByUserId.get(userId); 
		} else {
			
		}
		return null;
	}
	
	public Vertex addVertex(Object id) {
		return null;
	}

	public Vertex getVertex(Object id) {
		return null;
	}

	public void removeVertex(Vertex vertex) {
	}

	public Iterable<Vertex> getVertices() {
		return null;
	}

	public Edge getEdge(Object id) {
		return null;
	}

	public void removeEdge(Edge edge) {
	}

	public Iterable<Edge> getEdges() {
		return null;
	}

	public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex,
			String label) {
		return null;
	}
	
	public void clear() {
	}

	public void shutdown() {
	}
	
	

}
