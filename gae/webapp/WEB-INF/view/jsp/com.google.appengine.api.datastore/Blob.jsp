<%@page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"
%><%@page import="org.tangram.Constants,com.google.appengine.api.datastore.Blob"%><%
response.setContentType("image/jpeg");
Blob blob = (Blob)(request.getAttribute(Constants.THIS));
if (blob != null) {
  if (blob.getBytes() != null) {
    response.getOutputStream().write(blob.getBytes());
  } // if
} // if%>