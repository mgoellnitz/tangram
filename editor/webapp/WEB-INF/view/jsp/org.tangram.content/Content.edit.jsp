<%@page isELIgnored="false" language="java" session="false" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Collection,java.lang.reflect.Modifier,java.beans.PropertyDescriptor"
%><%@page import="org.tangram.Constants,org.tangram.components.editor.EditingHandler,org.tangram.util.JavaBean"
%><%@page import="org.tangram.mutable.HashModificationTime,org.tangram.content.Content,org.tangram.content.BeanFactory"
%><%@page import="org.tangram.annotate.Abstract,org.tangram.view.Utils,org.tangram.view.PropertyConverter"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html><fmt:setBundle basename="org.tangram.editor.Messages" var="msg"/>
<head>
<title>Tangram - <cms:include bean="${self}" view="description"/>: ${designClass.simpleName}</title>
<%@include file="../../../include/head-elements.jsp" %>
<%@include file="../../../include/editing-components.jsp" %>
<link rel="stylesheet" href="${cmlibprefix}/codemirror.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="${prefix}/editor/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="${prefix}/editor/print.css" type="text/css" media="print"/>
<% if (!(request.getAttribute(Constants.THIS) instanceof org.tangram.content.CodeResource)) { %>
<script type="application/javascript" src="${ckprefix}/ckeditor.js"></script>
<% } else { %>
<script type="application/javascript" src="${cmlibprefix}/codemirror.js"></script>
<script type="application/javascript" src="${cmprefix}/mode/groovy/groovy.js"></script>
<script type="application/javascript" src="${cmprefix}/mode/xml/xml.js"></script>
<script type="application/javascript" src="${cmprefix}/mode/javascript/javascript.js"></script>
<script type="application/javascript" src="${cmprefix}/mode/css/css.js"></script>
<script type="application/javascript" src="${cmprefix}/mode/htmlmixed/htmlmixed.js"></script>
<% } // if %>
<script type="application/javascript" src="${prefix}/editor/script.js"></script>
</head>
<body><c:set var="normalView"><c:catch><cms:link bean="${self}" href="true" target="true" /></c:catch></c:set
><form id="tangram" method="post" action="<cms:link bean="${self}" action="store"/>" enctype="multipart/form-data">
<div class="cms_editor_row"><span class="longversion"><span class="cms_editor_label">
<fmt:message key="label.edit" bundle="${msg}"/></span> |
<fmt:message key="label.type" bundle="${msg}"/>: ${designClassPackage.name}.<span class="cms_editor_title">${designClass.simpleName}</span>
| </span><span class="cms_editor_label">ID: </span>${self.id}
<c:if test="${! empty normalView}">| <a <c:out value="${normalView}" escapeXml="false"/>><fmt:message key="label.webview" bundle="${msg}"/></a></c:if>
| <a href="#" onclick="window.close();">[ X ]</a>
&#160; &#160; <input type="submit" value="    <fmt:message key="button.save" bundle="${msg}"/>    " class="cms_editor_button"/>
</div>
<div style="color: #FF0000;">
<pre><c:forEach items="${compilationErrors}" var="compilationError">
<c:out value="${compilationError}"/>
</c:forEach></pre>
</div>
<table class="cms_editor_table">
<%
BeanFactory beanFactory = (BeanFactory)request.getAttribute("beanFactory");
PropertyConverter propertyConverter = (PropertyConverter)request.getAttribute("propertyConverter");
int fid = 0; // form ids
JavaBean bw = new JavaBean(request.getAttribute(Constants.THIS));
for (String key : bw.propertyNames()) {
    if (!EditingHandler.SYSTEM_PROPERTIES.contains(key)) {
        if (bw.isWritable(key)) {
    Object value = bw.get(key);
    @SuppressWarnings("unchecked")
    Class<? extends Object> type = bw.getType(key);
%><tr class="cms_editor_row"><td class="cms_editor_label_td"><%=key%>:
<%
if ((propertyConverter.isTextType(type)) &&("code".equals(key)) && bw.isReadable("mimeType") && (!(""+bw.get("mimeType")).equals(Constants.MIME_TYPE_JS)) && (!(""+bw.get("mimeType")).equals(Constants.MIME_TYPE_CSS))) {
Class<? extends Object> c = null;
Object annotation = bw.get("annotation");
String className = annotation == null ? null : ""+annotation;
try {
    c = Class.forName(className);
} catch (Exception e) {
    try {
        int idx = className.lastIndexOf(".");
        className = className.substring(0, idx);
        c = Class.forName(className);
    } catch (Exception ex) {
        // who cares...
    } // try/catch
} // try/catch
request.setAttribute("errorStyle", (c == null) ? "color: #FF0000;" : "");%>
<div class="cms_editor_howto" style="${errorStyle}"><%
PropertyDescriptor[] ps = new PropertyDescriptor[0];
if (c != null) {
    ps = JavaBean.getPropertyDescriptors(c);
    String mimeType = ""+bw.get("mimeType");
	if (Constants.MIME_TYPE_GROOVY.equals(mimeType)) {
    %>delegate kennt<br/><%
    } else {
    %>$self kennt<br/><%
    } // if
} else {
    %><%=annotation == null ? "" : annotation%> <fmt:message key="text.novalidclassname" bundle="${msg}"/><%
} // if
for (PropertyDescriptor p : ps) {
    if ((p.getReadMethod() != null) && (!EditingHandler.SYSTEM_PROPERTIES.contains(p.getName()))) {
        String delimiter = "</span> :";
        if ((p.getName()+p.getPropertyType().getSimpleName()).length() > 18) {
            delimiter = "</span><br/>&#160;&#160;:";
        } // if
%><span class="cms_editor_label"><%=p.getName()+delimiter+p.getPropertyType().getSimpleName()%></span><br/><%
    } // if
} // for
%></div><%
} // if
%></td><%
if (propertyConverter.isBlobType(type)) {
    long blobLength = propertyConverter.getBlobLength(bw.get(key));
%><td class="cms_editor_field_value"><input class="cms_editor_blobfield" type="file" name="<%=key%>" /> (<%=blobLength%>)</td><%
    } else {
%><td class="cms_editor_field_value">
<%
    if (propertyConverter.isTextType(type)) {
      if (!("code".equals(key))) {
%>
<textarea id="ke<%=key%>" class="cms_editor_textfield ckeditor" cols="60" rows="5" name="<%=key%>"><%=propertyConverter.getEditString(value)%></textarea>
<script type="application/javascript">
//<![CDATA[
CKEDITOR.replace( 'ke<%=key%>');
//]]>
</script>
<%
    } else {
        String cmmode = "";
        try {
            String mimeType = ""+bw.get("mimeType");
            if (Constants.MIME_TYPE_GROOVY.equals(mimeType)) {
                cmmode = "groovy";
            } // if
            if (Constants.MIME_TYPE_JS.equals(mimeType)) {
                cmmode = "javascript";
            } // if
            if (Constants.MIME_TYPE_CSS.equals(mimeType)) {
                cmmode = "css";
            } // if
            if (Constants.MIME_TYPE_XML.equals(mimeType)) {
                cmmode = "xml";
            } // if
            if (Constants.MIME_TYPE_HTML.equals(mimeType)) {
                cmmode = "htmlmixed";
            } // if
        } catch (Exception e) {

        } // try/catch
        if (cmmode.length() == 0) {
%><textarea class="cms_editor_textfield" cols="60" rows="25" name="<%=key%>"><%=propertyConverter.getEditString(value)%></textarea><%
        } else {
%><textarea id="code" class="cms_editor_textfield" name="<%=key%>"><%=propertyConverter.getEditString(value)%></textarea>
<script type="application/javascript">
  var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
    height: "dynamic", continuousScanning: 500, mode: "<%=cmmode%>", lineNumbers: true
  });
</script>
<%
        } // if
    } // if
  } else {
%>
<input class="cms_editor_textfield" name="<%=key%>" value="<%=propertyConverter.getEditString(value)%>" />
 (<%=type.getSimpleName()%><%
if (value instanceof Collection) {
  Class<? extends Object>  elementClass = bw.getCollectionType(key);
  boolean abstractClass = ((elementClass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT) || (elementClass.getAnnotation(Abstract.class)!=null);
  if (elementClass != Object.class) {
%>&lt;<%=(abstractClass?"*":"")+elementClass.getSimpleName()%>&gt;)<%
  } // if
  request.setAttribute("propertyValue", value);
  request.setAttribute("elementClass", elementClass);
%><c:if test="${! empty beanFactory.implementingClassesMap[elementClass]}">
<br/><c:forEach items="${propertyValue}" var="item">
<a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a>
</c:forEach>
<select name="<%=EditingHandler.PARAMETER_CLASS_NAME%>" id="select<%=fid%>">
<c:forEach items="${beanFactory.implementingClassesMap[elementClass]}" var="c"
><option value="${c.name}">${c.simpleName}</option>
</c:forEach
></select>
<script language="JavaScript">
function new<%=fid%>() {
	var e = document.getElementById('select<%=fid%>');
	var url = '<cms:link bean="${editingHandler}" action="link"/>?<%=EditingHandler.PARAMETER_PROPERTY%>=<%=key%>&<%=EditingHandler.PARAMETER_ID%>=${self.id}&<%=EditingHandler.PARAMETER_CLASS_NAME%>='+e.value
	location.href=url;
} //
</script>
<a href="javascript:new<%=fid%>();">[<fmt:message key="label.newelement" bundle="${msg}"/>]</a><%
fid++;
%></c:if><%
} else {
%>)<%
} // if
if (value instanceof Content) {
    request.setAttribute("item", value);
%><br/>
<a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a>
<% } // if
   } // if
%></td>
<% } // if %>
</tr><%
            } // if
        } // if
    } // for
%>
</table>
</form>
<div class="cms_editor_row"><%
Object self = request.getAttribute(Constants.THIS);
if (self instanceof HashModificationTime) {
%><fmt:message key="label.last.modified" bundle="${msg}"/> <%
  java.util.Date d = new java.util.Date(((HashModificationTime)self).getModificationTime());
  out.write(propertyConverter.getEditString(d));
} // if%>
(${contentClass.name} ${note})</div>
<cms:include bean="${self}" view="tangramEditorClasses" />
<%@include file="../../../include/tangram-footer.jsp" %>
<script type="application/javascript">
window.focus();window.onkeydown=keydown;window.onkeypress=keypress;
</script>
</body>
</html>
