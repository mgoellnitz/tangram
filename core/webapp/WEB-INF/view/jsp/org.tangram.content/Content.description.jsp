<%@page isELIgnored="false" language="java" session="false"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" 
%><%@page import="org.tangram.Constants,org.tangram.components.editor.EditingHandler,org.tangram.content.Content"
%><%=EditingHandler.getDesignClass(((Class<? extends Content>)request.getAttribute(Constants.THIS).getClass())).getName()%>