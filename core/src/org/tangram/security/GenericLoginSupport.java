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
package org.tangram.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.tangram.view.Utils;


@Named("loginSupport")
@Singleton
public class GenericLoginSupport implements LoginSupport {

    @Inject
    private ServletContext servletContext;

    private String staticLoginURL = "";

    private boolean liveSystem = false;


    public void setStaticLoginURL(@Named("staticLoginURL") String staticLoginURL) {
        this.staticLoginURL = Utils.getUriPrefix(servletContext)+staticLoginURL;
    }


    public void setLiveSystem(@Named("liveSystem") boolean liveSystem) {
        this.liveSystem = liveSystem;
    }



    @Override
    public boolean isLiveSystem() {
        return liveSystem;
    }


    @Override
    public void storeLogoutURL(HttpServletRequest request, String currentURL) {
    }


    @Override
    public String createLoginURL(String currentURL) {
        return staticLoginURL;
    }

} // GenericLoginSupport
