<%@page isELIgnored="false" language="java" session="false"
%><%@page import="org.tangram.Constants,com.google.gson.Gson,com.google.gson.GsonBuilder,com.google.gson.JsonElement"
%><% JsonElement json = (JsonElement)(request.getAttribute(Constants.THIS));
response.setContentType("application/json");
Gson g = new GsonBuilder().setPrettyPrinting().create();
g.toJson(json, response.getWriter());
%>