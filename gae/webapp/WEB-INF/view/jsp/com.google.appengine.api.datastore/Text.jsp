<%@page isELIgnored="false" language="java" session="false"
	pageEncoding="UTF-8" import="org.tangram.Constants,com.google.appengine.api.datastore.Text" 
%><%@taglib	prefix="cms" uri="http://www.top-tangram.org/tags"
%><%
    Text t = (Text)request.getAttribute(Constants.THIS);
	request.setAttribute("dummy", t.getValue().toCharArray());
%><cms:include bean="${dummy}"/>