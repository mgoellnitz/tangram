<%@page isELIgnored="false" language="java" pageEncoding="UTF-8" session="false"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags"
%><%@page import="java.util.Map,java.util.Calendar,java.text.DateFormat,java.io.InputStreamReader"
%><%@page import="org.tangram.Constants,org.tangram.content.CodeResource"
%><%@page import="org.tangram.view.Utils,com.yahoo.platform.yui.compressor.CssCompressor"
%><% CodeResource code = (CodeResource)(request.getAttribute(Constants.THIS));
// hard code mimetype
response.setContentType(Constants.MIME_TYPE_CSS);
Calendar calendar = Calendar.getInstance();
response.setHeader("Last-modified", Utils.HTTP_HEADER_DATE_FORMAT.format(calendar.getTime()));
  Map<String, Object> viewSettings = (Map<String, Object>)application.getAttribute(Constants.ATTRIBUTE_VIEW_SETTINGS);
if (viewSettings.get("cssCacheTime") != null) {
  int cacheTimeMinutes = Integer.parseInt(""+viewSettings.get("cssCacheTime"));
  calendar.add(Calendar.MINUTE, cacheTimeMinutes);
  response.setHeader("Cache-Control", "max-age="+(cacheTimeMinutes*60));
  response.setHeader("Expires", Utils.HTTP_HEADER_DATE_FORMAT.format(calendar.getTime()));
} // if
InputStreamReader isr = new InputStreamReader(code.getStream());
CssCompressor csc = new CssCompressor(isr);
csc.compress(out, 0);
// Uggly hack: We had to use this new line to switch content type reliably
%>
