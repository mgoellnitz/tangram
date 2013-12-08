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
<title>Tangram - <cms:include bean="${self}" view="description"/>: ${designClass.simpleName}</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="${prefix}/editor/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="${prefix}/editor/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="${prefix}/t/e/favicon.ico" />
<script type="text/javascript" src="${prefix}/editor/ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="${prefix}/editor/codemirror/js/codemirror.js"></script>
<script type="text/javascript" src="${prefix}/editor/script.js"></script>
</head>
<body><c:set var="normalView"><c:catch><cms:link bean="${self}" href="true" target="true" /></c:catch></c:set
><form id="tangram" method="post" action="<cms:link bean="${self}" action="store"/>" enctype="multipart/form-data">
<div class="cms_editor_row"><span class="cms_editor_label">
Bearbeiten </span>| 
Typ: ${designClassPackage.name}.<span class="cms_editor_title">${designClass.simpleName}</span>
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
JavaBean bw = new JavaBean(request.getAttribute(Constants.THIS));
for (String key : bw.propertyNames()) {
    if (!EditingHandler.SYSTEM_PROPERTIES.contains(key)) {          
        if (bw.isWritable(key)) {
    Object value = bw.get(key);
    @SuppressWarnings("unchecked")
    Class<? extends Object> type = bw.getType(key);
%><tr class="cms_editor_row"><td class="cms_editor_label_td"><%=key%>:
<% 
if ((TangramServices.getPropertyConverter().isTextType(type)) &&("code".equals(key)) && bw.isReadable("mimeType") && (!(""+bw.get("mimeType")).equals("text/javascript")) && (!(""+bw.get("mimeType")).equals("text/css"))) {
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
	if ("application/x-groovy".equals(mimeType)) {
    %>delegate kennt<br/><%
    } else {
    %>$self kennt<br/><%
    } // if
} else {
    %><%=annotation == null ? "" : annotation+" ist kein<br/>gültiger Klassenname!"%><%
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
if (TangramServices.getPropertyConverter().isBlobType(type)) {
    long blobLength = TangramServices.getPropertyConverter().getBlobLength(bw.get(key));
%><td class="cms_editor_field_value"><input class="cms_editor_blobfield" type="file" name="<%=key%>" /> (<%=blobLength%>)</td><%
    } else {
%><td class="cms_editor_field_value">
<%
    if (TangramServices.getPropertyConverter().isTextType(type)) {
      if (!("code".equals(key))) {
%>
<textarea id="ke<%=key%>" class="cms_editor_textfield ckeditor" cols="60" rows="5" name="<%=key%>"><%=TangramServices.getPropertyConverter().getEditString(value)%></textarea>
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
            String mimeType = ""+bw.get("mimeType");
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
%><textarea class="cms_editor_textfield" cols="60" rows="25" name="<%=key%>"><%=TangramServices.getPropertyConverter().getEditString(value)%></textarea><%
        } else {
%><textarea id="code" class="cms_editor_textfield" name="<%=key%>"><%=TangramServices.getPropertyConverter().getEditString(value)%></textarea>
<script type="text/javascript">
  var editor = CodeMirror.fromTextArea('code', {
    height: "dynamic", continuousScanning: 500, path: "${prefix}/editor/codemirror/js/",
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
<input class="cms_editor_textfield" name="<%=key%>" value="<%=TangramServices.getPropertyConverter().getEditString(value)%>" />
 (<%=type.getSimpleName()%><%
if (value instanceof Collection) {
  Class<? extends Object>  elementClass = bw.getCollectionType(key);
  boolean abstractClass = (elementClass.getModifiers() & Modifier.ABSTRACT) == Modifier.ABSTRACT;
  if (elementClass != Object.class) {
%>&lt;<%=(abstractClass?"*":"")+elementClass.getSimpleName()%>&gt;)<%
  } // if
  request.setAttribute("propertyValue", value);
  request.setAttribute("elementClass", elementClass); 
  request.setAttribute("beanFactory", TangramServices.getBeanFactory()); 
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
	var url = '<cms:link bean="${self}" action="link"/>?<%=EditingHandler.PARAMETER_PROPERTY%>=<%=key%>&<%=EditingHandler.PARAMETER_ID%>=${self.id}&<%=EditingHandler.PARAMETER_CLASS_NAME%>='+e.value
	location.href=url;
} //
</script>
<a href="javascript:new<%=fid%>();">[Neues Element]</a><%
fid++;
%></c:if><%
} else {
%>)<%
} // if 
if (value instanceof MutableContent) {
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
(${self.class.name})<br/>
<cms:include bean="${self}" view="tangramEditorClasses" />
<cms:include bean="${self}" view="tangramEditorFooter" />
<script type="text/javascript">
window.focus();window.onkeydown=keydown;window.onkeypress=keypress;
</script>
</body>
</html>
