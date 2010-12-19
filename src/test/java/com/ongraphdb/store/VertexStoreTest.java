package com.ongraphdb.store;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.ongraphdb.Edge;
import com.ongraphdb.EdgeType;
import com.ongraphdb.StoreConfig;
import com.ongraphdb.TypeResolver;
import com.ongraphdb.Vertex;

public class VertexStoreTest {

	private VertexStore store;
	private static String DATA_FILE_NAME = "testData.dat";
	private static int DATA_FILE_SIZE = 1 * 1024 * 1024;
	private static int DATA_MAPPED_BUFFER_SIZE = 100;

	class VertexImpl implements Vertex {
		public String name;
		public String description;
		
		
		
		public VertexImpl() {
			
		}

		/*public VertexImpl(String name, String description) {
			super();
			this.name = name;
			this.description = description;
		}*/

		public Object getProperty(String key) {
			return "storeId";
		}

		public Set<String> getPropertyKeys() {
			return Collections.EMPTY_SET;
		}

		public void setProperty(String key, Object value) {
		}

		public Object removeProperty(String key) {
			return null;
		}

		public int getId() {
			return 0;
		}

		public void setId(int id) {
		}

		public boolean hasId() {
			return false;
		}

		public Iterable<Edge> getOutEdges() {
			return null;
		}

		public Iterable<Edge> getInEdges() {
			return null;
		}

		public Edge connectTo(Vertex endPoint, EdgeType edgeType) {
			return null;
		}

		public int getBlockSize() {
			return 10;
		}
		
		

	}

	@Before
	public void setUp() throws Exception {
		StoreConfig config = new StoreConfig();
		config.setDataFileName(DATA_FILE_NAME);
		config.setDataMappedMemorySize(DATA_MAPPED_BUFFER_SIZE);
		config.setInitialFileSize(DATA_FILE_SIZE);
		Kryo kryo = new Kryo();
		kryo.register(VertexImpl.class);
		TypeResolver.registerClass(VertexImpl.class, (byte) 0);
		store = new VertexStore(config, kryo);
		store.start();
	}

	@After
	public void tearDown() throws Exception {
		store.shutdown();
		File f = new File(DATA_FILE_NAME);
		boolean success = f.delete();
	    assertTrue(success);
	}

	@Test
	public void testReadWriteItem() throws IOException {
		Vertex vertex = new VertexImpl();//"Name", "Description");
		store.writeItem(vertex);
		VertexImpl vertex1 =  (VertexImpl) store.readItem(vertex.getId());
		assertEquals(vertex1.name, "Name");
	}

}
