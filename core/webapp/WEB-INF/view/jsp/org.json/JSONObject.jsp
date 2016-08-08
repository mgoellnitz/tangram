<%@page isELIgnored="false" language="java" session="false"
%><%@page import="org.json.JSONObject"
%><% JSONObject json = (JSONObject)(request.getAttribute(Constants.THIS));
response.setContentType("application/json");
json.write(response.getWriter());
%>