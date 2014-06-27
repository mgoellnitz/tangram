/**
 *
 * Copyright 2013 Martin Goellnitz
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
package org.tangram.components.gae;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.tangram.Constants;
import org.tangram.security.LoginSupport;


@Named("loginSupport")
@Singleton
public class GaeLoginSupport implements LoginSupport {

    private String liveSuffix = "live";

    private final UserService userService = UserServiceFactory.getUserService();


    public String getLiveSuffix() {
        return liveSuffix;
    }


    public void setLiveSuffix(String liveSuffix) {
        this.liveSuffix = liveSuffix;
    }


    @Override
    public boolean isLiveSystem() {
        String id = com.google.appengine.api.utils.SystemProperty.applicationId.get();
        return id.endsWith(getLiveSuffix());
    } // isLiveSystem()


    @Override
    public void storeLogoutURL(HttpServletRequest request, String currentURL) {
        if (!currentURL.startsWith("/WEB-INF")&&!currentURL.startsWith("_ah")) {
            request.setAttribute(Constants.ATTRIBUTE_LOGOUT_URL, userService.createLogoutURL(currentURL));
        } // if
    } // storeLogoutURL()


    @Override
    public String createLoginURL(String currentURL) {
        return userService.createLoginURL(currentURL);
    } // createLoginURL()

} // GaeLoginSupport
