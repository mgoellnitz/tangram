package org.tangram.rdbms.solution;

import java.util.List;

import javax.jdo.annotations.PersistenceCapable;

import org.tangram.content.CodeResource;

@PersistenceCapable
public class RootTopic extends Topic {

    private List<String> bottomLinkIds;

    private List<String> cssIds;

    private List<String> jsIds;

    private String logoId;


    public List<Topic> getBottomLinks() {
        return getContents(Topic.class, bottomLinkIds);
    }


    public void setBottomLinks(List<Topic> bottomLinks) {
        bottomLinkIds = getIds(bottomLinks);
    }


    public List<CodeResource> getCss() {
        return getContents(CodeResource.class, cssIds);
    }


    public void setCss(List<CodeResource> css) {
        cssIds = getIds(css);
    }


    public List<CodeResource> getJs() {
        return getContents(CodeResource.class, jsIds);
    }


    public void setJs(List<CodeResource> js) {
        jsIds = getIds(js);
    }


    public ImageData getLogo() {
        return getContent(ImageData.class, logoId);
    }


    public void setLogo(ImageData logo) {
        this.logoId = logo.getId();
    }


    @Override
    public RootTopic getRootTopic() {
        return this;
    } // getRootTopic()

} // RootTopic
