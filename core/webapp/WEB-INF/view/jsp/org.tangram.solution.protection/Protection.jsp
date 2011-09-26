<%@page isELIgnored="false" language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@taglib
	prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@taglib
	prefix="cms" uri="http://www.top-tangram.org/tags"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Tangram - <cms:include bean="${self}" view="description"/>: ${self.class.simpleName}</title>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="/editor/screen.css" type="text/css" media="screen"/>
<link rel="stylesheet" href="/editor/print.css" type="text/css" media="print"/>
<link rel="shortcut icon" href="/t/e/favicon.ico" />
</head>
<body>

<div>
<cms:include bean="${self}" view="login"/>
</div>

</body>
</html>
