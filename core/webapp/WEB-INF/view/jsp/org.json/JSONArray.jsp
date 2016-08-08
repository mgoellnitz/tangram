<%@page isELIgnored="false" language="java" session="false"
%><%@page import="org.json.JSONArray"
%><% JSONArray json = (JSONArray)(request.getAttribute(Constants.THIS));
response.setContentType("application/json");
json.write(response.getWriter());
%>