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
package org.tangram.nucleus;

import java.util.ArrayList;
import java.util.List;
import org.tangram.content.Content;

/**
 * Abstract base class for the protected content core feature.
 *
 * As of now implementations should be presented within your application and no generic implementations
 * can be delivered by the framework.
 *
 */
public abstract class AbstractProtection extends NucleusContent implements org.tangram.protection.Protection {

    /**
     * for compatibility reasons internally use String and map it to char[]
     */
    private String description;

    private String protectionKey;

    private List<Content> protectedContents;


    public char[] getDescription() {
        return stringToCharArray(description);
    }


    public void setDescription(char[] description) {
        this.description = charArraytoString(description);
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
        List<Content> result = new ArrayList<>();
        result.add(this);
        return result;
    } // getProtectionPath()

} // AbstractProtection
