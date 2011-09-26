package org.tangram.monitor;

public interface Statistics {
    
    public void increase(String eventIdentifier);

    public void avg(String eventIdentifier, long value);

} // Statistics
