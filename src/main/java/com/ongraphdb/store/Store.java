package com.ongraphdb.store;

import java.io.IOException;

public interface Store<E> {
	public E readItem(int id) throws IOException;
	public E writeItem(E element) throws IOException;
	public void removeItem(E item) throws IOException;
	public void start() throws IOException;
	public void shutdown() throws IOException;
}
