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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.feature.protection;

import java.util.List;
import org.tangram.content.Content;

/**
 * Implementing classes are considered content protected by some implementation of the protection interface.
 */
public interface ProtectedContent extends Content {

    /**
     * Present a content hierarchy under which the protection of this content is calculated.
     * 
     * If any of the contents in the list is protected by some protection this content is considered protected
     * by this protection instance.
     */
    List<? extends Content> getProtectionPath();

} // ProtectedContent()
