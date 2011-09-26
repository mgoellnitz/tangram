package org.tangram.rdbms.solution;

import javax.jdo.annotations.PersistenceCapable;

import org.tangram.content.Content;
import org.tangram.rdbms.RdbmsContent;

@PersistenceCapable
public abstract class Linkable extends RdbmsContent {

    private String title;

    private String shortTitle;

    private String keywords;


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getShortTitle() {
        return shortTitle;
    }


    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }


    public String getKeywords() {
        return keywords;
    }


    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }


    @Override
    public int compareTo(Content o) {
        return (o instanceof Linkable) ? ((getTitle()==null||((Linkable)o).getTitle()==null) ? 0 : getTitle()
                .compareTo(((Linkable)o).getTitle())) : super.compareTo(o);
    } // compareTo()
} // Linkable
