<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.jdo.TangramJDO"
%><c:set var="infoLineClass" value="tangram_infobar"/>
<c:if test="${! empty tangramAdminUser && empty tangramLiveSystem}"
	><p class="tangram_edit_toolbar"><a <cms:link bean="${self}" action="edit" href="true" target="true" handlers="true"/> >Objekt Bearbeiten</a>
 | tangram <%=Constants.getVersion() %> JDO <%=TangramJDO.getVersion() %> 
<%--<c:if test="${! empty logoutUrl}">| <a href="${logoutUrl}">Abmelden</a></c:if>--%>
</p><c:set var="infoLineClass" value="tangram_edit_toobar"/>
</c:if><p class="${infoLineClass}">
(<%=System.currentTimeMillis()-(Long)(request.getAttribute("start.time"))%>ms rendering time for this page)
</p>
