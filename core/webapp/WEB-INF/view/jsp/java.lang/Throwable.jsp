<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.view.Utils"
%><%@page import="java.io.StringWriter"
%><%@page import="java.io.PrintWriter"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - ${self}</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/favicon.ico" />
</head>
<body>
<h1>
<c:choose><c:when test="${! empty self.localizedMessage}">${self.localizedMessage}</c:when>
<c:when test="${! empty self.message}">${self.message}</c:when>
<c:otherwise>${self.class.name}</c:otherwise></c:choose>
</h1>
<p>Tangram went to pieces for some internal reason.</p>
<div><img src="<%=Utils.getUriPrefix(request)%>/t/e/7TangramTeile.gif"/></div>
<hr/>  
<pre>
<%
StringWriter stringWriter = new StringWriter();
PrintWriter writer = new PrintWriter(stringWriter);
Exception e = (Exception)request.getAttribute(Constants.THIS);
e.printStackTrace(writer);
%><%=stringWriter.toString()%>
</pre>
</body>
</html>
