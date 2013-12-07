<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@page import="java.util.Map,java.util.Calendar,java.text.DateFormat"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.components.TangramServices"
%><%@page import="org.tangram.feature.blob.MimedBlob"
%><% MimedBlob imageData = (MimedBlob)(request.getAttribute(Constants.THIS));
String type = imageData.getMimeType();
if ((type == null) || (type.length() == 0)) {
    type = "image/jpeg";
} // if
response.setContentType(type);
byte[] bytes = imageData.getBytes();
if (bytes != null) {
  DateFormat httpDateFormat = TangramServices.getHttpHeaderDateFormat();
  Map<String, Object> viewSettings = TangramServices.getViewSettings();
  int cacheTimeMinutes = Integer.parseInt(""+viewSettings.get("imageCacheTime"));
  Calendar calendar = Calendar.getInstance();
  response.setHeader("Last-modified", httpDateFormat.format(calendar.getTime()));
  calendar.add(Calendar.MINUTE, cacheTimeMinutes);
  response.setHeader("Expires", httpDateFormat.format(calendar.getTime()));  
  response.getOutputStream().write(bytes);
} // if
%>