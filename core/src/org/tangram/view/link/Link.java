package org.tangram.view.link;

import java.util.HashMap;
import java.util.Map;

public class Link {

    private String url;

    private String target;

    private Map<String, String> handlers = new HashMap<String, String>();


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public String getTarget() {
        return target;
    }


    public void setTarget(String target) {
        this.target = target;
    }


    public void addHandler(String handler, String value) {
        handlers.put(handler, value);
    }


    public void removeHandler(String handler) {
        handlers.remove(handler);
    }


    public Map<String, String> getHandlers() {
        return handlers;
    }


    @Override
    public String toString() {
        return url+"@"+target+": "+handlers;
    } // toString()

} // Link
