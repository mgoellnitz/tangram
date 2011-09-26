<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.content.CodeResource"
%><%@page import="org.tangram.Constants"
%><%@page import="com.yahoo.platform.yui.compressor.CssCompressor"
%><%@page import="org.springframework.context.ApplicationContext"
%><%@page import="java.text.DateFormat"
%><%@page import="java.io.InputStreamReader"
%><%@page import="java.util.Calendar"
%><%@page import="java.util.TimeZone"
%><%@page import="java.util.Map"
%><% CodeResource code = (CodeResource)(request.getAttribute(Constants.THIS));
// hard code mimetype
response.setContentType("text/css");

ApplicationContext appContext = (ApplicationContext)request.getAttribute(Constants.ATTRIBUTE_CONTEXT);
DateFormat httpDateFormat = appContext.getBean("httpHeaderDateFormat", DateFormat.class);
httpDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

@SuppressWarnings("unchecked")
Map viewSettings = appContext.getBean("viewSettings", Map.class);
int cacheTimeMinutes = Integer.parseInt(""+viewSettings.get("cssCacheTime"));

Calendar calendar = Calendar.getInstance();
response.setHeader("Last-modified", httpDateFormat.format(calendar.getTime()));
calendar.add(Calendar.MINUTE, cacheTimeMinutes);
response.setHeader("Expires", httpDateFormat.format(calendar.getTime()));

InputStreamReader isr = new InputStreamReader(code.getStream());
CssCompressor csc = new CssCompressor(isr); 
csc.compress(out, 0);
// Uggly hack: We had to use this new line to switch content type reliably
%>
