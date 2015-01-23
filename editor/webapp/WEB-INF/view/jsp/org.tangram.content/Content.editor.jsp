<%@page isELIgnored="false" language="java" session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.Constants"
%><c:set var="infoLineClass" value="tangram_infobar"/><fmt:setBundle basename="org.tangram.editor.Messages" var="msg"
/><c:if test="${tangramAdminUser && !tangramLiveSystem}"
	><p class="tangram_edit_toolbar"><a <cms:link bean="${self}" action="edit" href="true" target="true" handlers="true"/> ><fmt:message key="label.editobject" bundle="${msg}"/></a>
 | <%=application.getServerInfo()%> / <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%> /
 <%= JspFactory.getDefaultFactory().getEngineInfo().getSpecificationVersion()%> | tangram <%=Constants.VERSION %> 
</p><c:set var="infoLineClass" value="tangram_edit_toolbar"/>
</c:if><%if (request.getAttribute("start.time") != null) { %><p class="${infoLineClass}">
(<%=System.currentTimeMillis()-(Long)(request.getAttribute("start.time"))%>ms <fmt:message key="text.renderingtime" bundle="${msg}"/>)
</p><% } /* if */ %>