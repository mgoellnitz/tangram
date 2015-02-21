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
package org.tangram.nucleus.protection;

import javax.jdo.annotations.PersistenceCapable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.tangram.feature.protection.SimplePasswordProtection;


@PersistenceCapable
public class PasswordProtection extends AbstractProtection implements SimplePasswordProtection {

    private String login;

    private String password;


    @Override
    public String getLogin() {
        return login;
    }


    public void setLogin(String login) {
        this.login = login;
    }


    @Override
    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String handleLogin(HttpServletRequest request, HttpServletResponse response) {
        String result = null;

        String passedOverLogin = request.getParameter(PARAM_LOGIN);
        String passedOverPwd = request.getParameter(PARAM_PASSWORD);

        if ((passedOverLogin!=null)&&(passedOverPwd!=null)) {
            if (passedOverLogin.equals(getLogin())&&passedOverPwd.equals(getPassword())) {
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
    } // isContentVisible()


    @Override
    public boolean needsAuthorization(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return session.getAttribute(getProtectionKey())==null;
    } // needsAuthorization()

} // PasswordProtection
