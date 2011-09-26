<%@page isELIgnored="false" language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"%><%@page
	import="java.security.Principal"%><%@page
	import="com.google.appengine.api.users.User"%><%@page
	import="com.google.appengine.api.users.UserService"%><%@page
	import="com.google.appengine.api.users.UserServiceFactory"%><div
	style="padding: 10px; color: red;">
<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
    if (user!=null) {
%> Sie sind als Benutzer <%=user.getNickname()%>
nicht berechtigt, diese Seite zu sehen. <%
    } else {
        String loginURL = userService.createLoginURL(""+request.getAttribute("tangramURL"));
        response.sendRedirect(loginURL); %>
        <script type="text/javascript">location.href='<%=loginURL%>';</script>
        Die <a href="<%=loginURL%>">Login-Seite</a> konnte leider nicht automatisch aufgerufen werden.
<%  } // if
%></div>
