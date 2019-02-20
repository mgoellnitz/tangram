<%@page isELIgnored="false" language="java" session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><hr /><fmt:setBundle basename="org.tangram.Messages" var="msg"/>
<div class="tangram_footer">
  Tangram Framework <span class="longversion"><%=org.tangram.Constants.VERSION%><c:catch><c:if test="${! empty implementation}"> (${implementation})</c:if></c:catch> </span>| &copy; 2010-2019
<c:if test="${! empty tangramLogoutUrl}">| <a href="${tangramLogoutUrl}"><fmt:message key="label.logout" bundle="${msg}"/></a></c:if></div>