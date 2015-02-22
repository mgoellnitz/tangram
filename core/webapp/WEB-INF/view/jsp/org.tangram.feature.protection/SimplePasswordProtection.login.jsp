<%@page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="cms" uri="http://www.top-tangram.org/tags" 
%><%@page import="org.tangram.feature.protection.SimplePasswordProtection"
%><%@page import="org.tangram.Constants"
%><form action="" method="post">
<input type="hidden" name="<%=Constants.PARAMETER_PROTECTION_KEY%>" value="${self.protectionKey}"/>
<div style="padding: 10px;">
<c:if test="${! empty loginResult}">
<div style="padding: 10px; color: red;">
<c:out value="${loginResult}"/>
</div>
</c:if>
<div style="padding: 10px;">
<label >Login: </label><input type="text" name="<%=SimplePasswordProtection.PARAM_LOGIN %>"/>
</div>
<div style="padding: 10px;">
<label>Password: </label><input type="password" name="<%=SimplePasswordProtection.PARAM_PASSWORD%>" />
</div>
<div style="padding: 10px;">
<input type="submit"  name="<%=Constants.PARAMETER_PROTECTION_LOGIN%>" value="  Login  " />
</div>
</div>
</form>