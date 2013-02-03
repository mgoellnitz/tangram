<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"
	%><hr />
<div class="cms_editor_footer">
Tangram Object Presenter <%=org.tangram.Constants.VERSION%> | &copy; 2010-2013 | Warranty void when reading this ;-) 
<c:if test="${! empty logoutUrl}">| <a href="${logoutUrl}">Abmelden</a></c:if></div>
