package com.ongraphdb;

//TODO: Replace to Bluprints if will be the same
public interface Graph {
	
    public Vertex addVertex(Object id);

    public Vertex getVertex(Object id);

    public void removeVertex(Vertex vertex);

    public Iterable<Vertex> getVertices();

    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label);

    public Edge getEdge(Object id);

    public void removeEdge(Edge edge);

    public Iterable<Edge> getEdges();

    public void clear();

    public void shutdown();

}
