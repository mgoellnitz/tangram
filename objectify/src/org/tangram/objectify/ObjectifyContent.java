/**
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.objectify;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import org.tangram.content.Content;




/**
 * Content base class for Objectify.
 */
@Entity
public abstract class ObjectifyContent implements Content {

    @Id
    private Long objectifyInternalId;


    /**
     * returns the string representation of the objects persistent ID.
     *
     * Be aware not to call this before the object has been persisted!
     */
    @Override
    public String getId() {
        return getClass().getSimpleName()+":"+objectifyInternalId;
    } // getId()


    @Override
    public int hashCode() {
        return objectifyInternalId==null ? 0 : objectifyInternalId.hashCode();
    } // hashCode()


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ObjectifyContent) ? objectifyInternalId.equals(((ObjectifyContent) obj).objectifyInternalId) : super.equals(obj);
    } // equals()


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()


    @Override
    public String toString() {
        return getId();
    } // toString()

} // ObjectifyContent
