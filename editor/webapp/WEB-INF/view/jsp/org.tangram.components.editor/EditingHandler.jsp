<%@page isELIgnored="false" language="java" session="false"
        contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.view.Utils"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html><fmt:setBundle basename="org.tangram.editor.Messages" var="msg"/>
  <head>
    <title>Tangram - Import / Export</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/screen.css" type="text/css" media="screen"/>
    <link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/screen.css" type="text/css" media="screen"/>
    <link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/t/print.css" type="text/css" media="print"/>
    <link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/print.css" type="text/css" media="print"/>
    <link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/favicon.ico" />
  </head>
  <body>
    <fmt:message key="text.export.prefix" bundle="${msg}"/> <a href="export"><fmt:message key="button.export" bundle="${msg}"/></a><fmt:message key="text.export.suffix" bundle="${msg}"/>
    <br/>
    <br/>
    <fmt:message key="text.codes.prefix" bundle="${msg}"/> <a href="codes.zip"><fmt:message key="button.export" bundle="${msg}"/></a> <fmt:message key="text.codes.suffix" bundle="${msg}"/>
    <br/>
    <br/>
    <form action="import" method="post" enctype="multipart/form-data">
      <fmt:message key="text.upload.prefix" bundle="${msg}"/>
      <input name="xmlfile" type="file"/>
      <input name="submit" type="submit" value="  <fmt:message key="button.upload" bundle="${msg}"/>  "/>.
      <fmt:message key="text.upload.suffix" bundle="${msg}"/>
    </form>
    <br/>
    <br/>
    <form action="import-text" method="post">
      <fmt:message key="text.import.prefix" bundle="${msg}"/>
      <textarea name="xmltext" cols="120" rows="80"></textarea>
      <input name="submit" type="submit" value="  <fmt:message key="button.import" bundle="${msg}"/>  "/>.
      <fmt:message key="text.import.suffix" bundle="${msg}"/>
    </form>
    <br/>
    <hr/>
    <cms:include bean="${self}" view="tangramEditorClasses" />
    <cms:include bean="${self}" view="tangramEditorFooter" />
  </body>
</html>
