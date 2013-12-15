<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Map,java.util.Calendar,java.text.DateFormat"
%><%@page import="java.io.PrintWriter,java.io.InputStream,java.io.InputStreamReader" 
%><%@page import="org.tangram.Constants,org.tangram.content.CodeResource,org.tangram.components.TangramServices"
%><%@page import="org.tangram.view.Utils,com.yahoo.platform.yui.compressor.JavaScriptCompressor"
%><% CodeResource code = (CodeResource)(request.getAttribute(Constants.THIS));
// hard coded mimetype
response.setContentType("text/javascript");
Calendar calendar = Calendar.getInstance();
response.setHeader("Last-modified", Utils.HTTP_HEADER_DATE_FORMAT.format(calendar.getTime()));
Map<String, Object> viewSettings = TangramServices.getViewSettings();
if (viewSettings.get("jsCacheTime") != null) {
  int cacheTimeMinutes = Integer.parseInt(""+viewSettings.get("jsCacheTime"));
  calendar.add(Calendar.MINUTE, cacheTimeMinutes);
  response.setHeader("Expires", Utils.HTTP_HEADER_DATE_FORMAT.format(calendar.getTime()));
}// if
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
