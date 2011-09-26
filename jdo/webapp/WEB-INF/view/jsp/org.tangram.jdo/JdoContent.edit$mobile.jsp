<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.List"
%><%@page import="java.beans.PropertyDescriptor"
%><%@page import="org.springframework.beans.BeanWrapper"
%><%@page import="org.springframework.beans.BeanWrapperImpl"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.view.Utils"
%><%@page import="org.tangram.jdo.edit.EditingController"
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
&#160; &#160; <input type="submit" value="    Sichern    " />
<br/>
Typ: </span>${self.class.package.name}.<span class="cms_editor_title">${self.class.simpleName}</span><br/>
<span class="cms_editor_label">ID: </span>${self.id}
<c:if test="${! empty normalView}"><br/><a <c:out value="${normalView}"/>>Web Ansicht</a></c:if>
</div>
<div class="cms_editor_table">
<%
    BeanWrapper bw = new BeanWrapperImpl(request.getAttribute(Constants.THIS));
for (PropertyDescriptor desc : bw.getPropertyDescriptors()) {
    String key = desc.getName();

    if (!Constants.SYSTEM_PROPERTIES.contains(key)) {          
      if (desc.getWriteMethod() != null) {
        Object value = bw.getPropertyValue(key); 
        @SuppressWarnings("unchecked")
        Class type = bw.getPropertyType(key);
%><div class="cms_editor_row"><span class="cms_editor_label"><%=key%></span> (<%=type.getSimpleName()%>)<br/><%
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
if (value instanceof List) {
    request.setAttribute("propertyValue", value); 
%><br/><c:forEach items="${propertyValue}" var="item">
 <a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a> 
</c:forEach>
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
