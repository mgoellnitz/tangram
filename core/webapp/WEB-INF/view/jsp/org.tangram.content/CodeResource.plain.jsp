<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.content.CodeResource"
%><%@page import="org.tangram.Constants"
%><% CodeResource code = (CodeResource)(request.getAttribute(Constants.THIS));
String type = code.getMimeType();
if ((type == null) || (type.length() == 0)) {
    type = "text/plain";
} // if
// response.setHeader("ETag", ""+code.hashCode());
response.setHeader("Expires", "0");
response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
response.setContentType(type);
// Uggly hack: We had to use this new line to switch content type reliably
%>
<cms:include bean="${self.code}"/>