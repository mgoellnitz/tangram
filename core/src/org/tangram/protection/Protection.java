/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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
package org.tangram.protection;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tangram.content.Content;

public interface Protection extends ProtectedContent {

    /**
     * A site wide unique key for the protection.
     *
     * @return protection key
     */
    String getProtectionKey();


    /**
     * Protections point to the contents they protect.
     * Not the other way around.
     *
     * @return list of content items protected by thi instance.
     */
    List<? extends Content> getProtectedContents();


    /**
     * Handle a login request for the given protection
     * @param request http request to handle login in
     * @param response http response to be used while handling login
     * @return String describing the login state of this protection to be stored in the current session
     * @throws Exception mostlyIO exceptions
     */
    String handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception;


    /**
     * Tell if content requested is visible.
     * @param request http request to check
     * @return true if the content requested with the given http request is visible in this context
     * @throws Exception mostly IO related
     */
    boolean isContentVisible(HttpServletRequest request) throws Exception;


    /**
     * Tell if request needs authorization before being handled.
     * @param request http request to check
     * @return true if an authorization is needed before this request can show some content
     */
    boolean needsAuthorization(HttpServletRequest request);

} // Protection
