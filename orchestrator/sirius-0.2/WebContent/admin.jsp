<!doctype html>
<html>
<head>

	<meta charset="utf-8">
	<title>Supercloud Console</title>

	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
	<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
	<script src="jquery.terminal.min.js"></script>
	<script src="jquery.mousewheel.min.js"></script>
	<script src="vis.min.js" type="text/javascript"></script>
	<script src="circular.loader.js"></script>
	
	<link href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css" rel="stylesheet">
	<link href="jquery.terminal.min.css" rel="stylesheet"/>
	<link href="vis.min.css" rel="stylesheet" type="text/css"/>
	<link href="admin.css" rel="stylesheet" type="text/css"/>

	<script type="text/javascript" src="callback.js"></script>
	<script type="text/javascript" src="dialog.js"></script>
	<script type="text/javascript" src="common.js"></script> 
	<script type="text/javascript" src="initialize.js"></script> 

</head>
<body background="img/background.png">
	
	<div class="left">
		<div class="toolbar-left">
			<div class="toolbar">
				<button class="button" title="Save network to server" onclick="callbackSaveNetwork()">
					<img class="icon" src="img/save.png" alt="Save Network"/>
		  		</button>
				<button class="button" title="Reload network from server" onclick="callbackReloadNetwork()">
					<img class="icon" src="img/reload.png" alt="Reload Network"/>
				</button>
				<button class="button" title="Remove all hosts" onclick="callbackDeleteHosts()">
					<img class="icon" src="img/clear.png" alt="Remove Host"/>
				</button>
				<button class="button" title="Information on node/link" onclick="callbackInformation()">
					<img class="icon" src="img/info.png" alt="Get Info"/>
				</button>
				<button class="button" id="buttonSync" title="Synchronize with cloud" onclick="callbackSyncNetwork()">
					<img class="icon" src="img/sync.png" alt="Sync Network"/>
				</button>
				<span style="margin:0 10px"></span>
				<button class="button" title="Switch to container view" onclick="callbackZoomHost()">
					<img class="icon" src="img/host.png" alt="Host View"/>
				</button>
				<button class="button" title="Switch to VM view" onclick="callbackZoomVm()">
					<img class="icon" src="img/vm.png" alt="VM View"/>
				</button>
				<button class="button" title="Switch to cloud view" onclick="callbackZoomCloud()">
					<img class="icon" src="img/cloud.png" alt="Cloud View"/>
				</button>
			</div>
		</div>
		<div class="frame-left">
			<div class="physical">
				<div class="header">
					Substrate Infrastructure
					<span class="ui-icon ui-icon-newwin"></span>
				</div>
				<div class="footer" id="network0"></div>
			</div>
			<div class="rates">
				<div class="header">
					Cloud Occupation Rates
					<span class="ui-icon ui-icon-newwin"></span>
				</div>
				<div class="footer">
					<span class="loader1" id="loader1"></span>
					<span class="loader3" id="loader3"></span>
					<span class="loader2" id="loader2"></span>
					<span class="loader4" id="loader4"></span>
				</div>
			</div>
		</div>
	</div>
	<div class="right">
		<div class="toolbar-right">
			<div class="toolbar">
				<button class="button" title="Add tenant network" onclick="callbackAddTenant()">
					<img class="icon" src="img/tenant.png" alt="Add Tenant"/>
				</button>			
				<button class="button" id="buttonRunScript" title="Run script" onclick="callbackRunScript()">
					<img class="icon" src="img/script.png" alt="Run Script"/>
				</button>				
				<button class="button" title="Remove tenant network" onclick="callbackDeleteTenant()">
					<img class="icon" src="img/clear.png" alt="Remove Tenant"/>
				</button>
				<button class="button" title="Information on node/link" onclick="callbackInformation()">
					<img class="icon" src="img/info.png" alt="Get Info"/>
				</button>
				<button class="button" id="buttonMapping" title="Map tenant network" onclick="callbackMapNetwork()">
					<img class="icon" src="img/mapping.png" alt="Mapping"/>
				</button>
				<span style="margin:0 10px"></span>
		  		<button class="button" title="Configure options" onclick="callbackConfigure()">
					<img class="icon" src="img/configure.png" alt="Configure Options"/>
				</button>
		  		<button class="button" id="buttonSleep" title="VM sleep mode" onclick="callbackSleepMode()">
					<img class="icon" src="img/sleep.png" alt="Sleep Mode"/>
				</button>
				<form method="get" action="LogoutServlet" style="display:inline-block">
					<button class="button" title="Logout" type="submit" onclick="callbackLogout()">
						<img class="icon" src="img/logout.png" alt="Logout"/>
					</button>
				</form>							
			</div>
			<img class="logo" src="img/logo.png" alt="Logo"/>
		</div>			
		<div class="frame-right">
			<div class="top">
				<div class="frame-top">
					<div class="header">
						Tenant Networks
						<span class="ui-icon ui-icon-newwin"></span>
					</div>
					<div class="footer" id="tabs"><ul></ul></div>					
				</div>
			</div>
			<div class="bottom">
				<div class="frame-bottom">
					<div class="header">
						Host Terminal
						<span class="ui-icon ui-icon-newwin"></span>
					</div>
					<div class="footer" id="console"></div>
				</div>
			</div>
  		</div>
 	</div>
	
	<div id="dialogAddNode" title="Node Creation">
		<div class="widget">
		  	<fieldset>
		  		<legend>Number of Nodes</legend>
		  		<table>
			  		<tr>
						<td align="right">Host</td>
						<td><input type="text" id="dialogHostSpinner" value="2"/><br></td>
			  		</tr>
			  		<tr>
			  			<td align="right">Switch</td>
			  			<td><input type="text" id="dialogSwitchSpinner" value ="1"/></td>
			  		</tr>
		  		</table>	
		  	</fieldset>
	  	</div>
	</div>
	
	<div id="dialogEditNode" title="Node/Link Edition">
		<div class="scroll">
			<table class="table" id="dialogEditTable"></table>
		</div>
	</div>
	
	<div id="dialogAction" title="Action">
		<div>Please, wait for the process to complete ...</div>
		<div class="progressbar" id="dialogActionProgress"></div>
	</div>

	<div id="dialogAbout" title="Console Application">
		<div><h2>SUPERCLOUD Project</h2></div>
		<div>FCUL 2016-2017</div>
	</div>

	<div id="dialogPrompt" title="Enter Value">
		<input type="text" id="dialogPromptValue" value="1"/>
	</div>

	<div id="dialogConfirm" title="Confirm action"></div>

	<div id="dialogAlert" title="Warning"></div>	
		
</body>
</html>