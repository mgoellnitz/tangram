<%@page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags" 
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><div><fmt:setBundle basename="org.tangram.Messages" var="msg"/>
<div>
<p><fmt:message key="text.login.list" bundle="${msg}"/></p>
<p><fmt:message key="text.login.description" bundle="${msg}"/></p>
</div>
<c:forEach items="${self.providerNames}" var="providerName">
<div><a href="<cms:link bean="${self}" action="${providerName}"/>">${providerName}</a></div>
</c:forEach>
</div>