<%@page isELIgnored="false" language="java" session="false" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"
%><%
request.setAttribute("cmprefix", request.getAttribute("prefix")+"/editor/codemirror");
request.setAttribute("cmlibprefix", request.getAttribute("prefix")+"/editor/codemirror/lib");
request.setAttribute("ckprefix", request.getAttribute("prefix")+"/editor/ckeditor");
%>