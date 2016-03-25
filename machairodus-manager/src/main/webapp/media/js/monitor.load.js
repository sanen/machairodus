var Monitor = Monitor || {};
Monitor.Load = function() {
	var times = {};
	var diffTime = 0;
	var queues = {};
	var interval;
	var maxTime = 600000;
	var defaultQueueSize = 650;
	
	this.resizePanel = function() {
		setTimeout(function() {
			var centerWidth = $("#center").width();
			var centerHeight = $("#center").height();
			$("#area1").width(centerWidth - 5);
			$("#area1").height((centerHeight / 2) - 7.5);
			$("#area2").width(centerWidth - 5);
			$("#area2").height((centerHeight / 2) - 7.5);
			
			if($("#chkCpu")[0].checked && $("#chkMemory")[0].checked && ($("#chkClasses")[0].checked || $("#chkThreads")[0].checked)) {
				$("#pnlCpu").panel({ width: (centerWidth/ 2) - 7.5, height: (centerHeight / 2) - 7.5 });
				$("#pnlMemory").panel({ width: (centerWidth/ 2) - 7.5, height: (centerHeight / 2) - 7.5 });
				$('#area2').css({display: 'block'});
			} else if($("#chkCpu")[0].checked && $("#chkMemory")[0].checked && !$("#chkClasses")[0].checked && !$("#chkThreads")[0].checked) {
				$("#pnlCpu").panel({ width: (centerWidth/ 2) - 7.5, height: centerHeight - 7.5 });
				$("#pnlMemory").panel({ width: (centerWidth/ 2) - 7.5, height: centerHeight - 7.5 });
				$('#area2').css({display: 'none'});
			} else if(!$("#chkCpu")[0].checked && $("#chkMemory")[0].checked && ($("#chkClasses")[0].checked || $("#chkThreads")[0].checked)) {
				$("#pnlMemory").panel({ width: centerWidth - 7.5, height: (centerHeight / 2) - 7.5 });
				$('#area2').css({display: 'block'});
			} else if($("#chkCpu")[0].checked && !$("#chkMemory")[0].checked && ($("#chkClasses")[0].checked || $("#chkThreads")[0].checked)) {
				$("#pnlCpu").panel({ width: centerWidth - 7.5, height: (centerHeight / 2) - 7.5 });
				$('#area2').css({display: 'block'});
			} else if(!$("#chkCpu")[0].checked && $("#chkMemory")[0].checked && !$("#chkClasses")[0].checked && !$("#chkThreads")[0].checked) {
				$("#pnlMemory").panel({ width: centerWidth - 7.5, height: centerHeight - 7.5 });
				$('#area2').css({display: 'none'});
			} else if($("#chkCpu")[0].checked && !$("#chkMemory")[0].checked && !$("#chkClasses")[0].checked && !$("#chkThreads")[0].checked) {
				$("#pnlCpu").panel({ width: centerWidth - 7.5, height: centerHeight - 7.5 });
				$('#area2').css({display: 'none'});
			}
			
			if($("#chkClasses")[0].checked && $("#chkThreads")[0].checked && ($("#chkCpu")[0].checked || $("#chkMemory")[0].checked)) {
				$("#pnlClasses").panel({ width: (centerWidth/ 2) - 7.5, height: (centerHeight / 2) - 7.5 });
				$("#pnlThreads").panel({ width: (centerWidth/ 2) - 7.5, height: (centerHeight / 2) - 7.5 });
				$('#area1').css({display: 'block'});
			} else if($("#chkClasses")[0].checked && $("#chkThreads")[0].checked && !$("#chkCpu")[0].checked && !$("#chkMemory")[0].checked) {
				$("#pnlClasses").panel({ width: (centerWidth/ 2) - 7.5, height: centerHeight - 7.5 });
				$("#pnlThreads").panel({ width: (centerWidth/ 2) - 7.5, height: centerHeight - 7.5 });
				$('#area1').css({display: 'none'});
			} else if(!$("#chkClasses")[0].checked && $("#chkThreads")[0].checked && ($("#chkCpu")[0].checked || $("#chkMemory")[0].checked)) {
				$("#pnlThreads").panel({ width: centerWidth - 7.5, height: (centerHeight / 2) - 7.5 });
				$('#area1').css({display: 'block'});
			} else if($("#chkClasses")[0].checked && !$("#chkThreads")[0].checked && ($("#chkCpu")[0].checked || $("#chkMemory")[0].checked)) {
				$("#pnlClasses").panel({ width: centerWidth - 7.5, height: (centerHeight / 2) - 7.5 });
				$('#area1').css({display: 'block'});
			} else if(!$("#chkClasses")[0].checked && $("#chkThreads")[0].checked && !$("#chkCpu")[0].checked && !$("#chkMemory")[0].checked) {
				$("#pnlThreads").panel({ width: centerWidth - 7.5, height: centerHeight - 7.5 });
				$('#area1').css({display: 'none'});
			} else if($("#chkClasses")[0].checked && !$("#chkThreads")[0].checked && !$("#chkCpu")[0].checked && !$("#chkMemory")[0].checked) {
				$("#pnlClasses").panel({ width: centerWidth - 7.5, height: centerHeight - 7.5 });
				$('#area1').css({display: 'none'});
			}
			
			$('#viewCpu').css({ width: $("#pnlCpu").width() - 20, height: $("#pnlCpu").height() - 20, left: 10, top: 10 });
			$('#viewMemory').css({ width: $("#pnlMemory").width() - 20, height: $("#pnlMemory").height() - 20, left: 10, top: 10 });
			$('#viewClasses').css({ width: $("#pnlClasses").width() - 20, height: $("#pnlClasses").height() - 20, left: 10, top: 10 });
			$('#viewThreads').css({ width: $("#pnlThreads").width() - 20, height: $("#pnlThreads").height() - 20, left: 10, top: 10 });
			
			drawGraph($('#viewCpu')[0], null, null);
			drawGraph($('#viewMemory')[0], null, null);
			drawGraph($('#viewClasses')[0], null, null);
			drawGraph($('#viewThreads')[0], null, null);
			
		}, 10);
	}
	
	this.panelToggle = function(thiz, id) {
		var checked;
		if(checked = $(thiz)[0].checked) {
			$(id).panel('open');
		} else {
			$(id).panel('close');
		}
		
		resizePanel();
		/** 重复一次Resize，可以解决一次Resize无法正确布局的问题 */
		resizePanel();
	}
	
	this.initBasicTime = function(title, container, size) {
		try {
			var options, graph, i = 0, x, o;
			switch(container.id) {
				case 'viewCpu': 
					queues['viewCpu'] = new Queue(size || defaultQueueSize);
					break;
				case 'viewMemory':
					queues['viewMemory_COMMITED'] = new Queue(size || defaultQueueSize);
					queues['viewMemory_USED'] = new Queue(size || defaultQueueSize);
					break;
				case 'viewThreads': 
					queues['viewThreads_THREAD_COUNT'] = new Queue(size || defaultQueueSize);
					queues['viewThreads_DAEMON_THREAD_COUNT'] = new Queue(size || defaultQueueSize);
					break;
				case 'viewClasses': 
					queues['viewClasses_LOADED'] = new Queue(size || defaultQueueSize);
					queues['viewClasses_UNLOADED'] = new Queue(size || defaultQueueSize);
					break;
				default: 
					throw 'unkown container id'
			}
			
		 	drawGraph(container, null, null);
		} catch(error) { }
	}

	this.drawGraph = function (container, opts, data) {
		try {
			var now = times[container.id];
			var max = 0;
			if(data) {
				$.each(data, function(idx, item) {
					$.each(item.data, function(_idx, value) {
						if(value[1] && max < value[1])
							max = value[1];
					});
				})
			}
			
			max += max * 0.3;
			var options = {
			    xaxis : { min: now - diffTime, max: now, mode : 'time', timeMode: 'local', labelsAngle : 0 },
			    selection : { mode : 'x' },
			    HtmlText : false,
			    resolution: 2
			};
			
			var o = Flotr._.extend(Flotr._.clone(options), opts || {});
			o.yaxis = { min: 0, max: max }
			Flotr.draw(container, data || [], o);
		} catch(error) {
			console.log(error);
		}
	}
	
	this.setData = function(data) {
		if(!data.MEMORY)
			data.MEMORY = {};
		
		if(!data.THREAD)
			data.THREAD = {};
		
		if(!data.CLASSLOAD)
			data.CLASSLOAD = {};
		
		if(data.TIME) {
			queues['viewCpu'].offer([data.TIME, data.CPU]);
			queues['viewMemory_COMMITED'].offer([data.TIME, data.MEMORY.COMMITED]);
			queues['viewMemory_USED'].offer([data.TIME, data.MEMORY.USED]);
			queues['viewClasses_LOADED'].offer([data.TIME, data.CLASSLOAD.LOADED]);
			queues['viewClasses_UNLOADED'].offer([data.TIME, data.CLASSLOAD.UNLOADED]);
			queues['viewThreads_THREAD_COUNT'].offer([data.TIME, data.THREAD.THREAD_COUNT]);
			queues['viewThreads_DAEMON_THREAD_COUNT'].offer([data.TIME, data.THREAD.DAEMON_THREAD_COUNT]);
		} else 
			return ;
		
		var diff = data.TIME - times['viewCpu'];
		if(!isNaN(diff) && diffTime < maxTime) {
			diffTime += diff;
			if(diffTime > maxTime)
				diffTime = maxTime;
		}
		
		times['viewCpu'] = data.TIME;
		times['viewMemory'] = data.TIME;
		times['viewClasses'] = data.TIME;
		times['viewThreads'] = data.TIME;
		
		var options = {};
		options.time = data.TIME
		
		if($('#viewCpu')[0].clientWidth != 0 && $('#viewCpu')[0].clientHeight != 0) {
			var _data = [];
			_data.push({ data: queues['viewCpu'].get(), label: 'CPU ( '+queues['viewCpu'].last()[1]+'% )' });
			drawGraph($('#viewCpu')[0], options, _data);
			options.yaxis = {};
		}
		
		if($('#viewMemory')[0].clientWidth != 0 && $('#viewMemory')[0].clientHeight != 0) {
			var _data = [];
			_data.push({ data: queues['viewMemory_COMMITED'].get(), label: '可用内存 ( '+queues['viewMemory_COMMITED'].last()[1]+'MB )', lines: { fill: true} });
			_data.push({ data: queues['viewMemory_USED'].get(), label: '已用内存 ( '+queues['viewMemory_USED'].last()[1]+'MB )', lines: { fill: true} });
			drawGraph($('#viewMemory')[0], options, _data);
			options.yaxis = {};
		}
		
		if($('#viewClasses')[0].clientWidth != 0 && $('#viewClasses')[0].clientHeight != 0) {
			var _data = [];
			_data.push({ data: queues['viewClasses_LOADED'].get(), label: '已加载类 ( '+queues['viewClasses_LOADED'].last()[1]+' )' });
			_data.push({ data: queues['viewClasses_UNLOADED'].get(), label: '未加载类 ( '+queues['viewClasses_UNLOADED'].last()[1]+' )' });
			drawGraph($('#viewClasses')[0], options, _data);
			options.yaxis = {};
		}
		
		if($('#viewThreads')[0].clientWidth != 0 && $('#viewThreads')[0].clientHeight != 0) {
			var _data = [];
			_data.push({ data: queues['viewThreads_THREAD_COUNT'].get(), label: '线程数 ( '+queues['viewThreads_THREAD_COUNT'].last()[1]+' )' });
			_data.push({ data: queues['viewThreads_DAEMON_THREAD_COUNT'].get(), label: '守护线程数 ( '+queues['viewThreads_DAEMON_THREAD_COUNT'].last()[1]+' )' });
			drawGraph($('#viewThreads')[0], options, _data);
			options.yaxis = {};
		}
		
	}
	
	this.initSelect2 = function() {
		var id;
		if (id = $.cookie("machairodus.monitor.load")) {
			$('#node').select2({
				placeholder: "选择节点",
				ajax: {
				    url: context + '/configure/node/find/simple',
				    dataType: 'json',
				    delay: 300,
				    data: function (params) {
				      return {
				    	'type[]': [1, 2, 3], 
				        param: params.term,
				        offset: (params.page || 0) * 10,
				        limit: 10
				      };
				    },
				    processResults: function (data, params) {
				      params.page = params.page || 1;
				 
				      return {
				        results: data.rows,
				        pagination: {
				          more: (params.page * 10) < data.total
				        }
				      };
				    },
				    cache: true
				  },
				  initSelection: function(element, callback) {  
					  if (id !== "") {
						  $.ajax(context + '/configure/node/find/id', {
							  data : { id : id },
							  dataType : "json"
						  }).done(function(data) {
							  callback(data.rows);
							  reloadMonitor(data.rows[0].id, data.rows[0]);
						  });
					  }  
			      }, 
				  escapeMarkup: function (markup) { return markup; }, 
				  templateResult: formatRepo, 
				  templateSelection: formatRepoSelection
			});
		} else {
			$("#node").select2({
				placeholder: "选择节点",
				ajax: {
					url: context + '/configure/node/find/simple',
					dataType: 'json',
				    delay: 300,
				    data: function (params) {
				      return {
				    	'type[]': [1, 2, 3], 
				        param: params.term,
				        offset: (params.page || 0) * 10,
				        limit: 10
				      };
				    },
				    processResults: function (data, params) {
				      params.page = params.page || 1;
				 
				      return {
				        results: data.rows,
				        pagination: {
				          more: (params.page * 10) < data.total
				        }
				      };
				    },
				    cache: true
				},
				escapeMarkup: function (markup) { return markup; }, 
				templateResult: formatRepo, 
				templateSelection: formatRepoSelection 
			});
		}
		
		$("#node").on("select2:select", function(e) {
			reloadMonitor($('#node').val());
		});
	}
	
	var reloadMonitor = function(id, row) {
		setTimeout(function() {
			$.cookie("machairodus.monitor.load", id, { expires : 30, path : "/" });
			queues['viewCpu'].clear();
			queues['viewMemory_COMMITED'].clear();
			queues['viewMemory_USED'].clear();
			queues['viewClasses_LOADED'].clear();
			queues['viewClasses_UNLOADED'].clear();
			queues['viewThreads_THREAD_COUNT'].clear();
			queues['viewThreads_DAEMON_THREAD_COUNT'].clear();
			diffTime = 0;
			
			var text;
			var nodeType;
			if(row) {
				text = row.serverName + "." + row.name + "(" + row.serverAddress + ":" + row.jmxPort + "), TYPE: " + this.nodeType(row.type);
				nodeType = row.type;
			} else {
				$.each($('#node').select2('data'), function(idx, item) {
					if(item.id == $('#node').val()) {
						text = item.text;
						nodeType = item.type;
					}
				});
			}
		
			Chat.sendMessage(id, text, nodeType, 'add');
		}, 1000);
	}
	
	var formatRepo = function(repo) {
		if (repo.loading) return repo.text;
		
		repo.text = repo.serverName + "." + repo.name + "(" + repo.serverAddress + ":" + repo.jmxPort + "), TYPE: " + this.nodeType(repo.type);
	    return repo.text;
	}

	var formatRepoSelection = function(repo) {
		if(repo.text)
			return repo.text;
		
	  	return formatRepo(repo);
	}
	
	this.nodeType = function(type) {
		switch(type) {
			case 1: 
				return '均衡器';
			case 2: 
				return '调度器';
			case 3: 
				return '服务节点';
			default: 
				return 'Unknown';
		}
	}
	
	return {
		init : function() {
			resizePanel();
			$("#chkCpu").bind('click', function() { panelToggle(this, '#pnlCpu'); });
			$("#chkMemory").bind('click', function() { panelToggle(this, '#pnlMemory'); });
			$("#chkClasses").bind('click', function() { panelToggle(this, '#pnlClasses'); });
			$("#chkThreads").bind('click', function() { panelToggle(this, '#pnlThreads'); });

			$('#pnlCpu').panel({ onClose : function() { if($("#chkCpu")[0].checked) $("#chkCpu").click(); } });
			$('#pnlMemory').panel({ onClose : function() { if($("#chkMemory")[0].checked) $("#chkMemory").click(); } });
			$('#pnlClasses').panel({ onClose : function() { if($("#chkClasses")[0].checked) $("#chkClasses").click(); } });
			$('#pnlThreads').panel({ onClose : function() { if($("#chkThreads")[0].checked) $("#chkThreads").click(); } });

			setTimeout(function() {
				initBasicTime(null, $('#viewCpu')[0]);
				initBasicTime(null, $('#viewMemory')[0]);
				initBasicTime(null, $('#viewClasses')[0]);
				initBasicTime(null, $('#viewThreads')[0]);
				
				initSelect2();
			}, 100);
			
			var resize;
			window.onresize = function() { 
				if (resize) clearTimeout(resize);
				resize = setTimeout(function() { resizePanel(); }, 90);
			};
		},
		
		receiver : function(data) {
			setData(data);
		}
	};
}();

Monitor.Load.init();
