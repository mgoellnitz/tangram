<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="org.tangram.content.CodeResource"
%><%@page import="org.tangram.Constants"
%><%@page import="com.yahoo.platform.yui.compressor.JavaScriptCompressor"
%><%@page import="org.springframework.context.ApplicationContext"
%><%@page import="java.io.InputStream"
%><%@page import="java.io.InputStreamReader"
%><%@page import="java.text.DateFormat"
%><%@page import="java.util.Calendar"
%><%@page import="java.util.TimeZone"
%><%@page import="java.util.Map"
%><% CodeResource code = (CodeResource)(request.getAttribute(Constants.THIS));
//hard code mimetype
response.setContentType("text/javascript");

ApplicationContext appContext = (ApplicationContext)request.getAttribute(Constants.ATTRIBUTE_CONTEXT);
DateFormat httpDateFormat = appContext.getBean("httpHeaderDateFormat", DateFormat.class);
httpDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

@SuppressWarnings("unchecked")
Map viewSettings = appContext.getBean("viewSettings", Map.class);
int cacheTimeMinutes = Integer.parseInt(""+viewSettings.get("jsCacheTime"));
Calendar calendar = Calendar.getInstance();
response.setHeader("Last-modified", httpDateFormat.format(calendar.getTime()));
calendar.add(Calendar.MINUTE, cacheTimeMinutes);
response.setHeader("Expires", httpDateFormat.format(calendar.getTime()));

InputStream is = code.getStream();
if (is != null) {
  InputStreamReader isr = new InputStreamReader(is);
  try {
    JavaScriptCompressor jsc = new JavaScriptCompressor(isr, null); 
    jsc.compress(out, 80, false, false, false, false);
  } catch (Exception e) {
     out.write("/* Error while minifying JavasScript - sure that this is correct code? */");
     out.write(code.getCodeText());
  } // try/catch
} // if
// Uggly hack: We had to use this new line to switch content type reliably
%>
