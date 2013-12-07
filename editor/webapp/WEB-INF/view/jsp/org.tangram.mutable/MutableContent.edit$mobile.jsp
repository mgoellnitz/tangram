<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Collection"
%><%@page import="java.lang.reflect.Modifier"
%><%@page import="java.beans.PropertyDescriptor"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.util.JavaBean"
%><%@page import="org.tangram.view.Utils"
%><%@page import="org.tangram.mutable.MutableContent"
%><%@page import="org.tangram.components.TangramServices"
%><%@page import="org.tangram.components.editor.EditingHandler"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - ${designClass.simpleName} (${self.id})</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width = 320, initial-scale = 0.95" />  
<link rel="stylesheet" href="${prefix}/editor/mobile.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="${prefix}/editor/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="${prefix}/t/e/favicon.ico" />
<script type="text/javascript" src="${prefix}/editor/script.js"></script>
</head>
<body><c:set var="normalView"><c:catch><cms:link bean="${self}" href="true" target="true" /></c:catch></c:set
><form id="tangram" method="post" action="<cms:link bean="${self}" action="store"/>" enctype="multipart/form-data">
<div class="cms_editor_row"><span class="cms_editor_label">
Objekt Bearbeiten 
| <a href="#" onclick="window.close();">[ X ]</a> 
&#160; &#160; <input type="submit" value="    Sichern    " class="cms_editor_button"/>
<br/>
Typ: </span>${designClassPackage.name}.<span class="cms_editor_title">${designClass.simpleName}</span><br/>
<span class="cms_editor_label">ID: </span>${self.id}
<c:if test="${! empty normalView}"><br/><a <c:out value="${normalView}" escapeXml="false" />>Web Ansicht</a></c:if>
</div>
<div class="cms_editor_table">
<%
JavaBean bw = new JavaBean(request.getAttribute(Constants.THIS));
for (PropertyDescriptor desc : bw.getPropertyDescriptors()) {
    String key = desc.getName();

    if (!EditingHandler.SYSTEM_PROPERTIES.contains(key)) {          
      if (desc.getWriteMethod() != null) {
        Object value = bw.get(key); 
        @SuppressWarnings("rawtypes")
        Class type = bw.getType(key);
%><div class="cms_editor_row"><span class="cms_editor_label"><%=key%></span> (<%=type.getSimpleName()%><% 
if (value instanceof Collection) {
	Class<? extends Object> elementClass = bw.getCollectionType(key);
	boolean abstractClass = (elementClass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
	%>&lt;<%=(abstractClass?"*":"")+elementClass.getSimpleName()%>&gt;)<%
} // if
%>)<br/><%
    if (TangramServices.getPropertyConverter().isBlobType(type)) {
      long blobLength = TangramServices.getPropertyConverter().getBlobLength(bw.get(key));
%><div class="cms_editor_field_value"><input class="cms_editor_blobfield" type="file" name="<%=key%>" /> (<%=blobLength%>)</div><%
    } else {
%><div class="cms_editor_field_value">
<%
    if (TangramServices.getPropertyConverter().isTextType(type)) {
%>
<textarea class="cms_editor_textfield" cols="60" rows="7" name="<%=key%>"><%=TangramServices.getPropertyConverter().getEditString(value)%></textarea>
<%
    } else {
%>
<input class="cms_editor_textfield" name="<%=key%>" value="<%=TangramServices.getPropertyConverter().getEditString(value)%>" />
<%
if (value instanceof Collection) {
  Class<? extends Object> elementClass = bw.getCollectionType(key)
  boolean abstractClass = (elementClass.getModifiers() | Modifier.ABSTRACT) == Modifier.ABSTRACT;
  request.setAttribute("propertyValue", value);
  request.setAttribute("elementClass", elementClass); 
  request.setAttribute("beanFactory", TangramServices.getBeanFactory()); 
%><c:if test="${! empty beanFactory.implementingClassesMap[elementClass]}">
<br/><c:forEach items="${propertyValue}" var="item">
 <a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a> 
</c:forEach></c:if><%  
} // if 
if (value instanceof MutableContent) {
    request.setAttribute("item", value); 
%><br/>
 <a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a> 
<% } // if %>
<% } // if %>
 </div>
<% } // if %>
</div><%
            } // if
        } // if
    } // for
%>
</div>
</form>
<cms:include bean="${self}" view="tangramEditorClasses$mobile" />
<cms:include bean="${self}" view="tangramEditorFooter$mobile" />
<script type="text/javascript">
// alert(""+window.name);
window.focus();
window.onkeydown=keydown;
window.onkeypress=keypress;
</script>
</body>
</html>
