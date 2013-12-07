<%@page isELIgnored="false" language="java" session="false"
        contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.view.Utils"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <title>Tangram - Import und Export</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/screen.css" type="text/css" media="screen"/>
    <link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/print.css" type="text/css" media="print"/>
    <link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/favicon.ico" />
  </head>
  <body>
    Ich m&ouml;chte alle Daten aus dem Tangram <a href="export">herunterladen</a>
    <br/>
    <br/>
    <form action="import" method="post">
      Ich m&ouml;chte die Datei 
      <input name="xmltext" type="text"/>
      <input name="submit" type="submit" value="einsetzen"/>.
    </form>
    <br/>
    <hr/>
    <cms:include bean="${self}" view="tangramEditorClasses" />
    <cms:include bean="${self}" view="tangramEditorFooter" />
  </body>
</html>
