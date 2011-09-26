<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@page import="java.security.Principal"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.view.Utils"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram JDO Standalone System</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<%-- 
<meta HTTP-EQUIV="REFRESH" content="0; url=<%=request.getParameter("return")%>">
 --%>
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/e/favicon.ico" />
</head>
<body><%=request.getParameter("return")%>
<%
Principal principal = new Principal() {
    public String getName() {
        return "martin@goellnitz.de";
    } // getName()
};
request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, "true");
request.setAttribute(Constants.ATTRIBUTE_USER, principal);
%>
</body>
</html>
