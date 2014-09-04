/**
 *
 * Copyright 2013-2014 Martin Goellnitz
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
package org.tangram.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import org.tangram.content.Content;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class JpaContent implements Content {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;


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
    } // getId()


    @Override
    public int hashCode() {
        String myId = getId();
        return myId==null ? 0 : myId.hashCode();
    } // hashCode()


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JpaContent) ? getId().equals(((Content) obj).getId()) : super.equals(obj);
    } // equals()


    @Override
    public int compareTo(Content c) {
        return getId().compareTo(c.getId());
    } // compareTo()


    @Override
    public String toString() {
        return getClass().getSimpleName()+"/"+getId();
    } // toString()

} // JpaContent
