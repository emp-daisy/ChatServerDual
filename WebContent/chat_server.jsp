<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>GCM Server JSP</title>
</head>
<body>
<form action="GCMServer">
	Enter message:  
	<input type="text" name="GCM_msg"><br>
	Enter reciepent's number:  
	<input type="text" name="GCM_contactId"><br>
	<input type="hidden" name="Type" value="msg"><br>
	<input type="submit" value="submit">	
</form>
</body>
</html>