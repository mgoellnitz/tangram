<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Map,java.util.Calendar,java.text.DateFormat"
%><%@page import="java.io.InputStreamReader"
%><%@page import="org.tangram.content.CodeResource"
%><%@page import="org.tangram.Constants"
%><%@page import="org.tangram.components.spring.TangramSpringServices"
%><%@page import="com.yahoo.platform.yui.compressor.CssCompressor"
%><% CodeResource code = (CodeResource)(request.getAttribute(Constants.THIS));
// hard code mimetype
response.setContentType("text/css");
DateFormat httpDateFormat = TangramSpringServices.getHttpHeaderDateFormat();
Map<String, Object> viewSettings = TangramSpringServices.getViewSettings();
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
