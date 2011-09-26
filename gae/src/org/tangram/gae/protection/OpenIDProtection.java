package org.tangram.gae.protection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.tangram.Constants;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@PersistenceCapable
public class OpenIDProtection extends Protection {

    @NotPersistent
    private UserService userService = UserServiceFactory.getUserService();

    private static final Map<String, String> OPEN_ID_PROVIDERS;
    static {
        OPEN_ID_PROVIDERS = new HashMap<String, String>();
        OPEN_ID_PROVIDERS.put("Google", "google.com/accounts/o8/id");
        OPEN_ID_PROVIDERS.put("Yahoo", "yahoo.com");
        OPEN_ID_PROVIDERS.put("MySpace", "myspace.com");
        OPEN_ID_PROVIDERS.put("AOL", "aol.com");
        OPEN_ID_PROVIDERS.put("MyOpenID", "myopenid.com");
    }

    private String allowedUsers;


    public String getAllowedUsers() {
        return allowedUsers;
    }


    public void setAllowedUsers(String allowedUsers) {
        this.allowedUsers = allowedUsers;
    }


    public Map<String, String> getProviders() {
        return OPEN_ID_PROVIDERS;
    } // getProviders()


    public Collection<String> getProviderNames() {
        return OPEN_ID_PROVIDERS.keySet();
    } // getProviders()


    public String handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    } // handleLogin()


    private final boolean isValidUser(HttpServletRequest request, User user) {
        return (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)!=null)
                ||((user.getFederatedIdentity()!=null)&&(StringUtils.hasText(allowedUsers) ? allowedUsers.indexOf(user
                        .getNickname())>=0 : true));
    } // isValidUser()


    public boolean isContentVisible(HttpServletRequest request) throws Exception {
        User user = userService.getCurrentUser();
        return user!=null&&isValidUser(request, user);
    } // isTopicVisible()


    public boolean needsAuthorization(HttpServletRequest request) {
        User user = UserServiceFactory.getUserService().getCurrentUser();
        return user==null
                ||(user.getFederatedIdentity()==null&&(request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null));
    } // needsAuthorization()


    public String getLoginUrl(HttpServletRequest request, String providerName) {
        String providerUrl = getProviders().get(providerName);
        String tangramUrl = ""+request.getAttribute("tangramURL");
        String result = userService.createLoginURL(tangramUrl, null, providerUrl, new java.util.HashSet<String>());
        return result;
    } // getLoginUrl();

} // OpenIDProtection
