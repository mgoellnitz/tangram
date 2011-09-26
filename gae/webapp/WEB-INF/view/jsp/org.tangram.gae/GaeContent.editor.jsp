<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.gae.TangramGAE"
%><%@page import="org.tangram.jdo.TangramJDO"
%><%@page import="com.google.appengine.api.users.User"
%><%@page import="com.google.appengine.api.users.UserServiceFactory"
%><c:set var="infoLineClass" value="tangram_infobar"/>
<c:if test="${! empty tangramAdminUser && empty tangramLiveSystem}"
	><p class="tangram_edit_toolbar"><a <cms:link bean="${self}" action="edit" href="true" target="true" handlers="true"/> >Objekt Bearbeiten</a>
 | <%=com.google.appengine.api.utils.SystemProperty.applicationId.get()%>
v<%=com.google.appengine.api.utils.SystemProperty.applicationVersion.get()%>
with <%=com.google.appengine.api.utils.SystemProperty.version.get()%> and tangram <%=Constants.getVersion() %> JDO <%=TangramJDO.getVersion() %> GAE <%=TangramGAE.getVersion() %> 
<%--<c:if test="${! empty logoutUrl}">| <a href="${logoutUrl}">Abmelden</a></c:if>--%>
</p><c:set var="infoLineClass" value="tangram_edit_toobar"/>
</c:if><p class="${infoLineClass}">
(<%=System.currentTimeMillis()-(Long)(request.getAttribute("start.time"))%>ms rendering time for this page)
<% User user = UserServiceFactory.getUserService().getCurrentUser();
if (user != null) { %>
<%-- user.getUserId() --%> / <%=user.getEmail() %> / <%=user.getNickname() %><br/>
as <%=user.getFederatedIdentity() %> from <%=user.getAuthDomain() %>
<% 
} 
%></p>
