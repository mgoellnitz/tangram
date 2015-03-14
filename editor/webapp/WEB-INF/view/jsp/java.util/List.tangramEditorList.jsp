<%@page isELIgnored="false" language="java" session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.view.Utils"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html><fmt:setBundle basename="org.tangram.editor.Messages" var="msg"/>
<head>
<title>Tangram - <fmt:message key="label.list" bundle="${msg}"/> ${designClass.name}</title>
<%@include file="../../../include/head-elements.jsp" %>
<link rel="stylesheet" href="${prefix}/editor/screen.css" type="text/css" media="screen" />
<link rel="stylesheet" href="${prefix}/editor/print.css" type="text/css" media="print" />
<script type="application/javascript" src="${prefix}/editor/script.js">
</script>
</head>
<body>
  <div class="cms_editor_row"><span class="cms_editor_label"><span class="longversion"><fmt:message key="label.listobjects" bundle="${msg}"/> | </span>
<fmt:message key="label.type" bundle="${msg}"/>: </span> ${designClassPackage.name}.<span class="cms_editor_title">${designClass.simpleName}</span>
| <a href="#" onclick="window.close();">[ X ]</a></div>
<div class="cms_editor_row">
<ul><c:forEach items="${self}" var="item">
<cms:include bean="${item}" view="tangramEditorItem"/>
</c:forEach></ul>
</div>
<c:if test="${designClass != null}">
<form method="post" action="<cms:link bean="${editingHandler}" action="create"/>">
<div class="cms_editor_row">
<input class="cms_editor_textfield" type="hidden" name="cms.editor.class.name" value="${designClass.name}"/>
<input type="submit" value="  <fmt:message key="button.newobject" bundle="${msg}"/>  " />
</div>
</form>
</c:if>
<hr/>
<cms:include bean="${self}" view="tangramEditorClasses"/>
<%@include file="../../../include/tangram-footer.jsp" %>
</body>
</html>
