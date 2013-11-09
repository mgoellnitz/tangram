/**
 *
 * Copyright 2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.jpa;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.content.Content;
import org.tangram.mutable.MutableContent;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class JpaContent implements MutableContent {

    private static final Log log = LogFactory.getLog(JpaContent.class);

    @Id
    @GeneratedValue
    private String id;


    /**
     * returns the string representation of the objects persistent ID.
     *
     * Be aware not to call this before the object has been persisted!
     */
    @Override
    public String getId() {
        String kind = getClass().getSimpleName();
        if (kind.indexOf('$') >= 0) {
            kind = getClass().getSuperclass().getSimpleName();
        } // if
        return kind+":"+id;
        // return id;
    } // getId()


    @Override
    public int hashCode() {
        return id==null ? 0 : id.hashCode();
    } // hashCode()


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JpaContent) ? getId().equals(((Content) obj).getId()) : super.equals(obj);
    } // equals()


    /**
     * One more convenience method to use IDs in persistence layer.
     * This is might still be a useful pattern in google app engine scenarios
     *
     * @param c Content instance - may be null
     * @return id of content or null
     */
    protected String getId(Content c) {
        return c==null ? null : c.getId();
    } // getId()


    /**
     * One more convenience method to use IDs in persistence layer.
     * This is might still be a useful pattern in google app engine scenarios
     *
     * @param contents list of contents - should not be null
     * @return list of ids for the given list of contents
     */
    protected List<String> getIds(List<? extends Content> contents) {
        List<String> result = new ArrayList<String>();
        if (contents!=null) {
            for (Object o : contents) {
                result.add(((Content) o).getId());
            } // for
        } // if
        return result;
    } // getIds()


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()


    @Override
    public String toString() {
        return getClass().getSimpleName()+"/"+getId();
    } // toString()

} // JpaContent
