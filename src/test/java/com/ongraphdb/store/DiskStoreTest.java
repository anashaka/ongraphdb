package com.ongraphdb.store;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DiskStoreTest {
	private DiskStore store;
	private static String DATA_FILE_NAME = "testData.dat";
	private static int BLOCK_SIZE = 10+RecordHeader.HEADER_BYTES;
	private static byte[] DATA_5 = "12345".getBytes();
	private static byte[] DATA_10 = "1234567890".getBytes();
	private static byte[] DATA_20 = "RECORD_20 RECORD_20 ".getBytes();
	private static byte[] DATA_100 = "RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100".getBytes();
	private static byte[] DATA_200 = "RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100RECORD_100".getBytes();
	private static int DATA_FILE_SIZE = 1*1024*1024;
	private static int DATA_MAPPED_BUFFER_SIZE = 100;

	@Before
	public void initStore() throws IOException{
		store = new DiskStore(DATA_FILE_NAME, DATA_MAPPED_BUFFER_SIZE, DATA_FILE_SIZE);
		store.start();
	}
	
	@After
	public void shoutdowndAndCleanStore() throws IOException, InterruptedException{
		store.shutdown();
		File f = new File(DATA_FILE_NAME);
		boolean success = f.delete();
	    assertTrue(success);
	}
	
	@Test
	public void createStore() throws IOException {		
		File f = new File(DATA_FILE_NAME);
		assertTrue(f.isFile());
		assertTrue(f.exists());
		assertEquals(f.length(), DATA_FILE_SIZE);
	}
	
	@Test
	public void writeReadOneByOne() throws IOException {
		int[] pos = new int[5];
		pos[0] = store.writeData(DATA_10, BLOCK_SIZE);
		assertEquals(4, pos[0]);		
		assertEquals(new String(DATA_10), new String(store.readData(pos[0])));
		pos[1] = store.writeData(DATA_20, BLOCK_SIZE);
		assertEquals(new String(DATA_20), new String(store.readData(pos[1])));		
		pos[2] = store.writeData(DATA_100, BLOCK_SIZE);
		assertEquals(new String(DATA_100), new String(store.readData(pos[2])));
		pos[3] = store.writeData(DATA_20, BLOCK_SIZE);
		pos[4] = store.writeData(DATA_100, BLOCK_SIZE);
		assertEquals(new String(DATA_100), new String(store.readData(pos[4])));
	}
	
	@Test
	public void reopenStoreAndReadByPosition() throws IOException {
		int[] pos = new int[6];
		pos[0] = store.writeData(DATA_10, BLOCK_SIZE);
		pos[1] = store.writeData(DATA_20, BLOCK_SIZE);
		pos[2] = store.writeData(DATA_100, BLOCK_SIZE);
		store.shutdown();
		store.start();
		pos[3] = store.writeData(DATA_10, BLOCK_SIZE);
		pos[4] = store.writeData(DATA_20, BLOCK_SIZE);
		pos[5] = store.writeData(DATA_100, BLOCK_SIZE);
		assertEquals(new String(DATA_10), new String(store.readData(pos[0])));
		assertEquals(new String(DATA_20), new String(store.readData(pos[1])));
		assertEquals(new String(DATA_100), new String(store.readData(pos[2])));
		assertEquals(new String(DATA_10), new String(store.readData(pos[3])));
		assertEquals(new String(DATA_20), new String(store.readData(pos[4])));
		assertEquals(new String(DATA_100), new String(store.readData(pos[5])));
	}
	
	@Test
	public void updateWithRecordFragmentation() throws IOException {		
		int[] pos = new int[6];
		pos[0] = store.writeData(DATA_10, BLOCK_SIZE);
		pos[1] = store.writeData(DATA_100, BLOCK_SIZE);
		store.updateData(pos[0], DATA_5, BLOCK_SIZE);
		pos[2] = store.writeData(DATA_10, BLOCK_SIZE);
		pos[3] = store.writeData(DATA_100, BLOCK_SIZE);
		assertEquals("Update without fragmentation", new String(DATA_5), new String(store.readData(pos[0])));
		assertEquals("Check that existed was not corrupted", new String(DATA_100), new String(store.readData(pos[1])));
		assertEquals("Check that existed was not corrupted", new String(DATA_10), new String(store.readData(pos[2])));
		store.updateData(pos[0], DATA_20, BLOCK_SIZE);
		assertEquals("Update single entry to new data with fragmentation", new String(DATA_20), new String(store.readData(pos[0])));
		assertEquals("Check that existed was not corrupted", new String(DATA_100), new String(store.readData(pos[1])));
		assertEquals("Check that existed was not corrupted", new String(DATA_10), new String(store.readData(pos[2])));
		store.updateData(pos[0], DATA_200, BLOCK_SIZE);
		assertEquals("Update fragmented entry to new data with fragmentation (in new place)", new String(DATA_200), new String(store.readData(pos[0])));
		store.updateData(pos[0], DATA_200, BLOCK_SIZE);
		assertEquals("Update fragmented entry to new data with fragmentation (in existed place)", new String(DATA_200), new String(store.readData(pos[0])));
		store.updateData(pos[2], DATA_200, BLOCK_SIZE);
		assertEquals("CheckFragmented read from channel", new String(DATA_200), new String(store.readData(pos[0])));
	}

} 