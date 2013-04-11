<%@page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Test</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="/css/style.css" type="text/css" />
<script type="text/javascript" src="/js/script.js">
</script>
</head>
<body>
<h1>${self.title}</h1>
<div>
x: <c:out value="${self.text}"/>
y: <c:out value="${self.image}"/>
</div>
</body>
</html>
