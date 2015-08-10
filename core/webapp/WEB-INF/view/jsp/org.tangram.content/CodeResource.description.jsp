<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><c:choose
><c:when test="${! empty self.annotation}">${self.annotation}</c:when
><c:otherwise>-</c:otherwise></c:choose>