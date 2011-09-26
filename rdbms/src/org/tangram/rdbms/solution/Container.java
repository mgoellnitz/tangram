package org.tangram.rdbms.solution;

import java.util.List;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Container extends Linkable {

    private List<Topic> contents;


    public List<Topic> getContents() {
        return contents;
    }


    public void setContents(List<Topic> contents) {
        this.contents = contents;
    }

} // Container
