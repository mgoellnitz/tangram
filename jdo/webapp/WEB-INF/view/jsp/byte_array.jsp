<%@page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"
%><%@page import="org.tangram.Constants"%><%
response.setContentType("image/jpeg");
byte[] self = (byte[])(request.getAttribute(Constants.THIS));
if (self!= null) {
  response.getOutputStream().write(self);
} // if%>