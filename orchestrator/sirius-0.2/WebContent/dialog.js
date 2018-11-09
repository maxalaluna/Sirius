// Dialog functions

function dialogSyncNetwork() {
	$("#buttonSync").attr("disabled", true);
	dialogActionOpen("Running synchronization process");
	dialogActionUpdate("Check for topology changes...", false);
	dialogSyncRec(true);
}

function dialogSyncRec(init) {
	var args = "init=" + init;
	$.get("SyncServlet?" + args, function(data, status) {
		if (data.index < data.length) {
			var rate = ((data.index + 1) * 100) / data.length;
			dialogActionUpdate(data.message, rate);
			dialogSyncRec(false);			
		}
		else {
			$("#buttonSync").attr("disabled", false);
			$("#dialogAction").dialog("close");
			selectNetwork(0, 0);
			setNetwork(0, data.data);
		}
	})
	.fail(function(xhr, status, error) {
		$("#buttonSync").attr("disabled", false);
		$("#dialogAction").dialog("close");
		dialogAlert(xhr.responseText);
	});	
}

function dialogMappingNetwork() {
	var net = getTenantTab();
	$("#buttonMapping").attr("disabled", true);
	dialogActionOpen("Running mapping process");
	dialogActionUpdate("Connecting Hypervisor...", false);
	selectNetwork(0, 0);
	var args = "net=" + getTenantTab();
	$.get("MappingServlet?" + args, function(data, status) {
		$("#buttonMapping").attr("disabled", false);
		$("#dialogAction").dialog("close");
		addNetwork(0, data);
		selectNetwork(net, 0);
	})
	.fail(function(xhr, status, error) {
		$("#dialogAction").dialog("close");
		$("#buttonMapping").attr("disabled", false);
		dialogAlert(xhr.responseText);
	});
}

function dialogReloadNetwork() {
	$.get("ReloadServlet", function(data, status) {
        console.log(data);
		callbackZoomHost();
		clearNetwork(0);
		loadNetwork(0);
		removeAllTabs();
	})
	.fail(function(xhr, status, error) {
		dialogAlert(xhr.responseText);
	});
}

function dialogDeleteHosts() {
	var args = "net=" + activeNetwork + "&id=" + id;
	$.get("DeleteHostsServlet?" + args, function(data, status) {
		selectNetwork(0 ,0);
		delNetwork(activeNetwork, data);		
	})
	.fail(function(xhr, status, error) {
		dialogAlert(xhr.responseText);
	});
}

function dialogSaveNetwork() {
	$.get("SaveServlet", function(data, status) {
		console.log(data);
	})
	.fail(function(xhr, status, error) {
		dialogAlert(xhr.responseText);
	});
}

function dialogAddSection(table, name) {
	table.append("<tr><td colspan='2' " +
			"bgcolor='#f2f2f2'>" + name + "</td></tr>");
}

function dialogAddRow(table, data) {
	if (Array.isArray(data[2])) {
		table.append("<tr><td>" + data[0] + "</td><td contenteditable>" 
				+ "<select></select></td></tr>");
		var combo = table.find("select").last();
		for (var k = 0; k < data[2].length; k++)
			if (k == parseInt(data[1]))
				combo.append("<option selected>" + data[2][k] + "</option>");
			else combo.append("<option>" + data[2][k] + "</option>");
	}	
	else if (data[2] == true) 
		table.append("<tr><td>" + data[0] + 
				"</td><td contenteditable>" + data[1] + "</td></tr>");
	else
		table.append("<tr><td>" + data[0] + 
				"</td><td>" + data[1] + "</td></tr>");		
}

function dialogPopulateTable(table, data) {
	for (var k = 0; k < data.length; k++) {
		dialogAddRow(table, data[k]);
		dialogTableCells.push(data[k]);
	}
}

function dialogAddNode() {
	var nb1 = $("#dialogHostSpinner").spinner("value");
	var nb2 = $("#dialogSwitchSpinner").spinner("value");
	var args = "net=" + activeNetwork + "&id=" + id + "&nb1=" + nb1 + "&nb2=" + nb2;
	$.get("AddNodeServlet?" + args, function(data, status) {
		selectNetwork(0, 0);
		addNetwork(activeNetwork, data);
	})
	.fail(function(xhr, status, error) {
		dialogAlert(xhr.responseText);
	});
}

function dialogEditNode(data) {
	var table = $("#dialogEditTable");
	dialogTableCells = [];
	table.find("tr").remove();
	dialogAddSection(table, "Node");
	dialogPopulateTable(table, data[0]);
	dialogAddSection(table, "Virtual Machine");
	dialogPopulateTable(table, data[1]);
	dialogAddSection(table, "Cloud");
	dialogPopulateTable(table, data[2]);
	$("#dialogEditNode").dialog("open");
}

function dialogEditLink(data) {
	var table = $("#dialogEditTable");
	dialogTableCells = [];
	table.find("tr").remove();
	dialogAddSection(table, "Link");
	dialogPopulateTable(table, data[0]);
	$("#dialogEditNode").dialog("open");
}

