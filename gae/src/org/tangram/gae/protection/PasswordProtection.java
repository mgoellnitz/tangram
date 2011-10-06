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
