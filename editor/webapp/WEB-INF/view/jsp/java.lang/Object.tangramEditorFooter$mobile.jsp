<%@page isELIgnored="false" language="java" session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><hr /><fmt:setBundle basename="org.tangram.editor.Messages" var="msg"/>
<div class="cms_editor_footer">Tangram Object Presenter | &copy; 2010-2014 <c:if 
test="${! empty logoutUrl}">| <a href="${logoutUrl}"><fmt:message key="label.logout" bundle="${msg}"/></a></c:if></div>
