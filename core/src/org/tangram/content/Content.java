package org.tangram.content;

public interface Content extends Comparable<Content> {

    String getId();
    
    
    void setBeanFactory(BeanFactory factory);


    boolean persist();


    int compareTo(Content o);

} // Content
