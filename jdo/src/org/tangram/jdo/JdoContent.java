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
package org.tangram.jdo;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.NotPersistent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public abstract class JdoContent implements Content {

    private static final Log log = LogFactory.getLog(JdoContent.class);

    @NotPersistent
    protected PersistenceManager manager;

    @NotPersistent
    protected BeanFactory beanFactory;

    @NotPersistent
    private String id;


    /**
     * returns the string representation of the objects persistent ID.
     * Be aware not to call this before the object has been persisted!
     */
    @Override
    public String getId() {
        if (id==null) {
            id = ((JdoBeanFactory)beanFactory).postprocessPlainId(JDOHelper.getObjectId(this));
        } // if
        return id;
    } // getId()


    public void setManager(PersistenceManager manager) {
        this.manager = manager;
    }


    public BeanFactory getBeanFactory() {
        return beanFactory;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }


    @Override
    public int hashCode() {
        return id==null ? 0 : id.hashCode();
    } // hashCode()


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JdoContent) ? getId().equals(((JdoContent)obj).getId()) : super.equals(obj);
    } // equals()


    protected List<String> getIds(List<? extends Content> contents) {
        List<String> result = new ArrayList<String>();
        for (Object o : contents) {
            result.add(((JdoContent)o).getId());
        } // for
        return result;
    } // getIds()


    protected <T extends Content> T getContent(Class<T> c, String i) {
        if (log.isDebugEnabled()) {
            log.debug("getContent() id="+i+" beanFactory="+beanFactory);
        } // if
        return (StringUtils.hasText(id)) ? beanFactory.getBean(c, i) : null;
    } // getContent()


    protected <T extends Content> List<T> getContents(Class<T> c, List<String> ids) {
        List<T> result = new ArrayList<T>();
        if (ids!=null) {
            for (String i : ids) {
                T content = getContent(c, i);
                if (content!=null) {
                    result.add(content);
                } // if
            } // for
        } // if
        return result;
    } // getContents()


    @Override
    public boolean persist() {
        boolean result = true;
        try {
            manager.makePersistent(this);
            manager.currentTransaction().commit();
            ((JdoBeanFactory)beanFactory).clearCacheFor(this.getClass());
        } catch (Exception e) {
            log.error("persist()", e);
            // yes we saw situations where this was not the case thus hiding other errors!
            if (manager.currentTransaction().isActive()) {
                manager.currentTransaction().rollback();
            } // if
            result = false;
        } // try/catch/finally
        return result;
    } // persist()


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()

} // JdoContent
