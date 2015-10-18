<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@page import="org.tangram.Constants,org.tangram.view.Utils"
%><%@page import="java.io.StringWriter,java.io.PrintWriter"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head><fmt:setBundle basename="org.tangram.Messages" var="msg"/>
<title>Tangram - <c:choose><c:when test="${! empty self.localizedMessage}">${self.localizedMessage}</c:when
  ><c:when test="${! empty self.message}">'${self.message}'</c:when
  ><c:otherwise>"<%=request.getAttribute(Constants.THIS).getClass().getName()%>"</c:otherwise></c:choose></title>
<%@include file="../../../include/head-elements.jsp" %>
</head>
<body style="background-color: white;">
<h1>${self}</h1>
<p><fmt:message key="text.generic.error" bundle="${msg}"/></p>
<div><img src="<%=Utils.getUriPrefix(request)%>/t/pieces.gif"/></div>
<hr/>
<pre>
<%
StringWriter stringWriter = new StringWriter();
PrintWriter writer = new PrintWriter(stringWriter);
Throwable e = (Throwable)request.getAttribute(Constants.THIS);
e.printStackTrace(writer);
%><%=stringWriter.toString()%>
</pre>
<% if (e.getCause() != null) { %>
<hr/>
<pre>
<%
stringWriter = new StringWriter();
writer = new PrintWriter(stringWriter);
e.getCause().printStackTrace(writer);
%><%=stringWriter.toString()%>
</pre>
<% } /* if */ %>
<%@include file="../../../include/tangram-footer.jsp" %>
</body>
</html>
