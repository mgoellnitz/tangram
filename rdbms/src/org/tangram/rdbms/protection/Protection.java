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
package org.tangram.rdbms.protection;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;

import org.tangram.content.Content;
import org.tangram.rdbms.RdbmsContent;

@PersistenceCapable
public abstract class Protection extends RdbmsContent implements org.tangram.feature.protection.Protection {

    private char[] description;

    private String protectionKey;

    private List<Content> protectedContents;


    public char[] getDescription() {
        return description;
    }


    public void setDescription(char[] description) {
        this.description = description;
    }


    @Override
    public String getProtectionKey() {
        return protectionKey;
    }


    public void setProtectionKey(String protectionKey) {
        this.protectionKey = protectionKey;
    }


    public List<Content> getProtectedContent() {
        return protectedContents;
    }


    public void setProtectedContent(List<Content> protectedContents) {
        this.protectedContents = protectedContents;
    }


    @Override
    public List<? extends Content> getProtectionPath() {
        List<Content> result = new ArrayList<Content>();
        result.add(this);
        return result;
    } // getProtectionPath()

} // Protection
