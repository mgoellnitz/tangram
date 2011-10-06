package org.tangram.rdbms.protection;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;

import org.tangram.content.Content;
import org.tangram.rdbms.RdbmsContent;

@PersistenceCapable
public abstract class Protection extends RdbmsContent implements org.tangram.solution.protection.Protection {

    private char[] description;

    private String protectionKey;

    private List<Content> protectedContents;


    public char[] getDescription() {
        return description;
    }


    public void setDescription(char[] description) {
        this.description = description;
    }


	@Override
    public String getProtectionKey() {
        return protectionKey;
    }


    public void setProtectionKey(String protectionKey) {
        this.protectionKey = protectionKey;
    }


    public List<Content> getProtectedContent() {
        return protectedContents;
    }


    public void setProtectedContent(List<Content> protectedContents) {
        this.protectedContents = protectedContents;
    }


    @Override
    public List<? extends Content> getProtectionPath() {
        List<Content> result = new ArrayList<Content>();
        result.add(this);
        return result;
    } // getProtectionPath()

} // Protection
