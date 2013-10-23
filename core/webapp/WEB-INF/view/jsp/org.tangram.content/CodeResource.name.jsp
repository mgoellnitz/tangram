<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib	prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><c:choose>
<c:when test="${self.mimeType == 'application/x-groovy'}">
Groovy Code ${self.annotation}
</c:when>
<c:when test="${self.mimeType == 'text/javascript'}">
JavaScript Code ${self.annotation}
</c:when>
<c:when test="${self.mimeType == 'text/css'}">
Stylesheet für Medium ${self.annotation}
</c:when>
<c:when test="${self.mimeType == 'text/html'}">
Template für ${self.annotation}
</c:when>
<c:when test="${self.mimeType == 'text/xml'}">
XML Template für ${self.annotation}
</c:when>
<c:otherwise>
Code ${self.annotation}
</c:otherwise>
</c:choose>