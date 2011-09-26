<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.content.CodeResource"
%><%@page import="org.tangram.Constants"
%><c:choose><c:when test="${self.size < 1024}"
><script type="text/javascript">
<cms:include bean="${self}" view="js"/></script></c:when
><c:otherwise><c:set var="link"><cms:link bean="${self}" view="js"/></c:set
><script type="text/javascript" src="${link}"></script></c:otherwise
></c:choose>