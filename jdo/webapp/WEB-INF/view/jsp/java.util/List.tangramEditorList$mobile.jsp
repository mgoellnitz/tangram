<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><?xml version="1.0" encoding="UTF-8" ?><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.view.Utils"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - Liste</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width = 320, initial-scale = 0.95" />  
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/mobile.css" type="text/css" media="screen" />
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/print.css" type="text/css" media="print" />
<link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/e/favicon.ico" />
<script type="text/javascript" src="<%=Utils.getUriPrefix(request)%>/editor/script.js">
</script>
</head>
<c:forEach items="${self}" var="item"><c:set var="oneItem" value="${item}" /></c:forEach>
<body>
<div class="cms_editor_row"><span class="cms_editor_label">Objekte Auflisten 
| <a href="#" onclick="window.close();">[ X ]</a><br/>
Typ: </span>${oneItem.class.package.name}.<span class="cms_editor_title">${oneItem.class.simpleName}</span>
</div>
<div class="cms_editor_row">
<c:forEach items="${self}" var="item">
<cms:include bean="${item}" view="tangramEditorItem$mobile"/>
</c:forEach>
</div>
<form method="post" action="<cms:link bean="${self}" action="create"/>">
<div class="cms_editor_row">
<input class="cms_editor_textfield" type="hidden" name="cms.editor.class.name" value="${oneItem.class.name}"/>
<input type="submit" value="  Neues Objekt erzeugen  " />
</div>
</form>
<hr/>
<cms:include bean="${self}" view="tangramEditorClasses$mobile"/>
<cms:include bean="${self}" view="tangramEditorFooter$mobile" />
</body>
</html>
