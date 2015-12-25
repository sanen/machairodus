var Monitor = Monitor || {};
Monitor.TPS = function() {
	var $btnAdd = $('#btnAdd'), 
	$btnOptionEnter = $('#btnOptionEnter'), 
	$btnOptionCancle = $('#btnOptionCancle'), 
	portlet;
	
	var times = {};
	var diffTimes = {};
	var queues = {};
	var queueIds = {};
	var interval;
	var maxTime = 600000;
	var defaultQueueSize = 650;
	
	var add = function() {
		$('#dlgOption').dialog('open');
	}
	
	var addEnter = function() {
		var selectedText = $("#dlgOption #node option:selected").text();
		var nodeId = $("#dlgOption #node option:selected").val();
		if(selectedText == null || selectedText == '') {
			$.messager.alert('警告', '请选择服务节点');
			return ;
		}
		
		var exists = false;
		$.each($('.portlet-title .caption'), function(idx, element) {
			var html = $(element).html();
			if(html == selectedText) {
				exists = true;
			}
		})
		
		if(exists) {
			$.messager.alert('警告', '已经存在所选服务节点，不能重复选择');
			return ;
		}
		
		$('#dlgOption').dialog('close');
		appendMonitor(nodeId, selectedText);
	}
	
	var appendMonitor = function(id, server) {
		var portlet = 
		'<div id="ptlServ' + id + '" class="column sortable">' +
		'	<div class="portlet box blue">' +
		'		<div class="portlet-title">' +
		'			<div class="caption">' + server + '</div>' +
		'			<div class="actions">' +
		'				<a id="btnServ' + id + '" class="btn blue mini" href="#"><i class="icon-remove"></i></a>' +
		'			</div>' +
		'		</div>' +
		'		<div class="portlet-body" style="height: 250px;">' +
		'			<div id="view_'+id+'" class="div100"></div>';
		'		</div>' +
		'	</div>' +
		'</div>';
			
		$('#monitor').append(portlet);
		drawGraph($('#view_'+id)[0], null, null);
		Chat.sendMessage(id, server, 'add');
		
		$('#btnServ' + id).bind('click', function() {
			$('#ptlServ' + id).remove();
			Chat.sendMessage(id, server, 'remove');
			if(queueIds['view_' + id]) {
				$.each(queueIds['view_' + id], function(idx, queueId) { delete queues[queueId]; });
				delete queueIds['view_' + id];
				delete times['view_' + id];
				delete diffTimes['view_' + id];
			}
		});
	}
	
	var addCancle = function() {
		$('#dlgOption').dialog('close');
	}
	
	var initButtonEvent = function() {
		$btnAdd.bind('click', add);
		$btnOptionEnter.bind('click', addEnter);
		$btnOptionCancle.bind('click', addCancle);
	}
	
	var initDialog = function() {
		$('#dlgOption').dialog({
		    title: '添加节点',
		    width: 400,
		    closed: true,
		    cache: false,
		    modal: true
		});
		
		dialogExtend();
	}
	
	var initSelect2 = function() {
		$("#dlgOption #node").select2({
			placeholder: "选择服务节点",
			ajax: {
				url: context + '/configure/node/find/simple',
				dataType: 'json',
			    delay: 300,
			    data: function (params) {
			      return {
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
	
	var formatRepo = function(repo) {
		if (repo.loading) return repo.text;

		repo.text = repo.serverAddress + ':' + repo.jmxPort;
		
		var markup = 
		'<div>'+ repo.serverName + '.' + repo.name +'</div>' + 
		'<div>('+ repo.serverAddress + ':' + repo.jmxPort +')</div>';
		
		return markup;
	}

	var formatRepoSelection = function(repo) {
		if(repo.serverAddress && repo.jmxPort) 
	  		return repo.serverAddress + ':' + repo.jmxPort;
	  	
	  	return repo.text;
	}
	
	var initSortable = function() {
		if (!jQuery().sortable) {
            return;
        }

        $("#monitor").sortable({
            connectWith: ".portlet",
            items: ".portlet",
            opacity: 0.8,
            coneHelperSize: true,
            placeholder: 'sortable-box-placeholder round-all',
            forcePlaceholderSize: true,
            tolerance: "pointer"
        });

        $(".column").disableSelection();
	}
	
	var drawGraph = function (container, opts, data) {
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
		
		if(!opts)
			opts = {};
		
		if(!opts.diffTime)
			opts.diffTime = 0;
		
		max += max * 0.3;
		var options = {
		    xaxis : { min: now - opts.diffTime, max: now, mode : 'time', timeMode: 'local', labelsAngle : 0 },
		    selection : { mode : 'x' },
		    HtmlText : false,
            resolution:2
		};
		
		o = Flotr._.extend(Flotr._.clone(options), opts || {});
		o.yaxis = { min: 0, max: max }
		Flotr.draw(container, data || [], o);
	}
	
	var setData = function(data) {
		if(data.status)
			return ;
		
		if(!data.TPS)
			data.TPS = [];
		
		var diff = data.TIME - times[data.ID];
		if(!diffTimes[data.ID])
			diffTimes[data.ID] = 0;
		
		if(!isNaN(diff) && diffTimes[data.ID] < maxTime) {
			diffTimes[data.ID] += diff;
			if(diffTimes[data.ID] > maxTime)
				diffTimes[data.ID] = maxTime;
		}
		
		times[data.ID] = data.TIME;

		
		var options = {};
		options.diffTime = diffTimes[data.ID];
		options.time = data.TIME;
		
		$.each(data.TPS, function(idx, pointer) {
			if(!queues[data.ID + "_" + pointer.scene]) {
				queues[data.ID + "_" + pointer.scene] = new Queue(defaultQueueSize);
				if(!queueIds[data.ID])
					queueIds[data.ID] = [];
				
				queueIds[data.ID].push(data.ID + "_" + pointer.scene);
			}
			
			queues[data.ID + "_" + pointer.scene].offer([pointer.time, pointer.tps]);
		});
		
		if($('#' + data.ID)[0].clientWidth != 0 && $('#' + data.ID)[0].clientHeight != 0) {
			var _data = [];
			$.each(data.TPS, function(idx, pointer) {
				_data.push({ data: queues[data.ID + "_" + pointer.scene].get(), label: pointer.scene + ' ( '+queues[data.ID + "_" + pointer.scene].last()[1]+' )' });
			});
			
			drawGraph($('#' + data.ID)[0], options, _data);
		}
	}
	
	var initMonitor = function() {
		$.ajax({
			url: context + '/monitor/tps/init',
			type: "POST", 
			contentType: "application/x-www-form-urlencoded; charset=utf-8", 
			data: { monitorType: 'monitor.tps' }, 
			success: function(data) {
				if(data.status) {
					if(data.status == '2001') {
						if(data.items) {
							$.each(data.items, function(idx, item) {
								appendMonitor(item.id, item.server);
							});
						}
					} else if(data.status == '2099') {
						$.messager.alert('错误', data.message);
					}
				} else {
					$.messager.alert('错误', '未知错误');
				}
			}, 
			error: function(data) {
				$.messager.alert('错误', data);
			}
		});
		
	}
	
	var resizeCanvas = function() {
		$.each($('.portlet-body div'), function(idx, element) {
			drawGraph($('#' + element.id)[0], null, null);
		});
	}
	
	return {
		init: function() {
			initButtonEvent();
			initDialog();
			initSelect2();
			initSortable();
			
			setTimeout(function() { initMonitor(); }, 1500);
			var resize;
			$(window).resize(function() { 
				if (resize) clearTimeout(resize);
				resize = setTimeout(function() { resizeCanvas(); }, 100); 
			});
		},
	
		receiver: function(data) {
			setData(data);
		}
	}
}();

Monitor.TPS.init();