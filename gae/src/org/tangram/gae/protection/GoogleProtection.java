/**
 *
 * Copyright 2011 Martin Goellnitz
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
package org.tangram.gae.protection;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

@PersistenceCapable
public class GoogleProtection extends AbstractProtection {

    @NotPersistent
    private final UserService userService = UserServiceFactory.getUserService();

    private String allowedUsers;


    public String getAllowedUsers() {
        return allowedUsers;
    }


    public void setAllowedUsers(String allowedUsers) {
        this.allowedUsers = allowedUsers;
    }


    @Override
    public String handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    } // handleLogin()


    /**
     * check auth domain if on live syetem
     */
    private boolean isValidDomain(User user) {
        return user.getAuthDomain().equals("gmail.com");
    } // isValidDomain()


    private boolean isValidUser(User user) {
        return StringUtils.isNotBlank(allowedUsers) ? allowedUsers.indexOf(user.getNickname())>=0 : true;
    } // isValidUser()


    @Override
    public boolean isContentVisible(HttpServletRequest request) throws Exception {
        User user = UserServiceFactory.getUserService().getCurrentUser();
        return user==null ? false : (isValidDomain(user)&&isValidUser(user));
    } // isTopicVisible()


    @Override
    public boolean needsAuthorization(HttpServletRequest request) {
        User user = UserServiceFactory.getUserService().getCurrentUser();
        return user==null||( !isValidDomain(user));
    } // needsAuthorization()


    public String getLoginUrl(HttpServletRequest request) {
        return userService.createLoginURL(""+request.getAttribute("tangramURL"));
    } // getLoginUrl();

} // PasswordProtection
