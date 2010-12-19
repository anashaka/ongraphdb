package com.ongraphdb;

public class TypeResolver {
	private static Class[] types = new Class[Byte.MAX_VALUE];

	public static Class getClassByID(byte id) {
		return types[id]; 
	}
	
	public static void registerClass(Class clazz, byte id){
		types[id] = clazz;
	}

}
