/**
 *
 * Copyright 2015 Martin Goellnitz
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
package org.tangram.nucleus.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import org.tangram.content.Content;
import org.tangram.protection.Protection;
import org.tangram.nucleus.NucleusContent;


@PersistenceCapable
public abstract class AbstractProtection extends NucleusContent implements Protection {

    private char[] description;

    private String protectionKey;

    @Join
    private List<NucleusContent> protectedContents;


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


    @Override
    public List<NucleusContent> getProtectedContents() {
        try {
            return protectedContents;
        } catch (Exception e) {
            return Collections.emptyList();
        } // try/catch
    } // getProtectedContents()


    public void setProtectedContents(List<NucleusContent> protectedContents) {
        this.protectedContents = protectedContents;
    }


    @Override
    public List<? extends Content> getProtectionPath() {
        List<Content> result = new ArrayList<>();
        result.add(this);
        return result;
    } // getProtectionPath()

} // AbstractProtection