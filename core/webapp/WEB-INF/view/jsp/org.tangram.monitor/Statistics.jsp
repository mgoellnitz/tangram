<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
	import="org.tangram.view.Utils"
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html><fmt:setBundle basename="org.tangram.Messages" var="msg"/>
<head>
<title>Tangram - <fmt:message key="title.statistics" bundle="${msg}"/></title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/favicon.ico" />
</head>
<body>
<div class="cms_editor_row">
<span class="cms_editor_label"><fmt:message key="label.starttime" bundle="${msg}"/>: <fmt:formatDate pattern="HH:mm:ss dd.MM.yyyy" timeZone="CET" value="${self.startTime}"  /></span>
</div>
<c:forEach items="${self.counter}" var="item">
<div class="cms_editor_row">
<span class="cms_editor_label">${item.key}</span>: ${item.value}
</div></c:forEach>
<%@include file="../../../include/tangram-footer.jsp" %>
</body>
</html>
