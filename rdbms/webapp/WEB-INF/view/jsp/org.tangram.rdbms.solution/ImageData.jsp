<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Map"
%><%@page import="java.util.Calendar"
%><%@page import="java.util.TimeZone"
%><%@page import="java.text.DateFormat"
%><%@page import="org.springframework.context.ApplicationContext"
%><%@page import="org.springframework.web.servlet.DispatcherServlet"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.rdbms.solution.ImageData"
%><% ImageData imageData = (ImageData)(request.getAttribute(Constants.THIS));
String type = imageData.getMimeType();
if ((type == null) || (type.length() == 0)) {
    type = "image/jpeg";
} // if
response.setContentType(type);
byte[] bytes = imageData.getBytes();
if (bytes != null) {
  ApplicationContext appContext = (ApplicationContext)request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
  DateFormat httpDateFormat = appContext.getBean("httpHeaderDateFormat", DateFormat.class);
  httpDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

  @SuppressWarnings("rawtypes")
  Map viewSettings = appContext.getBean("viewSettings", Map.class);
  int cacheTimeMinutes = Integer.parseInt(""+viewSettings.get("imageCacheTime"));
  
  Calendar calendar = Calendar.getInstance();
  response.setHeader("Last-modified", httpDateFormat.format(calendar.getTime()));
  calendar.add(Calendar.MINUTE, cacheTimeMinutes);
  response.setHeader("Expires", httpDateFormat.format(calendar.getTime()));
  
  response.getOutputStream().write(bytes);
} // if
%>