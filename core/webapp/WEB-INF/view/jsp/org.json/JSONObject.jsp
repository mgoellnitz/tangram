<%@page isELIgnored="false" language="java" session="false"
%><%@page import="org.tangram.Constants,org.json.JSONObject"
%><% JSONObject json = (JSONObject)(request.getAttribute(Constants.THIS));
response.setContentType("application/json");
json.write(response.getWriter());
%>