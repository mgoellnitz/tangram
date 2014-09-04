/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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

import javax.jdo.JDOHelper;
import javax.jdo.annotations.NotPersistent;
import org.tangram.content.Content;



public abstract class JdoContent implements Content {

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


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()


    @Override
    public String toString() {
        return getClass().getSimpleName()+"/"+getId();
    } // toString()

} // JdoContent
