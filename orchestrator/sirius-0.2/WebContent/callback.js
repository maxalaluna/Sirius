
// Callback functions

function callbackSyncNetwork() {
	dialogConfirmCallback = dialogSyncNetwork;
	dialogConfirm("Run cloud synchronization?", 
			"The process may take a while");	
}

function callbackMapNetwork() {
	var net = getTenantTab();
	if (net > 0) {
		dialogConfirmCallback = dialogMappingNetwork;
		dialogConfirm("Map tenant" + net + "'s network?", 
				"The process may take a while");
	}
	else dialogAlert("You must open a tenant view");
}

function callbackReloadNetwork() {
	dialogConfirmCallback = dialogReloadNetwork;
	dialogConfirm("Reload networks from server?", 
			"All network contents will be lost");
}

function callbackSaveNetwork() {
	dialogConfirmCallback = dialogSaveNetwork;
	dialogConfirm("Save networks to server?", 
			"All network tabs will be saved");
}

function callbackDeleteHosts() {
	if ((id = selectedNode(activeNetwork)) != 0) {
		dialogConfirmCallback = dialogDeleteHosts;
		dialogConfirm("Delete hosts from VM?", 
				"All hosts will be removed");	
	}
}

function callbackLogout() {
	window.onbeforeunload = null;
}

function callbackZoomHost() {
	for (var k in clusters) {
		var name = clusters[k];
		if (network[0].isCluster(name)) {
			network[0].openCluster(name, {
				releaseFunction: function(clusterPos, nodePos) {
					return nodePos;
				}
			});
		}
	}
	network[0].stabilize();
	clusters = [];
}

function callbackZoomVm() {
	callbackZoomHost();
	var clusterOptions;
	for (var k in vms[0]) {
		var state = vms[0][k].state;
		var image = "img/vm" + state + ".png";		
    	clusterOptions = {
    		joinCondition: function(childOptions) {
    			return childOptions.vid == k;
    		},
    		clusterNodeProperties: {
    			allowSingleNodeCluster: true,
   				label: vms[0][k].name,
   				title: "VM" + k,
   				id: "vm" + k,
   				image: image,
   				shape: "image"
    		}
    	}
    	clusters.push("vm" + k);
        network[0].cluster(clusterOptions);
    	network[0].stabilize();
	}
}

function callbackZoomCloud() {
	callbackZoomHost();
	var clusterOptions;
	for (var k in clouds[0]) {
		var image = "img/cloud" + k + ".png";
    	clusterOptions = {
    		joinCondition: function(childOptions) {
    			return childOptions.cid == k;
    		},
    		clusterNodeProperties: {
    			allowSingleNodeCluster: true,
   				label: clouds[0][k],
   				title: "Cloud" + k,
   				id: "cloud" + k,
   				image: image,
   				shape: "image"
    		}
    	}
    	clusters.push("cloud" + k);
        network[0].cluster(clusterOptions);
    	network[0].stabilize();
	}
}

function callbackAddEdge(data, callback) {
	var args = "net=" + activeNetwork + "&from=" + data.from + "&to=" + data.to;
	$.get("AddLinkServlet?" + args, function(data, status) {
		selectNetwork(0, 0);
		addNetwork(activeNetwork, data); 
	})
	.fail(function(xhr, status, error) {
		dialogAlert(xhr.responseText);
	});
	callback(null);
}

function callbackAddNode(data, callback) {
	if ((id = selectedNode(activeNetwork)) != 0) {
		$("#dialogAddNode").dialog("open");
		$("#dialogHostSpinner").spinner("value", 1);
		$("#dialogSwitchSpinner").spinner("value", 0);		
	}
}

function callbackDeleteNode(data, callback) {
	if ((id = selectedNode(activeNetwork)) != 0) {
		var args = "net=" + activeNetwork + "&id=" + id;
		$.get("DeleteNodeServlet?" + args, function(data, status) {
			selectNetwork(0 ,0);
			delNetwork(activeNetwork, data);		
		})
		.fail(function(xhr, status, error) {
			dialogAlert(xhr.responseText);
		});
	}
	callback(null);
}

function callbackDeleteEdge(data, callback) {
	if ((id = selectedLink(activeNetwork)) != 0) {
		var args = "net=" + activeNetwork + "&id=" + id;
		$.get("DeleteLinkServlet?" + args, function(data, status) {
			selectNetwork(0 ,0);
			delNetwork(activeNetwork, data);		
		})
		.fail(function(xhr, status, error) {
			dialogAlert(xhr.responseText);
		});
	}
	callback(null);
}

function callbackInformation() {
	var ids1 = network[activeNetwork].getSelectedNodes();
	var ids2 = network[activeNetwork].getSelectedEdges();
	if (ids1.length == 1 && ids2.length == 0) {
		if (!network[activeNetwork].isCluster(ids1[0])) {
			id = ids1[0]; type = "node";
			var args = "net=" + activeNetwork + "&id=" + id;
			$.get("EditNodeServlet?" + args, function(data, status) {
		        dialogEditNode(data);        
			});
		}
	}
	else if (ids1.length == 0 && ids2.length == 1) {
		id = ids2[0]; type = "link";
		var args = "net=" + activeNetwork + "&id=" + id;
		$.get("EditLinkServlet?" + args, function(data, status) {
	        dialogEditLink(data);        
		});
	}
	else dialogAlert("Must select one host/switch");
}

function callbackAddTenant() {
	dialogPromptCallback = dialogAddTenant;
	dialogPrompt("Enter tenant id");
}

function callbackDeleteTenant() {
	var net = getTenantTab();
	if (net > 0) {
		dialogConfirmCallback = dialogDeleteTenant;
		dialogConfirm("Remove tenant network " + net + "?", 
				"Network's content will be lost");
	}
	else dialogAlert("You must open a tenant view");
}

function callbackSleepMode() {
	dialogConfirmCallback = dialogSleepMode;
	dialogConfirm("Stop all public VMs?", 
			"This action will put all VMs in sleep mode");
}

function callbackRunScript() {
	dialogConfirmCallback = dialogRunScript;
	dialogConfirm("Run Script?", 
			"Graphic representation will be disabled");
}

function callbackClick(net, nodes) {
	if (nodes.length == 1 && clusters.length == 0) 
		selectNetwork(net, nodes[0]);
}

function callbackConfigure() {
	$("#dialogAbout").dialog("open");
}
