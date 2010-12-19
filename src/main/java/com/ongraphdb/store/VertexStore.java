package com.ongraphdb.store;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import com.ongraphdb.StoreConfig;
import com.ongraphdb.TypeResolver;
import com.ongraphdb.Vertex;

public class VertexStore implements Store<Vertex> {
	private final DiskStore diskStore;
	private final Kryo kryo;

	//

	public VertexStore(final StoreConfig config, final Kryo kryo) {
		diskStore = new LinkedDiskStore(config.getDataFileName(), config.getDataMappedMemorySize(),
				config.getInitialFileSize());
		this.kryo = kryo;
	}

	public Vertex readItem(int id) throws IOException {
		ObjectBuffer buffer = new ObjectBuffer(kryo);
		byte[] data = diskStore.readData(id);
		TypeResolver.getClassByID(data[0]);
		return buffer.readObjectData(data, TypeResolver.getClassByID(data[0]));
	}

	public Vertex writeItem(Vertex vertex) throws IOException {
		ObjectBuffer buffer = new ObjectBuffer(kryo);
		if (vertex.hasId()) {
			diskStore.updateData(vertex.getId(), buffer.writeObjectData(vertex), vertex.getBlockSize());
		} else {
			int id = diskStore.writeData(buffer.writeObjectData(vertex), vertex.getBlockSize());
			vertex.setId(id);
		}
		return vertex;
	}

	public void removeItem(Vertex vertex) throws IOException {
		diskStore.removeData(vertex.getId());

	}

	public void start() throws IOException {
		diskStore.start();
	}

	public void shutdown() throws IOException {
		diskStore.shutdown();
	}

}
