<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><c:set var="normalView"><c:catch><cms:link bean="${self}" href="true" target="true" /></c:catch></c:set
><li><div class="cms_editor_id_box">${self.id}</div> 
| <a href="<cms:link bean="${self}" action="edit" />">Bearbeiten</a> 
<c:if test="${! empty normalView}">| <a <c:out value="${normalView}" escapeXml="false"/>>Web Ansicht</a></c:if>
| <span class="cms_editor_title"><cms:include bean="${self}" view="name"/> </span></li>