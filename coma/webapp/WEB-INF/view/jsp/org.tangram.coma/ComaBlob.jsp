<%@page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="cm" uri="http://www.coremedia.com/2004/objectserver-1.0-2.0" 
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.coma.ComaBlob"
%><% ComaBlob imageData = (ComaBlob)(request.getAttribute(Constants.THIS));
String type = imageData.getMimeType();
if ((type == null) || (type.length() == 0)) {
    type = "image/jpeg";
} // if
response.setContentType(type);
%><cm:include self="${self.data}" />