/*
 *
 * Copyright 2017 martin
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
package org.tangram.content;


/**
 * Implementing instances will be notified about every single item change.
 */
public interface ChangeListener {

    /**
     * Called everytime before any single object in the repository gets deleted.
     *
     * @param content content item that will subsequently be deleted.
     */
    void delete(Content content);


    /**
     * Called everytime before any single object in the repository gets created or modified.
     *
     * @param content content item that will subsequently be updated (created or changed).
     */
    void update(Content content);

} // ChangeListener
