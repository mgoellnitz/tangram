<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><?xml version="1.0" encoding="UTF-8" ?><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.view.Utils"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html><c:forEach items="${self}" var="item"
><c:set var="oneItem" value="${item}" /></c:forEach
><head>
<title>Tangram - Liste ${designClass.name}</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${prefix}/editor/screen.css" type="text/css" media="screen" />
<link rel="stylesheet" href="${prefix}/editor/print.css" type="text/css" media="print" />
<link rel="shortcut icon" href="${prefix}/t/e/favicon.ico" />
<script type="text/javascript" src="${prefix}/editor/script.js">
</script>
</head>
<body>
<div class="cms_editor_row"><span class="cms_editor_label">Objekte Auflisten | 
Typ: </span> ${designClass.package.name}.<span class="cms_editor_title">${designClass.simpleName}</span>
| <a href="#" onclick="window.close();">[ X ]</a></div>
<div class="cms_editor_row">
<ul><c:forEach items="${self}" var="item">
<cms:include bean="${item}" view="tangramEditorItem"/>
</c:forEach></ul>
</div>
<form method="post" action="<cms:link bean="${self}" action="create"/>">
<div class="cms_editor_row">
<input class="cms_editor_textfield" type="hidden" name="cms.editor.class.name" value="${designClass.name}"/>
<input type="submit" value="  Neues Objekt dieser Klasse erzeugen  " />
</div>
</form>
<c:if test="${! empty oneItem}"><hr/></c:if>
<cms:include bean="${self}" view="tangramEditorClasses"/>
<cms:include bean="${self}" view="tangramEditorFooter" />
</body>
</html>