function dialogEditUpdate(name, value) {
	var args = "net=" + activeNetwork + "&type=" + type 
		+ "&id=" + id + "&name=" + name + "&value=" + value;
	$.get("UpdateNodeServlet?" + args, function(data, status) {
		callbackZoomHost();
		setNetwork(activeNetwork, data);    		
	})
	.fail(function(xhr, status, error) {
		dialogAlert(xhr.responseText);
	});	
}

function dialogEditSave() {
	var table = $("#dialogEditTable");
	var contents = table.find("td[contenteditable]");
	var index = 0;
	if (activeNetwork == 0) 
		callbackZoomHost();
	for (var k = 0; k < dialogTableCells.length; k++) {
		if (Array.isArray(dialogTableCells[k][2])) {
			var value = contents[index++].firstChild.selectedIndex;
			if (dialogTableCells[k][1] != value) {
				var name = dialogTableCells[k][0];
				dialogEditUpdate(name, value);
			}
		}
		else if (dialogTableCells[k][2] == true) {
			var value = contents[index++].innerHTML;
			if (String(dialogTableCells[k][1]) != value) {
				var name = dialogTableCells[k][0];
				dialogEditUpdate(name, value);
			}
		}
	}
}

function dialogActionOpen(title) {
	$("#dialogAction").dialog("option", "title", title);
	dialogActionUpdate("Please, wait for the process to complete ...", false);
	$("#dialogAction").dialog("open");
}

function dialogActionUpdate(label, progress) {
	$("#dialogAction").find("div:first").html(label);
	$("#dialogActionProgress").progressbar("value", progress);
}

function dialogActionStop() {
	var net = getTenantTab();
	var args = "net=" + net + "&id=0&cmd=stop";
	$("#dialogAction").dialog("close");
	$.get("ExecuteServlet?" + args);
}

function dialogPrompt(title) {
	$("#dialogPrompt").dialog("option", "title", title);
	$("#dialogPrompt").dialog("open");
}

function dialogConfirm(title, data) {
	data = data.replace(/\n/g, "<br>");
	$("#dialogConfirm").find("div").remove();
	$("#dialogConfirm").append("<div>" + data + "</div>");
	$("#dialogConfirm").dialog("option", "title", title);
	$("#dialogConfirm").dialog("open");
}

function dialogAlert(data) {
	data = data.replace(/\n/g, "<br>");
	$("#dialogAlert").find("div").remove();
	$("#dialogAlert").append("<div>" + data + "</div>");
	$("#dialogAlert").dialog("open");
}

function dialogAddTenant(tenant) {
	if (tenant <= 0) 
		dialogAlert("Tenant id must be greater than zero");
	else if (!existTenantTab(tenant)) {
		$("#tabs").find(".ui-tabs-nav").append('<li><a href="#network' + tenant + '">Tenant' 
				+ tenant + '</a><span class="ui-icon ui-icon-close"></span></li>');
		$("#tabs").append('<div id="network' + tenant + '" class="tenant"></div>');
		$("#tabs").tabs("refresh");
		
		// Load tenant network
		selectNetwork(0, 0);
		setTenantTab(tenant);
		initNetwork(tenant);
		loadNetwork(tenant);
	}
}

function dialogDeleteTenant() {
	var net = getTenantTab();
	var args = "net=" + net;
	$.get("DeleteTenantServlet?" + args, function(data, status) {
		console.log("Delete tenant network " + net);
		selectNetwork(0, 0);
		removeTab(net);	
	})
	.fail(function(xhr, status, error) {
		dialogAlert(xhr.responseText);
	});
}

function dialogSleepMode() {
	$("#buttonSleep").attr("disabled", true);
	dialogActionOpen("Stopping public VMs");
	dialogActionUpdate("Check for running VMs...", false);
	dialogSleepRec(true);
}

function dialogSleepRec(init) {
	var args = "init=" + init;
	$.get("StopAllServlet?" + args, function(data, status) {
		if (data.index < data.length) {
			var rate = ((data.index + 1) * 100) / data.length;
			dialogActionUpdate(data.message, rate);
			dialogSleepRec(false);			
		}
		else {
			$("#buttonSleep").attr("disabled", false);
			$("#dialogAction").dialog("close");
		}
	})
	.fail(function(xhr, status, error) {
		$("#buttonSleep").attr("disabled", false);
		$("#dialogAction").dialog("close");
		dialogAlert(xhr.responseText);
	});	
}

function dialogRunScript() {
	$("#buttonRunScript").attr("disabled", true);
	dialogActionOpen("Running script");
	dialogActionUpdate("Starting actions...", false);
	dialogRunScriptRec(true);
}

function dialogRunScriptRec(init) {
	var args = "init=" + init;
	$.get("RunScriptServlet?" + args, function(data, status) {
		if (data.index < data.length) {
			var rate = ((data.index + 1) * 100) / data.length;
			dialogActionUpdate(data.message, rate);
			dialogRunScriptRec(false);			
		}
		else {
			$("#buttonRunScript").attr("disabled", false);
			$("#dialogAction").dialog("close");
		}
	})
	.fail(function(xhr, status, error) {
		$("#buttonRunScript").attr("disabled", false);
		$("#dialogAction").dialog("close");
		dialogAlert(xhr.responseText);
	});	
}
