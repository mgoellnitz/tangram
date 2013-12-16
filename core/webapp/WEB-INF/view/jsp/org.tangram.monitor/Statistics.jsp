<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
	import="org.tangram.view.Utils"
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - Statistics</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/e/favicon.ico" />
</head>
<body>
<div class="cms_editor_row">
<span class="cms_editor_label">Starttime: <fmt:formatDate pattern="HH:mm:ss dd.MM.yyyy" timeZone="CET" value="${self.startTime}"  /></span>
</div>
<c:forEach items="${self.counter}" var="item">
<div class="cms_editor_row">
<span class="cms_editor_label">${item.key}</span>: ${item.value}
</div></c:forEach>
<hr/>
</body>
</html>
