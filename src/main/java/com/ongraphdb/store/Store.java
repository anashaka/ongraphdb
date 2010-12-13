package com.ongraphdb.store;

public interface Store<E> {
	E readItem(long id);
	E writeItem(E element);
	E removeItem(E item);
}
