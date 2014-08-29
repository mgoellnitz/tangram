<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><c:set var="normalView"><c:catch><cms:link bean="${self}" href="true" target="true" /></c:catch></c:set
><span class="cms_editor_title"><cms:include bean="${self}" view="name"/></span>
<br/>${self.id} <fmt:setBundle basename="org.tangram.editor.Messages" var="msg"/>
<br/><a href="<cms:link bean="${self}" view="edit" />"><fmt:message key="label.edit" bundle="${msg}"/></a> 
<c:if test="${! empty normalView}">| <a <c:out value="${normalView}" escapeXml="false" />><fmt:message key="label.webview" bundle="${msg}"/></a></c:if>
<br/>