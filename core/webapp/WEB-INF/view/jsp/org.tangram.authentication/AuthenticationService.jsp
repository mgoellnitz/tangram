<%@page isELIgnored="false" language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
        import="org.tangram.view.Utils"%><%@taglib
	prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html><fmt:setBundle basename="org.tangram.Messages" var="msg"/>
<head>
<title><fmt:message key="title.login" bundle="${msg}"/></title>
<%@include file="../../../include/head-elements.jsp" %>
</head>
<body>
<h3><fmt:message key="title.login" bundle="${msg}"/></h3>
<div>
<cms:include bean="${self}" view="login"/>
</div>
<%@include file="../../../include/tangram-footer.jsp" %>
</body>
</html>
