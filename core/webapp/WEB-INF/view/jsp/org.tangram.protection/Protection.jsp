<%@page isELIgnored="false" language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.view.Utils"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - <cms:include bean="${self}" view="description"/>: <%=request.getAttribute("self").getClass().getSimpleName()%></title>
<%@include file="../../../include/head-elements.jsp" %>
</head>
<body>
<div>
<cms:include bean="${self}" view="login"/>
</div>
<%@include file="../../../include/tangram-footer.jsp" %>
</body>
</html>
