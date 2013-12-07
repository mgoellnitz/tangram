<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Map,java.util.Calendar,java.text.DateFormat"
%><%@page import="java.io.PrintWriter,java.io.InputStream,java.io.InputStreamReader" 
%><%@page import="org.tangram.Constants,org.tangram.content.CodeResource,org.tangram.components.TangramServices"
%><%@page import="com.yahoo.platform.yui.compressor.JavaScriptCompressor"
%><% CodeResource code = (CodeResource)(request.getAttribute(Constants.THIS));
// hard coded mimetype
response.setContentType("text/javascript");
DateFormat httpDateFormat = TangramServices.getHttpHeaderDateFormat();
Map<String, Object> viewSettings = TangramServices.getViewSettings();
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
	 out.write("/* Error while minifying JavasScript - sure that this is correct code?\n");
	 e.printStackTrace(new PrintWriter(out));
	 out.write("\n*/\n");
     out.write(code.getCodeText());
  } // try/catch
} // if
// Uggly hack: We had to use this new line to switch content type reliably
%>
