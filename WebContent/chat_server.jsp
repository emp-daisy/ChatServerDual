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
	<input type="text" name="msg"><br>
	Enter reciepent's name:  
	<input type="text" name="contact"><br>
	Enter receipent's nunmber: 
	<input type="text" name="MobileNumberTo"><br>
	<input type="hidden" name="Message" value="Yes"><br>
	<input type="submit" value="submit">	
</form>
</body>
</html>