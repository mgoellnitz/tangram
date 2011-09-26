<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.content.CodeResource"
%><%@page import="org.tangram.Constants"
%><c:choose><c:when test="${self.size < 1024}"
><style type="text/css" media="${self.annotation}">
<cms:include bean="${self}" view="css"/></style></c:when
><c:otherwise><c:set var="link"><cms:link bean="${self}" view="css"/></c:set
><link rel="stylesheet" href="${link}" type="text/css" media="${self.annotation}"/></c:otherwise
></c:choose>