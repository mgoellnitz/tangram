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
package org.tangram.jpa.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import org.tangram.content.Content;
import org.tangram.feature.protection.Protection;
import org.tangram.jpa.JpaContent;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractProtection extends JpaContent implements Protection {

    private char[] description;

    private String protectionKey;

    @OneToMany
    private List<JpaContent> protectedContents;


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
    public List<JpaContent> getProtectedContents() {
        try {
            return protectedContents;
        } catch (Exception e) {
            return Collections.emptyList();
        } // try/catch
    } // getProtectedContents()


    public void setProtectedContents(List<JpaContent> protectedContents) {
        this.protectedContents = protectedContents;
    }


    @Override
    public List<? extends Content> getProtectionPath() {
        List<Content> result = new ArrayList<>();
        result.add(this);
        return result;
    } // getProtectionPath()

} // AbstractProtection
