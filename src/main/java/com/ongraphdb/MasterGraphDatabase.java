package com.ongraphdb;

public interface MasterGraphDatabase extends Graph {
	Graph getUserGraph(String userId);
	void addUserGraph(String userId, Graph userGraph);
}
