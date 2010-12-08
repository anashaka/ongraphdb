package com.ongraphdb;

import java.util.Set;

//TODO: Replace to Bluprints if will be the same
public abstract interface Element {

	public Object getProperty(String key);

    public Set<String> getPropertyKeys();

    public void setProperty(String key, Object value);
    
    public Object removeProperty(String key);
    
    public Object getId();

}
