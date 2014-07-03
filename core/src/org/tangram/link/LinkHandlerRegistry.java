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
package org.tangram.link;


/**
 * Implementing classes hold a registry of link schemes.
 *
 * The implementation is intended for "static" link schemes which are not part of the class repository.
 */
public interface LinkHandlerRegistry {

    /**
     * Handlers to be registered need not implement the LinkHandler interface but might be annotated with
     * @LinkHandler.
     *
     * In the later case some @LinkAction at some methods must be present for the handler to
     * be in effect.
     *
     * @param handler instance implementing the link handler interfaces or with the link handler annotation
     */
    void registerLinkHandler(Object handler);

} // LinkHandlerRegistry
