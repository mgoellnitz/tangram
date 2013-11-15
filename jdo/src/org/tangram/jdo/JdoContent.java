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
package org.tangram.jdo;

import java.util.ArrayList;
import java.util.List;
import javax.jdo.JDOHelper;
import javax.jdo.annotations.NotPersistent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.content.Content;
import org.tangram.mutable.MutableContent;
import org.tangram.view.Utils;



public abstract class JdoContent implements MutableContent {

    private static final Log log = LogFactory.getLog(JdoContent.class);

    @NotPersistent
    private String id;


    /**
     * get readable and storable representation of ID.
     *
     * @param oid id as JDO internal object
     * @return id as readable and storable string
     */
    protected abstract String postprocessPlainId(Object oid);


    /**
     * returns the string representation of the objects persistent ID.
     *
     * Be aware not to call this before the object has been persisted!
     */
    @Override
    public String getId() {
        if (id==null) {
            id = postprocessPlainId(JDOHelper.getObjectId(this));
        } // if
        return id;
    } // getId()


    @Override
    public int hashCode() {
        return id==null ? 0 : id.hashCode();
    } // hashCode()


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JdoContent) ? getId().equals(((Content) obj).getId()) : super.equals(obj);
    } // equals()


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
        return Utils.getBeanFactory().getBean(cls, id);
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
                result.add(Utils.getBeanFactory().getBean(cls, id));
            } // for
        } // if
        return result;
    } // getContents()


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()


    @Override
    public String toString() {
        return getClass().getSimpleName()+"/"+getId();
    } // toString()

} // JdoContent
