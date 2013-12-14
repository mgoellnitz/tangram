<%@page isELIgnored="false" language="java" session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.Constants"
%><%@page import="com.google.appengine.api.users.User,com.google.appengine.api.users.UserServiceFactory"
%><c:set var="infoLineClass" value="tangram_infobar"/><fmt:setBundle basename="org.tangram.editor.Messages" var="msg"/>
<c:if test="${! empty tangramAdminUser && empty tangramLiveSystem}"
	><p class="tangram_edit_toolbar"><a <cms:link bean="${self}" action="edit" href="true" target="true" handlers="true"/> ><fmt:message key="label.editobject" bundle="${msg}"/></a>
 | <%=com.google.appengine.api.utils.SystemProperty.applicationId.get()%>
v<%=com.google.appengine.api.utils.SystemProperty.applicationVersion.get()%>
 @ <%=com.google.appengine.api.utils.SystemProperty.version.get()%> Tangram <%=Constants.VERSION %> 
</p><c:set var="infoLineClass" value="tangram_edit_toolbar"/>
</c:if><p class="${infoLineClass}"><%if (request.getAttribute("start.time") != null) { %>
(<%=System.currentTimeMillis()-(Long)(request.getAttribute("start.time"))%>ms <fmt:message key="text.renderingtime" bundle="${msg}"/>)
<% } /* if */ %><% User user = UserServiceFactory.getUserService().getCurrentUser();
if (user != null) { %>
<%-- user.getUserId() --%> / <%=user.getEmail() %> / <%=user.getNickname() %><br/>
as <%=user.getFederatedIdentity() %> from <%=user.getAuthDomain() %>
<% } %></p>
