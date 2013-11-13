<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.components.editor.EditingController"
%><div class="cms_editor_row"><span class="cms_editor_title">Vorhandene Objekt-Klassen:</span>
<c:forEach items="${classes}" var="c">
<br/><a href="<cms:link bean="${self}" action="create"/>?<%=EditingController.PARAMETER_CLASS_NAME%>=${c.name}">Neu</a> |
<%Class<? extends Object> c = (Class<? extends Object>)pageContext.getAttribute("c");%><%=c.getPackage().getName()%>.<a href="<cms:link bean="${self}" action="list"/>?<%=EditingController.PARAMETER_CLASS_NAME%>=${c.name}">${c.simpleName}</a>
</c:forEach>
</div>
