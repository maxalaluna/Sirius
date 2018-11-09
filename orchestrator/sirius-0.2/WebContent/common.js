// Common functions

var NODE_NORMAL_SIZE = 25;
var NODE_SELECTED_SIZE = 32;
var LINK_NORMAL_COLOR = "gray";
var LINK_SELECTED_COLOR = "red";
var LINK_NORMAL_WIDTH = 1;
var LINK_SELECTED_WIDTH = 4;
	
// Terminal functions

function terminal_connect(net, id, term) {
	term.freeze(true);
	dialogActionOpen("Running connectivity test");
	var args = "net=" + net + "&id=" + id;
	$.get("ConnectServlet?" + args, function(data, status) {
		term.echo(data);
	})	
	.always(function() {
		$("#dialogAction").dialog("close");
		term.freeze(false);
	})
	.fail(function(xhr, status, error) {
		term.error(xhr.responseText);
	});
}

function terminal_exec(net, id, cmd, term) {
	term.freeze(true);
	dialogActionOpen("Running command on node");
	var args = "net=" + net + "&id=" + id + "&cmd=" + cmd;
	$.get("ExecuteServlet?" + args, function(data, status) {
		term.echo(data);
	})
	.always(function() {
		$("#dialogAction").dialog("close");
		term.freeze(false);
	})
	.fail(function(xhr, status, error) {
		term.error(xhr.responseText);
	});
}

function terminal_test(words, term) {
	var net = getTenantTab();
	if (words.length != 2) {
		term.error("test: Incorrect arguments");
		terminal_help(term);		
	}
	else if (net == 0) 
		term.error("You must open a tenant view");
	else {
		var id = getNodeId(net, words[0]);
		if (id == 0)
			term.error("Unfound node in tenant network " + net);
		else terminal_connect(net, id, term);
	}
}

function terminal_run(words, term) {
	var net = getTenantTab();
	if (words.length < 3) {
		term.error("run: Incorrect arguments");
		terminal_help(term);		
	}
	else if (net == 0)
		term.error("You must open a tenant view");
	else {
		var id = getNodeId(net, words[0]);
		if (id == 0)
			term.error("Unfound node in tenant network " + net);
		else {
			var cmd = words.splice(2).join(" ");
			terminal_exec(net, id, cmd, term);
		}
	}
}

function terminal_ping(words, term) {
	var net = getTenantTab();
	if (words.length != 3) {
		term.error("ping: Incorrect arguments");
		terminal_help(term);		
	}
	else if (net == 0)
		term.error("You must open a tenant view");
	else {
		var id = getNodeId(net, words[0]);
		if (id == 0)
			term.error("Unfound node in tenant network " + net);
		else {
			var cmd = "ping -c 1 " + words[2];
			terminal_exec(net, id, cmd, term);
		}
	}	
}

function terminal_help(term) {
	term.echo("[[b;black;white][host] test]: test host connectivity with peers");
	term.echo("[[b;black;white][host] run [cmd]]: run command in tenant host");
	term.echo("[[b;black;white][host] ping [ip]]: ping from host to host");
}

function terminal_handle(cmd, term) {
	var words = cmd.split(" ");
	if (words.length > 1) {
		switch (words[1]) {
		case "test":
			terminal_test(words, term);
			break;
		case "run":
			terminal_run(words, term);
			break;
		case "ping":
			terminal_ping(words, term);		
			break;
		default:
			term.error("Unknown command");
			terminal_help(term);		
		}
	}
	else {
		term.error("Unknown command");
		terminal_help(term);		
	}
}

// Network functions

function removeNetwork(net) {
	network[net].destroy();
	delete network[net];
	delete nodes[net];
	delete edges[net];
}

function loadNetwork(net) {
	$.get("LoadServlet?net=" + net, function(data, status) {
        addNetwork(net, data);
	});
}

function clearNetwork(net) {
	nodes[net].clear();
	edges[net].clear();
	clouds[net] = {};
	vms[net] = {};
}

function selectNetwork(net, id) {
	var args = "net=" + net + "&id=" + id;
	$.get("SelectNodeServlet?" + args, function(data, status) {
		callbackZoomHost();
		setNetwork(0, data);		
	})
}

// Tab functions

function removeAllTabs() {
	$('#tabs .ui-tabs-nav a').each(function(index){
	    var id = $(this).attr('href');
	    var net = id.substring(8);
	    removeNetwork(net);
	    $(id).remove();
	});
	$("#tabs").find("li").remove();
	$("#tabs").tabs("refresh");
}

function removeTab(k) {
	var tab = $("#network" + k);
	var index = $("#tabs .ui-tabs-panel").index(tab);
	$("#tabs").find("li:eq(" + index + ")").remove();
	$("#network" + k).remove();
	$("#tabs").tabs("refresh");
	removeNetwork(k);
}

function existTenantTab(k) {
	var tab = $("#network" + k);
	return $("#tabs .ui-tabs-panel").index(tab) != -1;
}

