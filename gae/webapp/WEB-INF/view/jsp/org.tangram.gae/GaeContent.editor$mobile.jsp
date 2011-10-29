<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.Constants"
%><%@page import="com.google.appengine.api.users.User"
%><%@page import="com.google.appengine.api.users.UserServiceFactory"
%><c:set var="infoLineClass" value="tangram_infobar"/>
<c:if test="${! empty tangramAdminUser && empty tangramLiveSystem}"
	><p class="tangram_edit_toolbar"><a <cms:link bean="${self}" action="edit" href="true" target="true" handlers="true"/> >Objekt Bearbeiten</a>
<br/><%=com.google.appengine.api.utils.SystemProperty.applicationId.get()%>
v<%=com.google.appengine.api.utils.SystemProperty.applicationVersion.get()%>
<br/>with <%=com.google.appengine.api.utils.SystemProperty.version.get()%> and<br/>tangram <%=Constants.VERSION %> 
</p><c:set var="infoLineClass" value="tangram_edit_toobar"/>
</c:if><p class="${infoLineClass}">
(<%=System.currentTimeMillis()-(Long)(request.getAttribute("start.time"))%>ms rendering time for this page)<br/>
<% User user = UserServiceFactory.getUserService().getCurrentUser();
if (user != null) { %>
<%=user.getEmail() %> / <%=user.getNickname() %>
<% 
} 
%></p>
