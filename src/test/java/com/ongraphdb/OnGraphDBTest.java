package com.ongraphdb;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ongraphdb.service.MasterGraphDatabaseImpl;


/**
 * Unit test for simple App.
 */
public class OnGraphDBTest{
	
	@BeforeClass
	public static void initDB(){
		
	}
	
	@AfterClass
	public static void shoutdownDB(){
		
	}
	
	@Test
	public void fillProperites(){
		Map map = new HashMap();
		map.put("storeDir", "i://temp");
		MasterGraphDatabase db = new MasterGraphDatabaseImpl(map);
		db.getUserGraph("userId");
	}
    
}
