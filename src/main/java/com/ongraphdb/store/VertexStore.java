package com.ongraphdb.store;

import com.ongraphdb.DBConfig;
import com.ongraphdb.StoreConfig;
import com.ongraphdb.Vertex;

public class VertexStore extends DiskStore implements Store<Vertex> {
	
	public VertexStore(StoreConfig config) {
		super(config.getHolesFileName(), config.getHolesFileName(), config.getDataMappedMemorySize());
		
	}

	public Vertex readItem(long position) {
		/*getFileName()
		getData()
		deserialize()
		*/
		return null;
	}

	public Vertex writeItem(Vertex vertex) {
		/*if(hasId){
			getPos
		} else {
			getFreeId()
		}	
		serialize()
		if(getBlockSizeByVertexType()){
			
		}
		writeData()*/
		return null;
	}

	public Vertex removeItem(Vertex vertex) {
		//removeFromCache
		return null;
	}
	
	byte[] getData(long pos){
		return null;
	}
	
	

}
