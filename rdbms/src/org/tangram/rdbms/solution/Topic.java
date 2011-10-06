/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.rdbms.solution;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.content.Content;
import org.tangram.solution.protection.ProtectedContent;

@PersistenceCapable
public class Topic extends Linkable implements ProtectedContent {

    private static final Log log = LogFactory.getLog(Topic.class);

    private List<String> subTopicIds;

    private List<String> contentIds;

    private String thumbnailId; /* ImageData */

    private char[] teaser;

    private List<String> relatedContainerIds;


    public List<Topic> getSubTopics() {
        return getContents(Topic.class, subTopicIds);
    }


    public void setSubTopics(List<Topic> subTopics) {
        subTopicIds = getIds(subTopics);
    }


    public List<Linkable> getContents() {
        return getContents(Linkable.class, contentIds);
    }


    public void setContents(List<Linkable> contents) {
        contentIds = getIds(contents);
    }


    public ImageData getThumbnail() {
        return getContent(ImageData.class, thumbnailId);
    }


    public void setThumbnail(ImageData thumbnail) {
        this.thumbnailId = thumbnail.getId();
    }


    public char[] getTeaser() {
        return teaser;
    }


    public void setTeaser(char[] teaser) {
        this.teaser = teaser;
    }


    public List<Container> getRelatedContainers() {
        return getContents(Container.class, relatedContainerIds);
    }


    public void setRelatedContainers(List<Container> relatedContainers) {
        relatedContainerIds = getIds(relatedContainers);
    }

    /************************************/

    @NotPersistent
    RootTopic rootTopic = null;


    public RootTopic getRootTopic() {
        if (rootTopic==null) {
            List<RootTopic> rootTopics = getBeanFactory().listBeans(RootTopic.class, null);
            if (rootTopics!=null) {
                if (rootTopics.size()>0) {
                    rootTopic = rootTopics.get(0);
                } // if
            } // if
        } // if
        return rootTopic;
    } // getRootTopic()


    public List<Topic> getPathRecursive(Topic t) {
        List<Topic> result = null;
        if (equals(t)) {
            result = new ArrayList<Topic>();
            result.add(t);
        } else {
            List<Topic> subTopics = t.getSubTopics();
            if (subTopics!=null) {
                for (Topic x : subTopics) {
                    if (x!=null) {
                        List<Topic> path = getPathRecursive(x);
                        if (path!=null) {
                            path.add(0, t);
                            return path;
                        } // if
                    } else {
                        log.error("getPathRecursive() "+t.getId()+" has null pointer subtopic");
                    } // if
                } // for
            } // if
        } // if

        return result;
    } // getPath()

    @NotPersistent
    private List<Topic> path;


    public List<Topic> getPath() {
        if (log.isDebugEnabled()) {
            log.debug(getClass().getName()+".getPath("+getId()+") "+getRootTopic().getClass().getName());
        } // if
        if (path==null) {
            path = getPathRecursive(getRootTopic());
            if (path==null) {
                path = new ArrayList<Topic>();
                // path.add(getRootTopic());
                path.add(this);
            } // if
        } // if
        return path;
    } // getPath()

    @NotPersistent
    private List<Container> inheritedRelatedContainers;


    public List<Container> getInheritedRelatedContainers() {
        List<Container> result = new ArrayList<Container>();
        if (inheritedRelatedContainers==null) {
            List<Topic> path = getPath();
            int i = path.size();
            while (( --i>=0)&&(result.size()==0)) {
                result = path.get(i).getRelatedContainers();
            } // while
            inheritedRelatedContainers = result;
        } else {
            result = inheritedRelatedContainers;
        } // if
        return result;
    } // getInheritedRelatedContainers


    /**** Protections ***/

    @Override
    public List<? extends Content> getProtectionPath() {
        return getPath();
    } // getProtectionPath()

} // Topic
