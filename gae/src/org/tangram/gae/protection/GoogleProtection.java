package org.tangram.gae.protection;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@PersistenceCapable
public class GoogleProtection extends Protection {

    @NotPersistent
    private UserService userService = UserServiceFactory.getUserService();

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
    private final boolean isValidDomain(User user, HttpServletRequest request) {
        return user.getAuthDomain().equals("gmail.com");
    } // isValidDomain()


    private final boolean isValidUser(User user) {
        return StringUtils.hasText(allowedUsers) ? allowedUsers.indexOf(user.getNickname())>=0 : true;
    } // isValidUser()


    @Override
	public boolean isContentVisible(HttpServletRequest request) throws Exception {
        User user = UserServiceFactory.getUserService().getCurrentUser();
        return user==null ? false : (isValidDomain(user, request)&&isValidUser(user));
    } // isTopicVisible()


    @Override
	public boolean needsAuthorization(HttpServletRequest request) {
        User user = UserServiceFactory.getUserService().getCurrentUser();
        return user==null||( !isValidDomain(user, request));
    } // needsAuthorization()


    public String getLoginUrl(HttpServletRequest request) {
        return userService.createLoginURL(""+request.getAttribute("tangramURL"));
    } // getLoginUrl();

} // PasswordProtection
