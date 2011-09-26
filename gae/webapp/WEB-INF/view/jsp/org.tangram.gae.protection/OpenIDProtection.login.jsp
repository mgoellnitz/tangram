<%@page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags" 
%><%@page import="com.google.appengine.api.users.UserService"
%><%@page import="com.google.appengine.api.users.UserServiceFactory"
%><% UserService userService = UserServiceFactory.getUserService(); 
%><div>
<div style="padding: 10px;">
<p>Um diesen Bereich der Web-Site zu sehen, benötigen Sie eine Identifizierung.</p>
<p>Wir wollen Ihnen jedoch keinen weiteren Login, den Sie sich merken müssen, zumuten. 
Nutzen Sie einfach einen der Dienste unten, denen Sie vertrauen.</p>
<p>&#160;</p>
<p>Login-Möglichkeiten:</p>
<p>Bitte authentifizieren Sie sich mit den Benutzerdaten ihrer Wahl</p>
</div>
<c:forEach items="${self.providerNames}" var="providerName">
<c:set var="providerUrl" value="${self.providers[providerName]}"/>
<%
String providerName = ""+pageContext.getAttribute("poviderName");
String providerUrl = ""+pageContext.getAttribute("providerUrl");
String providerLoginUrl = "";
providerLoginUrl = userService.createLoginURL(""+request.getAttribute("tangramURL"), null, providerUrl, new java.util.HashSet<String>());
pageContext.setAttribute("providerLoginUrl", providerLoginUrl);
%>
<div style="padding: 10px; text-align: right; font-weight: bold;"><a href="${providerLoginUrl}">${providerName}</a></div>
</c:forEach>
</div>