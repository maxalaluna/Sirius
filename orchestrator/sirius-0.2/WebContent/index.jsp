<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>Supercloud Console</title>

	<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
	<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
	
	<style type="text/css">
		html, body {
			margin: 0;
			padding: 0;
			background: 
		}
		label, input { 
			display: block;
		}
    	input.text { 
    		margin-bottom: 12px; 
    		width: 95%; 
    		padding: .4em; 
    	}
		form {
			font-family: Arial, Helvetica Neue, Helvetica, sans-serif; 
			font-size: 16px;
			position: absolute;
			margin: auto;
			top: 0;
			bottom: 0;
			right: 0;
			left: 0;
			width: 200px;
			height: 100px;
		}
	</style>
</head>
<body background="img/background.png">

	<form action="LoginServlet" method="post">
		<label for="username">Username</label>
		<input type="text" name="username" value="admin" class="text ui-widget-content ui-corner-all"/>
		<label for="password">Password</label>
		<input type="password" name="password" value ="supercloud" class="text ui-widget-content ui-corner-all"/><br><br>
		<button type="submit" class="ui-button ui-widget ui-corner-all">Login</button>
    </form>
</body>
</html>