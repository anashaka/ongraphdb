package com.ongraphdb;

//TODO: Replace to Bluprints if will be the same
public interface Edge extends Element{
	
	public Vertex getOutVertex();

	public Vertex getInVertex();
	
	public String getName();
}
