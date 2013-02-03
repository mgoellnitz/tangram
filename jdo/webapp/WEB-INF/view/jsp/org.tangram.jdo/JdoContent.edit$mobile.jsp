<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Collection"
%><%@page import="java.lang.reflect.Modifier"
%><%@page import="java.beans.PropertyDescriptor"
%><%@page import="org.springframework.beans.BeanWrapper"
%><%@page import="org.springframework.beans.BeanWrapperImpl"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.view.Utils"
%><%@page import="org.tangram.jdo.JdoContent"
%><%@page import="org.tangram.components.jdo.EditingController"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - ${self.class.simpleName} (${self.id})</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width = 320, initial-scale = 0.95" />  
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/mobile.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/e/favicon.ico" />
<script type="text/javascript" src="<%=Utils.getUriPrefix(request)%>/editor/script.js"></script>
</head>
<body><c:set var="normalView"><c:catch><cms:link bean="${self}" href="true" target="true" /></c:catch></c:set
><form id="tangram" method="post" action="<cms:link bean="${self}" action="store"/>" enctype="multipart/form-data">
<div class="cms_editor_row"><span class="cms_editor_label">
Objekt Bearbeiten 
| <a href="#" onclick="window.close();">[ X ]</a> 
&#160; &#160; <input type="submit" value="    Sichern    " class="cms_editor_button"/>
<br/>
Typ: </span>${self.class.package.name}.<span class="cms_editor_title">${self.class.simpleName}</span><br/>
<span class="cms_editor_label">ID: </span>${self.id}
<c:if test="${! empty normalView}"><br/><a <c:out value="${normalView}" escapeXml="false" />>Web Ansicht</a></c:if>
</div>
<div class="cms_editor_table">
<%
BeanWrapper bw = new BeanWrapperImpl(request.getAttribute(Constants.THIS));
int fid = 0; // form ids
for (PropertyDescriptor desc : bw.getPropertyDescriptors()) {
    String key = desc.getName();

    if (!EditingController.SYSTEM_PROPERTIES.contains(key)) {          
      if (desc.getWriteMethod() != null) {
        Object value = bw.getPropertyValue(key); 
        @SuppressWarnings("rawtypes")
        Class type = bw.getPropertyType(key);
%><div class="cms_editor_row"><span class="cms_editor_label"><%=key%></span> (<%=type.getSimpleName()%><% 
if (value instanceof Collection) {
	Class<? extends Object> elementClass = bw.getPropertyTypeDescriptor(key).getElementType();
	boolean abstractClass = (elementClass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
	%>&lt;<%=(abstractClass?"*":"")+elementClass.getSimpleName()%>&gt;)<%
} // if
%>)<br/><%
    if (Utils.getPropertyConverter(request).isBlobType(type)) {
      long blobLength = Utils.getPropertyConverter(request).getBlobLength(bw.getPropertyValue(key));
%><div class="cms_editor_field_value"><input class="cms_editor_blobfield" type="file" name="<%=key%>" /> (<%=blobLength%>)</div><%
    } else {
%><div class="cms_editor_field_value">
<%
    if (Utils.getPropertyConverter(request).isTextType(type)) {
%>
<textarea class="cms_editor_textfield" cols="60" rows="7" name="<%=key%>"><%=Utils.getPropertyConverter(request).getEditString(value)%></textarea>
<%
    } else {
%>
<input class="cms_editor_textfield" name="<%=key%>" value="<%=Utils.getPropertyConverter(request).getEditString(value)%>" />
<%
if (value instanceof Collection) {
  Class<? extends Object> elementClass = bw.getPropertyTypeDescriptor(key).getElementType();
  boolean abstractClass = (elementClass.getModifiers() | Modifier.ABSTRACT) == Modifier.ABSTRACT;
  request.setAttribute("propertyValue", value);
  request.setAttribute("elementClass", elementClass); 
%><c:if test="${! empty self.beanFactory.implementingClassesMap[elementClass]}">
<br/><c:forEach items="${propertyValue}" var="item">
 <a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a> 
</c:forEach>
<select name="<%=EditingController.PARAMETER_CLASS_NAME%>" id="select<%=fid%>">
<c:forEach items="${self.beanFactory.implementingClassesMap[elementClass]}" var="c"
><option value="${c.name}">${c.simpleName}</option>
</c:forEach
></select>
<script language="JavaScript">
function new<%=fid%>() {
	var e = document.getElementById('select<%=fid%>');
	var url = '<cms:link bean="${self}" action="link"/>?<%=EditingController.PARAMETER_PROPERTY%>=<%=key%>&<%=EditingController.PARAMETER_ID%>=${self.id}&<%=EditingController.PARAMETER_CLASS_NAME%>='+e.value
	location.href=url;
} //
</script>
<a href="javascript:new<%=fid%>();">[Neues Element]</a><%
fid++;
%></c:if><%  
} // if 
if (value instanceof JdoContent) {
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
