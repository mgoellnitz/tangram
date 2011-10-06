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
package org.tangram.solution.protection;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tangram.content.Content;

public interface Protection extends ProtectedContent {

    public String getProtectionKey();


    public List<Content> getProtectedContents();


    public String handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception;


    public boolean isContentVisible(HttpServletRequest request) throws Exception;


    public boolean needsAuthorization(HttpServletRequest request);

} // Protection
