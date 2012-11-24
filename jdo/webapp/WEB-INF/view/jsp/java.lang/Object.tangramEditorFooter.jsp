<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"
	%><%@page import="org.tangram.Constants"
%><hr />
<div class="cms_editor_footer">
Tangram Object Presenter <%=Constants.VERSION%> | &copy; 2010-2012 
| Warranty void when reading this ;-) 
<c:if test="${! empty logoutUrl}">| <a href="${logoutUrl}">Abmelden</a></c:if></div>
