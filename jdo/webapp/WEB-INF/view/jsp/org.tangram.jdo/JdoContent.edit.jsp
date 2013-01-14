<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><?xml version="1.0" encoding="UTF-8" ?><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Collection"
%><%@page import="java.lang.reflect.Modifier"
%><%@page import="java.beans.PropertyDescriptor"
%><%@page import="org.springframework.beans.BeanWrapper"
%><%@page import="org.springframework.beans.BeanWrapperImpl"
%><%@page import="org.springframework.beans.BeanUtils"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.view.Utils"
%><%@page import="org.tangram.jdo.JdoContent"
%><%@page import="org.tangram.jdo.edit.EditingController"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - <cms:include bean="${self}" view="description"/>: ${self.class.simpleName}</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="<%=Utils.getUriPrefix(request)%>/editor/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="<%=Utils.getUriPrefix(request)%>/t/e/favicon.ico" />
<script type="text/javascript" src="<%=Utils.getUriPrefix(request)%>/editor/ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="<%=Utils.getUriPrefix(request)%>/editor/codemirror/js/codemirror.js"></script>
<script type="text/javascript" src="<%=Utils.getUriPrefix(request)%>/editor/script.js"></script>
</head>
<body><c:set var="normalView"><c:catch><cms:link bean="${self}" href="true" target="true" /></c:catch></c:set
><form id="tangram" method="post" action="<cms:link bean="${self}" action="store"/>" enctype="multipart/form-data">
<div class="cms_editor_row"><span class="cms_editor_label">
Bearbeiten </span>| 
Typ: ${self.class.package.name}.<span class="cms_editor_title">${self.class.simpleName}</span>
| <span class="cms_editor_label">ID: </span>${self.id}
<c:if test="${! empty normalView}">| <a <c:out value="${normalView}" escapeXml="false"/>>Web Ansicht</a></c:if> 
| <a href="#" onclick="window.close();">[ X ]</a> 
&#160; &#160; <input type="submit" value="    Sichern    " class="cms_editor_button"/>
</div>
<div style="color: #FF0000;">
<pre><c:forEach items="${compilationErrors}" var="compilationError">
<c:out value="${compilationError}"/>
</c:forEach></pre>
</div>
<table class="cms_editor_table">
<%
int fid = 0; // form ids
BeanWrapper bw = new BeanWrapperImpl(request.getAttribute(Constants.THIS));
for (PropertyDescriptor desc : bw.getPropertyDescriptors()) {
    String key = desc.getName();

    if (!EditingController.SYSTEM_PROPERTIES.contains(key)) {          
        if (desc.getWriteMethod() != null) {
    Object value = bw.getPropertyValue(key);
    @SuppressWarnings("unchecked")
    Class<? extends Object> type = bw.getPropertyType(key);
%><tr class="cms_editor_row"><td class="cms_editor_label_td"><%=key%>:
<% 
if ((Utils.getPropertyConverter(request).isTextType(type)) &&("code".equals(key)) && bw.isReadableProperty("mimeType") && (!(""+bw.getPropertyValue("mimeType")).equals("text/javascript")) && (!(""+bw.getPropertyValue("mimeType")).equals("text/css"))) {
Class<? extends Object> c = null;
String className = ""+bw.getPropertyValue("annotation");
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
    ps = BeanUtils.getPropertyDescriptors(c);
    String mimeType = ""+bw.getPropertyValue("mimeType");
	if ("application/x-groovy".equals(mimeType)) {
    %>delegate kennt<br/><%
    } else {
    %>$self kennt<br/><%
    } // if
} else {
    %><%=className %> ist kein<br/>gültiger Klassenname!<%
} // if
for (PropertyDescriptor p : ps) {
    if ((p.getReadMethod() != null) && (!EditingController.SYSTEM_PROPERTIES.contains(p.getName()))) {
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
if (Utils.getPropertyConverter(request).isBlobType(type)) {
    long blobLength = Utils.getPropertyConverter(request).getBlobLength(bw.getPropertyValue(key));
%><td class="cms_editor_field_value"><input class="cms_editor_blobfield" type="file" name="<%=key%>" /> (<%=blobLength%>)</td><%
    } else {
%><td class="cms_editor_field_value">
<%
    if (Utils.getPropertyConverter(request).isTextType(type)) {
      if (!("code".equals(key))) {
%>
<textarea id="ke<%=key%>" class="cms_editor_textfield ckeditor" cols="60" rows="5" name="<%=key%>"><%=Utils.getPropertyConverter(request).getEditString(value)%></textarea>
<script type="text/javascript">
//<![CDATA[
CKEDITOR.replace( 'ke<%=key%>',	{ skin : 'v2' });
//]]>
</script>
<%
    } else {
        String parserfile = "";
        String stylesheet = "";
        try {
            String mimeType = ""+bw.getPropertyValue("mimeType");
            if ("application/x-groovy".equals(mimeType)) {
                parserfile = "['tokenizegroovy.js', 'parsegroovy.js']";
                stylesheet = "'"+Utils.getUriPrefix(request)+"/editor/codemirror/css/groovycolors.css'";
            } // if
            if ("text/javascript".equals(mimeType)) {
                parserfile = "['tokenizejavascript.js', 'parsejavascript.js']";
                stylesheet = "'"+Utils.getUriPrefix(request)+"/editor/codemirror/css/jscolors.css'";
            } // if
            if ("text/css".equals(mimeType)) {
                parserfile = "'parsecss.js'";
                stylesheet = "'"+Utils.getUriPrefix(request)+"/editor/codemirror/css/csscolors.css'";
            } // if
            if ("text/xml".equals(mimeType)) {
                parserfile = "'parsexml.js'";
                stylesheet = "'"+Utils.getUriPrefix(request)+"/editor/codemirror/css/xmlcolors.css'";
            } // if
            if ("text/html".equals(mimeType)) {
                parserfile = "['parsexml.js', 'parsecss.js', 'tokenizejavascript.js', 'parsejavascript.js', 'parsehtmlmixed.js']";
                stylesheet = "['"+Utils.getUriPrefix(request)+"/editor/codemirror/css/xmlcolors.css', '"+Utils.getUriPrefix(request)+"/editor/codemirror/css/jscolors.css', '"+Utils.getUriPrefix(request)+"/editor/codemirror/css/csscolors.css']";
            } // if
        } catch (Exception e) {
            
        } // try/catch
        if (parserfile.length() == 0) {
%><textarea class="cms_editor_textfield" cols="60" rows="25" name="<%=key%>"><%=Utils.getPropertyConverter(request).getEditString(value)%></textarea><%
        } else {
%><textarea id="code" class="cms_editor_textfield" name="<%=key%>"><%=Utils.getPropertyConverter(request).getEditString(value)%></textarea>
<script type="text/javascript">
  var editor = CodeMirror.fromTextArea('code', {
    height: "dynamic", continuousScanning: 500, path: "<%=Utils.getUriPrefix(request)%>/editor/codemirror/js/",
    parserfile: <%=parserfile%>,
    stylesheet: <%=stylesheet%>,
    lineNumbers: true
  });
</script>
<%
        } // if
    } // if
  } else {
%>
<input class="cms_editor_textfield" name="<%=key%>" value="<%=Utils.getPropertyConverter(request).getEditString(value)%>" />
 (<%=type.getSimpleName()%><%
if (value instanceof Collection) {
  Class<? extends Object>  elementClass = bw.getPropertyTypeDescriptor(key).getElementType();
  boolean abstractClass = (elementClass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
  if (elementClass != Object.class) {
%>&lt;<%=(abstractClass?"*":"")+elementClass.getSimpleName()%>&gt;)<%
  } // if
  request.setAttribute("propertyValue", value);   
  request.setAttribute("elementClass", elementClass); 
%><c:if test="${! empty self.beanFactory.implementingClassesMap[elementClass]}">
<br/><c:forEach items="${propertyValue}" var="item">
<a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a> 
</c:forEach><form method="get" id="f<%=fid%>" action="<cms:link bean="${self}" action="link"/>" class="cms_editor_inline">
<input type="hidden" name="<%=EditingController.PARAMETER_PROPERTY%>" value="<%=key%>"/>
<input type="hidden" name="<%=EditingController.PARAMETER_ID%>" value="<c:out value="${self.id}"/>"/>
<select name="<%=EditingController.PARAMETER_CLASS_NAME%>" id="submit<%=fid++%>">
<c:forEach items="${self.beanFactory.implementingClassesMap[elementClass]}" var="c"
><option value="${c.name}">${c.simpleName}</option>
</c:forEach
></select>
</form>
<a href="javascript:document.getElementById('f<%=fid++%>').submit()">[Neues Element]</a>
</c:if><%
} else {
%>)<%
} // if 
if (value instanceof JdoContent) {
    request.setAttribute("item", value); 
%><br/>
 <a href="<cms:link bean="${item}" action="edit"/>">[<cms:include bean="${item}" view="description"/>]</a> 
<% } // if %>
<% } // if %>
 </td>
<% } // if %>
</tr><%
            } // if
        } // if
    } // for
%>
</table>
</form>
<cms:include bean="${self}" view="tangramEditorClasses" />
<cms:include bean="${self}" view="tangramEditorFooter" />
<script type="text/javascript">
window.focus();window.onkeydown=keydown;window.onkeypress=keypress;
</script>
</body>
</html>
