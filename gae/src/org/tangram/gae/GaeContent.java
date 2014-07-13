/**
 *
 * Copyright 2011-2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.gae;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.util.ArrayList;
import java.util.List;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanFactoryAware;
import org.tangram.content.Content;
import org.tangram.jdo.JdoContent;


@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE, customStrategy = "complete-table")
public abstract class GaeContent extends JdoContent implements BeanFactoryAware {

    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @PrimaryKey
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    private String id;

    @NotPersistent
    private BeanFactory gaeBeanFactory;


    @Override
    public void setBeanFactory(BeanFactory factory) {
        gaeBeanFactory = factory;
    } // setBeanFactory()


    @Override
    public String postprocessPlainId(Object oid) {
        String result = (oid==null) ? "" : ""+oid;
        try {
            Key key = KeyFactory.stringToKey(result);
            result = key.getKind()+":"+key.getId();
        } catch (Exception e) {
            // never mind
        } // try/catch
        return result;
    } // postprocessPlainId()


    /**
     * Return bean factory used by this instance.
     *
     * This is just a deprecated convenience method since we only have the bean factory at hand
     * for the other deprecated convenience methods.
     *
     * @return GAE bean factory instance
     */
    public BeanFactory getBeanFactory() {
        return gaeBeanFactory;
    } // getBeanFactory()


    /**
     * One more convenience method to use IDs in persistence layer.
     *
     * This is still a useful pattern in google app engine scenarios
     *
     * @param c Content instance - may be null
     * @return id of content or null
     */
    protected String getId(Content c) {
        return c==null ? null : c.getId();
    } // getId()


    /**
     * One more convenience method to use IDs in persistence layer.
     *
     * This is still a useful pattern in google app engine scenarios
     *
     * @param contents list of contents - should not be null
     * @return list of ids for the given list of contents
     */
    @Deprecated
    protected List<String> getIds(List<? extends Content> contents) {
        List<String> result = new ArrayList<String>();
        if (contents!=null) {
            for (Object o : contents) {
                result.add(((Content) o).getId());
            } // for
        } // if
        return result;
    } // getIds()


    /**
     * Legacy helper to store IDs as references.
     *
     * @param id
     * @return content for the given ID
     */
    @Deprecated
    protected <T extends JdoContent> T getContent(Class<T> cls, String id) {
        return gaeBeanFactory.getBean(cls, id);
    } // getContent()


    /**
     * Legacy helper to store IDs as references.
     *
     * @param ids list of ids to get contents for
     * @return list of contents for the given ids in the same order
     */
    @Deprecated
    protected <T extends JdoContent> List<T> getContents(Class<T> cls, List<String> ids) {
        List<T> result = null;
        if (ids!=null) {
            result = new ArrayList<T>(ids.size());
            for (String id : ids) {
                result.add(gaeBeanFactory.getBean(cls, id));
            } // for
        } // if
        return result;
    } // getContents()

} // GaeContent
