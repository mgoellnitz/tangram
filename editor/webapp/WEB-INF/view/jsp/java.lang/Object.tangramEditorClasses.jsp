<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><fmt:setBundle basename="org.tangram.editor.Messages" var="msg"/><%@page import="org.tangram.components.editor.EditingHandler"
%><div class="cms_editor_row"><span class="cms_editor_title"><fmt:message key="text.availableclasses" bundle="${msg}"/>:</span>
<ul><c:forEach items="${classes}" var="c">
<li><a href="<cms:link bean="${self}" action="create"/>?<%=EditingHandler.PARAMETER_CLASS_NAME%>=${c.name}">[ <fmt:message key="label.new" bundle="${msg}"/> ]</a> |
<%Class<? extends Object> c = (Class<? extends Object>)pageContext.getAttribute("c");%><%=c.getPackage().getName()%>.<a href="<cms:link bean="${self}" action="list"/>?<%=EditingHandler.PARAMETER_CLASS_NAME%>=${c.name}">${c.simpleName}</a>
</li>
</c:forEach></ul>
</div>
