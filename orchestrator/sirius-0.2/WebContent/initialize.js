var vms = {};
var clouds = {};
var clusters = [];
var network = {};
var nodes = {};
var edges = {};

function initNetwork(k) {
	nodes[k] = new vis.DataSet([]);
	edges[k] = new vis.DataSet([]);
	clouds[k] = {};
	vms[k] = {};
  
	// Create static network
	var container = document.getElementById('network' + k);
	var data = {
		nodes: nodes[k],
		edges: edges[k]
	};
	var locales = {
		en: {
		    edit: 'Edit',
		    del: 'Delete selected',
		    back: 'Back',
		    addNode: 'Add Node',
		    addEdge: 'Add Edge',
		    editNode: 'Edit Node',
		    editEdge: 'Edit Edge',
		    addDescription: 'Select a source host or switch.',
		    edgeDescription: 'Click on a node and drag the edge to another node to connect them.',
		    editEdgeDescription: 'Click on the control points and drag them to a node to connect to it.',
		    createEdgeError: 'Cannot link edges to a cluster.',
		    deleteClusterError: 'Clusters cannot be deleted.',
		    editClusterError: 'Clusters cannot be edited.'
		}
	}
	var options = {
    	locale: 'en',
		locales: locales,
		interaction: {
			multiselect: false,
			selectConnectedEdges: false,
			navigationButtons: true
		},
		nodes: {
			shadow: true,
		},
		edges: {
			smooth: true,
			shadow: true
		},
		physics: {
			enabled: true
		},
		manipulation: {
			addNode: function(data, callback) {
				callbackAddNode(data, callback);
			},
			addEdge: function(data, callback) {
				callbackAddEdge(data, callback);
			},
			deleteNode: function(data, callback) {
				callbackDeleteNode(data, callback);
			},			
			deleteEdge: function(data, callback) {
				callbackDeleteEdge(data, callback);
			}
		}
	}
	
	// Create network
	network[k] = new vis.Network(container, data, options);
	
	// Focus change
	activeNetwork = k;
	network[k].on("release", function(data) {
		activeNetwork = k;
	}.bind(k));
	
	// Double click 
	network[k].on("click", function(params) {
		callbackClick(k, params.nodes);
	}.bind(k));
}

function initLoader(name, id, color) {
    $("#loader" + id).circularloader({
        progressPercent: 0,
        backgroundColor: "#ffffff",
        fontColor: "#000000",
        fontSize: "10px",
        radius: 20,
        progressBarBackground: "#cdcdcd",
        progressBarColor: color,
        progressBarWidth: 10,
        speed: 10,
        progressvalue: 0,
        showText: true,
        title: name
    });
}

function initJQuery() {
	$("#dialogHostSpinner").spinner();
	$("#dialogSwitchSpinner").spinner();
	$("#dialogPromptValue").spinner();
	$("#dialogActionProgress").progressbar();
	
	// Node addition dialog
	$("#dialogAddNode").dialog({
		autoOpen: false,
		width: 500,
	    buttons: {
	    	"Create": function() {
	    		dialogAddNode();
	    		$("#dialogAddNode").dialog("close");
	    	},
	        Cancel: function() {
	        	$("#dialogAddNode").dialog("close");
	        }
	    }
	});	
	
	// Node edition dialog
	$("#dialogEditNode").dialog({
		autoOpen: false,
		width: 500,
	    buttons: {
	        Save: function() {
	        	dialogEditSave();
	        	$("#dialogEditNode").dialog("close");
	        },
	    	Close: function() {
	        	$("#dialogEditNode").dialog("close");
	        }
	    }
	});
	
	// Action dialog
	$("#dialogAction").dialog({
		autoOpen: false,
		width: 500,
	    buttons: {
	        Cancel: function() {
	        	dialogActionStop();
	        }
	    }
	});
	
	// About dialog
	$("#dialogAbout").dialog({
		autoOpen: false,
		width: 500,
	    buttons: {
	        Ok: function() {
	        	$("#dialogAbout").dialog("close");
	        }
	    }
	});
	
	// Prompt dialog
	$("#dialogPrompt").dialog({
		autoOpen: false,
		width: 500,
		buttons: {
			Ok: function() {
				dialogPromptCallback($("#dialogPromptValue").spinner("value"));
				$("#dialogPrompt").dialog("close");
			},
			Cancel: function() {
				$("#dialogPrompt").dialog("close");
			}
		},
	    close: function() {
	    	$("#dialogPrompt").dialog("close");
		}
	});
	
	// Confirm dialog
	$("#dialogConfirm").dialog({
		autoOpen: false,
		width: 500,
		buttons: {
			Ok: function() {
				dialogConfirmCallback();
				$("#dialogConfirm").dialog("close");
			},
			Cancel: function() {
				$("#dialogConfirm").dialog("close");
			}
		},
	    close: function() {
	    	$("#dialogConfirm").dialog("close");
		}
	});
	
	// Warning dialog
	$("#dialogAlert").dialog({
		autoOpen: false,
		width: 500,
	    buttons: {
	        Ok: function() {
	        	$("#dialogAlert").dialog("close");
	        }
	    }
	})
	.parent().addClass("ui-state-error");
	
	// Tabs
	$("#tabs").tabs();	
	$("#tabs").on("click", "span.ui-icon-close", function() {
		var panelId = $(this).closest("li").remove().attr("aria-controls");
	    $( "#" + panelId ).remove();
	    $("#tabs").tabs("refresh");
	    var id = panelId.substring(7);
	    removeNetwork(id);
	});
	
	// Terminal
    $("#console").terminal(function(cmd, term) {
        if (cmd !== "")
           	terminal_handle(cmd, term);
        else term.echo("");
    }, {
        greetings: "[[b;black;white]Supercloud Console]",
        name: "supercloud_console",
        prompt: "> "
    });
}