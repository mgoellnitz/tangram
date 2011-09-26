package org.tangram.rdbms.solution;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Article extends Linkable {

    private char[] text;


    public char[] getText() {
        return text;
    }


    public void setText(char[] text) {
        this.text = text;
    }

} // Article