function setTenantTab(k) {
	var tab = $("#network" + k);
	var index = $("#tabs .ui-tabs-panel").index(tab);
	$("#tabs").tabs("option", "active", index);
}

function getTenantTab() {
	var id = $('#tabs').find(".ui-tabs-active").attr("aria-controls");
	if (id != undefined && id.substring(0, 7) == "network") 
		return id.substring(7);
	return 0;
}

// Node and link functions

function selectedNode(k) {
	var ids = network[k].getSelectedNodes();
	if (ids.length == 1) {
		if (!network[k].isCluster(ids[0]))
			return ids[0];
	}
	dialogAlert("Must select one host/switch");
	return 0;
}

function selectedLink(k) {
	var ids = network[k].getSelectedEdges();
	if (ids.length == 1) return ids[0];
	dialogAlert("Must select one link");
	return 0;
}

function getNodeId(k, name) {
	var tab = nodes[k].get({
		filter: function (item) {
			return (item.label == name);
		}
	});
	if (tab.length > 0)
		return tab[0].id;
	return 0;
}

// JSON data functions

function addNetwork(i, data) {
    for (var k in data.clouds) {
    	var cloud = data.clouds[k];
    	clouds[i][cloud.id] = cloud.name;
    }
    for (var k in data.vms) {
    	var vm = data.vms[k];
    	vms[i][vm.id] = { 
    		name: vm.name, 
    		state: vm.state
    	}
    }
    for (var k in data.nodes)
    	addNode(i, data.nodes[k]);
    for (var k in data.links)
    	addLink(i, data.links[k]);
    network[i].stabilize();
}

function setNetwork(i, data) {
    for (var k in data.clouds) {
    	var cloud = data.clouds[k];
    	clouds[i][cloud.id] = cloud.name;
    }
    for (var k in data.vms) {
    	var vm = data.vms[k];
    	vms[i][vm.id] = { 
    		name: vm.name, 
    		state: vm.state
    	}
    }
    for (var k in data.nodes)
    	setNode(i, data.nodes[k]);
    for (var k in data.links)
    	setLink(i, data.links[k]);
    network[i].stabilize();
}

function delNetwork(i, data) {
    for (var k in data.clouds) {
    	var cloud = data.clouds[k];
    	delete clouds[i][cloud.id];
    }
    for (var k in data.vms) {
    	var vm = data.vms[k];
    	delete vms[i][vm.id];
    }
    for (var k in data.nodes)
    	delNode(i, data.nodes[k]);
    for (var k in data.links)
    	delLink(i, data.links[k]);
    network[i].stabilize();	
}

function addNode(k, node) {
	if (node.selected == true)
		nodes[k].add({id:node.id, vid:node.vid, cid:node.cid, label:node.label, 
			title:node.title, image:node.image, shape:'image', size:NODE_SELECTED_SIZE});
	else
		nodes[k].add({id:node.id, vid:node.vid, cid:node.cid, label:node.label, 
			title:node.title, image:node.image, shape:'image', size:NODE_NORMAL_SIZE});
		
}

function setNode(k, node) {
	if (node.selected == true)
		nodes[k].update({id:node.id, vid:node.vid, cid:node.cid, label:node.label, 
			title:node.title, image:node.image, shape:'image', size:NODE_SELECTED_SIZE});
	else
		nodes[k].update({id:node.id, vid:node.vid, cid:node.cid, label:node.label, 
			title:node.title, image:node.image, shape:'image', size:NODE_NORMAL_SIZE});		
}

function delNode(k, node) {
	nodes[k].remove(node.id);
}

function addLink(k, link) {
	if (link.selected == true)
		edges[k].add({id:link.id, title:link.title, from:link.from, to:link.to, 
			color:LINK_SELECTED_COLOR, width:LINK_SELECTED_WIDTH, arrows:"from, to"});
	else
		edges[k].add({id:link.id, title:link.title, from:link.from, to:link.to, 
			color:LINK_NORMAL_COLOR, width:LINK_NORMAL_WIDTH, arrows:""});		
}

function setLink(k, link) {
	if (link.selected == true)
		edges[k].update({id:link.id, title:link.title, from:link.from, to:link.to, 
			color:LINK_SELECTED_COLOR, width:LINK_SELECTED_WIDTH, arrows:"from, to"});
	else
		edges[k].update({id:link.id, title:link.title, from:link.from, to:link.to, 
			color:LINK_NORMAL_COLOR, width:LINK_NORMAL_WIDTH, arrows:""});		
}

function delLink(k, link) {
	edges[k].remove(link.id);
}

window.onbeforeunload = function (event) {
    var message = "Please use logout to leave page";
    if (typeof event == 'undefined') {
        event = window.event;
    }
    if (event) {
        event.returnValue = message;
    }
    return message;
};

// Initialization function

$(function() {
	initJQuery();
	initNetwork(0);
	loadNetwork(0);
	initLoader("FCUL", 1, "#0066cc");
	initLoader("Amazon", 2, "#ffc300");
	//initLoader("IMT", 3, "#00b9dd");
	initLoader("Google", 3, "#d5473a");
	console.log("Network initialized");
});

