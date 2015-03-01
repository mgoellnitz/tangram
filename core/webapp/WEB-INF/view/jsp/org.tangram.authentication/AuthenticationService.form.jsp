<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
	import="org.tangram.view.Utils"
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html><fmt:setBundle basename="org.tangram.Messages" var="msg"/>
<head>
<title><fmt:message key="title.login" bundle="${msg}"/></title>
<%@include file="../../../include/head-elements.jsp" %>
</head>
<body>
<h3><fmt:message key="title.login" bundle="${msg}"/></h3>
<%
request.setAttribute("tangramLoginError", request.getSession(true).getAttribute("tangram.login.error"));
%>
<p style="color: red;">&nbsp;${tangramLoginError}</p>
<form name="loginform" action="<cms:link bean="${self}" action="callback"/>" method="get">
<div class="cms_editor_row">
<span class="cms_editor_label">Username: </span><input type="text" name="username"/>
</div>
<div class="cms_editor_row">
<span class="cms_editor_label">Password: </span><input type="password" name="password"/>
</div>
<div class="cms_editor_row">
<span class="cms_editor_label">Remember me: </span><input type="checkbox" name="rememberMe"/>
</div>
<input type="submit" value="  <fmt:message key="label.login" bundle="${msg}"/>  "/>
<input type="hidden" name="client_name" value="form"/>
</form>
<%@include file="../../../include/tangram-footer.jsp" %>
</body>
</html>
