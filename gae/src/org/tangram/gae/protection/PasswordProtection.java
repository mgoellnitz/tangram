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
package org.tangram.gae.protection;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@PersistenceCapable
public class PasswordProtection extends Protection {

    @NotPersistent
    public static final String PARAM_LOGIN = "login";

    @NotPersistent
    public static final String PARAM_PASSWORD = "password";

    @NotPersistent
    private static final String ERROR_CODE = "Falscher Benutzername und/oder falsches Paßwort eingegeben!";

    private String login;

    private String password;


    public String getLogin() {
        return login;
    }


    public void setLogin(String login) {
        this.login = login;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    @Override
	public String handleLogin(HttpServletRequest request, HttpServletResponse response) {
        String result = null;

        String login = request.getParameter(PARAM_LOGIN);
        String password = request.getParameter(PARAM_PASSWORD);

        if ((login!=null)&&(password!=null)) {
            if (login.equals(getLogin())&&password.equals(getPassword())) {
                HttpSession session = request.getSession();
                session.setAttribute(getProtectionKey(), getLogin());
            } else {
                result = ERROR_CODE;
            } // if
        } else {
            result = ERROR_CODE;
        } // if

        return result;
    } // handleLogin()


    @Override
	public boolean isContentVisible(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        String sessionValue = ""+session.getAttribute(getProtectionKey());
        return sessionValue.equals(getLogin());
    } // isTopicVisible()


    @Override
	public boolean needsAuthorization(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return session.getAttribute(getProtectionKey())==null;
    } // needsAuthorization()

} // PasswordProtection
