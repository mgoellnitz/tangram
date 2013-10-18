/**
 * 
 * Copyright 2013 Martin Goellnitz
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
package org.tangram.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class JpaContent implements Content {

    private static final Log log = LogFactory.getLog(JpaContent.class);

    @Transient
    protected BeanFactory beanFactory;

    @Id
    @GeneratedValue
    private String id;


    /**
     * returns the string representation of the objects persistent ID. Be aware not to call this before the object has
     * been persisted!
     */
    @Override
    public String getId() {
        return id;
    } // getId()


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
        return (obj instanceof JpaContent) ? getId().equals(((JpaContent)obj).getId()) : super.equals(obj);
    } // equals()


    /**
     * One more convenience method to use IDs in persistence layer - which is still a useful pattern in google app
     * engine scenarios
     * 
     * @param c
     *            Content instance - may be null
     * @return id of content or null
     */
    protected String getId(Content c) {
        return c==null ? null : c.getId();
    } // getId()


    /**
     * One more convenience method to use IDs in persistence layer - which is still a useful pattern in google app
     * engine scenarios
     * 
     * @param contents
     *            list of contents - should not be null
     * @return list of ids for the given list of contents
     */
    protected List<String> getIds(List<? extends Content> contents) {
        List<String> result = new ArrayList<String>();
        if (contents!=null) {
            for (Object o : contents) {
                result.add(((JpaContent)o).getId());
            } // for
        } // if
        return result;
    } // getIds()


    /**
     * One more convenience method to use IDs in persistence layer - which is still a useful pattern in google app
     * engine scenarios
     * 
     * @param i
     *            id to fetch content for - may be null or empty
     * @return resulting content or null
     */
    protected <T extends Content> T getContent(Class<T> c, String i) {
        if (log.isDebugEnabled()) {
            log.debug("getContent() id="+i+" beanFactory="+beanFactory);
        } // if
        return (StringUtils.hasText(i)) ? beanFactory.getBean(c, i) : null;
    } // getContent()


    /**
     * One more convenience method to use IDs in persistence layer - which is still a useful pattern in google app
     * engine scenarios
     * 
     * @param ids
     *            list of id which should match the given type - may be null
     * @return Array of contents where none of the is null
     */
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
        EntityManager manager = null;
        try {
            // TODO: It might be that there are situations where this is still null
            manager = ((JpaBeanFactory)getBeanFactory()).getManager();
            manager.persist(this);
            manager.getTransaction().commit();
            ((JpaBeanFactory)beanFactory).clearCacheFor(this.getClass());
        } catch (Exception e) {
            log.error("persist()", e);
            if (manager!=null) {
                // yes we saw situations where this was not the case thus hiding other errors!
                if (manager.getTransaction().isActive()) {
                    manager.getTransaction().rollback();
                } // if
            } // if
            result = false;
        } // try/catch/finally
        return result;
    } // persist()


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()


    @Override
    public String toString() {
        return getClass().getSimpleName()+"/"+getId();
    } // toString()

} // JpaContent
