package org.tangram.gae.protection;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.tangram.content.Content;
import org.tangram.gae.GaeContent;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable
public abstract class Protection extends GaeContent implements org.tangram.solution.protection.Protection {

    @Persistent
    private Text description;

    private String protectionKey;

    private List<String> protectedContentIds;


    public Text getDescription() {
        return description;
    }


    public void setDescription(Text description) {
        this.description = description;
    }


    public String getProtectionKey() {
        return protectionKey;
    }


    public void setProtectionKey(String protectionKey) {
        this.protectionKey = protectionKey;
    }


    public List<Content> getProtectedContents() {
        return getContents(Content.class, protectedContentIds);
    }


    public void setProtectedContents(List<Content> protectedTopics) {
        protectedContentIds = getIds(protectedTopics);
    }


    @Override
    public List<? extends Content> getProtectionPath() {
        List<Content> result = new ArrayList<Content>();
        result.add(this);
        return result;
    } // getProtectionPath()

} // Protection
