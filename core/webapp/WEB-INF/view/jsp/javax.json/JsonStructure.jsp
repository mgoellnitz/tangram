<%@page isELIgnored="false" language="java" session="false"
%><%@page import="org.tangram.Constants,javax.json.Json,javax.json.JsonStructure"
%><% JsonStructure json = (JsonStructure)(request.getAttribute(Constants.THIS));
response.setContentType("application/json");
Json.createWriter(response.getWriter()).write(json);
%>