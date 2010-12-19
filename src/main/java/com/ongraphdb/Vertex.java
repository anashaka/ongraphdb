package com.ongraphdb;

//TODO: Replace to Bluprints if will be the same
public interface Vertex extends Element{

	public Iterable<Edge> getOutEdges();
    
	public Iterable<Edge> getInEdges();
	
	Edge connectTo(Vertex endPoint, EdgeType edgeType);

	public int getBlockSize();

}
