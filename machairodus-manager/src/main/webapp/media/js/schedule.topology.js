var Schedule = Schedule || {};
Schedule.Topology = function(){
	var $table = $('#schedule-topology-table'), 
	$btnQuery = $('#btnQuery'), 
	$btnAdd = $('#btnAdd'), 
	$btnAppend = $('#btnAppend'), 
	$btnRefresh = $('#btnRefresh'), 
	$btnBatchStart = $('#btnBatchStart'), 
	$btnBatchStop = $('#btnBatchStop'), 
	$btnBatchRemove = $('#btnBatchRemove'), 
	
	$btnFindEnter = $('#btnFindEnter'),
	$btnFindCancle = $('#btnFindCancle'),
	init = true
	;
	
	var query = function() {
		$('#dlgFind').dialog('open');
	}
	
	var queryParams = function(params) {
		if(init) {
			params.init = true;
			init = false;
		}
		
		if($('#dlgFind #node').val() != null && $('#dlgFind #node').val() != '')
			params["node[]"] = $('#dlgFind #node').val();
		
		if($('#dlgFind #group').val() != '')
			params["group[]"] = $('#dlgFind #group').getTags();
		
		if($('#dlgFind #quartzId').val() != '')
			params["quartzId[]"] = $('#dlgFind #quartzId').getTags();
		
		if($('#dlgFind #status').val() != null && $('#dlgFind #status').val() != '')
			params["status[]"] = $('#dlgFind #status').val();
		
		return params;
	}
	
	var findEnter = function() {
		$('#dlgFind').dialog('close');
		refresh();
	}
	
	var findCancle = function() {
		$('#dlgFind').dialog('close');
	}
	
	var add = function() {
		
	}
	
	var append = function() {
		
	}
	
	var refresh = function() {
		$table.bootstrapTable('refresh');
		setTimeout(function() {
			updateButtonStatus();
		}, 100);
	}
	
	var batchStart = function() {
		
	}
	
	var batchStop = function() {
		
	}
	
	var batchRemove = function() {
		
	}
	
	var start = function(row) {
		ajax('/schedule/topology/start', 'POST', { st: JSON.stringify(row) }, success, error);
	}

	var stop = function(row) {
		ajax('/schedule/topology/stop', 'POST', { st: JSON.stringify(row) }, success, error);
	}

	var remove = function(row) {
		ajax('/schedule/topology/remove', 'POST', { st: JSON.stringify(row) }, success, error);
	}
	
	var success = function(data) {
		if(data.status) {
			if(data.status == '2001') {
				$table.bootstrapTable('updateByUniqueId', {
					id: data.item.id,
					row: data.item
				});
			} else if(data.status == '2099') {
				$.messager.alert('错误', data.message);
			}
		} else {
			$.messager.alert('错误', '未知错误');
		}
	}
	
	var error = function(error) {
		$.messager.alert('错误', error);
	}

	var ajax = function(url, type, data, success, error) {
		$.ajax({
			url : context + url,
			type : type || 'POST',
			contentType : "application/x-www-form-urlencoded; charset=utf-8",
			data : data,
			success : success,
			error : error
		});
	}
	 
	var initTable = function() {
		$table.bootstrapTable({
			method: 'post',
			cache: false,
			pagination: true,
			height: 400,
			pageSize: 20,
			pageNumber:1,
			pageList: [10, 20, 50, 100, 200, 500],
			clickToSelect: true,
			singleSelect: false,
//			sidePagination: 'server',  // 前端分页
			url: context + '/schedule/topology/find',
			queryParams: function(params) { return queryParams(params); }, 
			contentType: 'application/x-www-form-urlencoded; charset=utf-8', 
			uniqueId: 'id'
		});
		
		$table.on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
			updateButtonStatus();
		});

	}
	
	var updateButtonStatus = function() {
//		var selections;
//		var isSelected = !(selections = $table.bootstrapTable('getSelections')).length;
		
	}
	
	var getIdSelections = function() {
        return $.map($table.bootstrapTable('getSelections'), function (row) {
            return row.id
        });
    }
	
	var initButtonEvent = function() {
		$btnQuery.bind('click', query);
		$btnAdd.bind('click', add);
		$btnAppend.bind('click', append);
		$btnRefresh.bind('click', refresh); 
		$btnBatchStart.bind('click', batchStart);
		$btnBatchStop.bind('click', batchStop); 
		$btnBatchRemove.bind('click', batchRemove); 
		
		$btnFindEnter.bind('click', findEnter);
		$btnFindCancle.bind('click', findCancle);
	}
	
	var initDialog = function() {
		$('#dlgFind').dialog({
		    title: '查询',
		    width: 500,
		    closed: true,
		    cache: false,
		    modal: true
		});
		
		dialogExtend();
	}
	
	var initSelect2 = function() {
		$("#dlgFind #node").select2({
			placeholder: "选择节点",
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
		
		$("#dlgFind #status").select2({
			placeholder: "选择状态",
			allowClear: true
		});
	}
	
	var formatRepo = function(repo) {
	      if (repo.loading) return repo.text;

	      repo.text = repo.serverName + "." + repo.name;
	      repo.id = JSON.stringify(repo);
	      return repo.serverName + "." + repo.name + " (" + repo.serverAddress + ":" + repo.port + ")";
	}

    var formatRepoSelection = function(repo) {
    	return repo.text;
    }
    
    var initTagsInput = function() {
		$('#dlgFind #group').tagsInput({
			defaultText: '添加条件', 
			height: 34
		});
		
		$('#dlgFind #quartzId').tagsInput({
			defaultText: '添加条件', 
			height: 34
		});
	}
    
	return {
		init: function() {
			initTagsInput();
			initSelect2();
			initButtonEvent();
			initDialog();
			initTable();
			
		},
	
		operateFormatter: function(value, row) {
			var startStatus, stopStatus, removeStatus;
			if(row.status) {
				switch(row.status) {
					case 'STARTED': 
						startStatus = "disabled";
						stopStatus = "";
						removeStatus = "";
						break;
						
					case 'STOPPING': 
						startStatus = "disabled";
						stopStatus = "disabled";
						removeStatus = "disabled";
						break;
						
					case 'STOPPED': 
						startStatus = "";
						stopStatus = "disabled";
						removeStatus = "";
						break;
						
					default: 
						return ;
				}
					
			}
			
			var id = row.nodeAddress.replace(':', '_') + "-" + row.nodeName + "-" + row.quartzId;
			var content =
			"<div class='row-fluid'>" +
			"	<button id='btnStart-"+id+"' class='btn black' type='button' "+startStatus+">启动</button>" +
			"	<button id='btnStop-"+id+"' class='btn black' type='button' "+stopStatus+">停止</button>" +
			"	<button id='btnRemove-"+id+"' class='btn black' type='button' "+removeStatus+">移除</button>" +
			"</div>";
			
			setTimeout(function() {
				$('#btnStart-' + id).bind('click', function() {
					start(row);
				});
				
				$('#btnStop-' + id).bind('click', function() {
					stop(row);
				});
				
				$('#btnRemove-' + id).bind('click', function() {
					remove(row);
				});
			}, 100);
			
			return content;
		}, 
		
		statusFormatter: function(value, row) {
			switch(row.status) {
			case 'STARTED': 
				return '已启动';
				
			case 'STOPPING': 
				return '停止中';
				
			case 'STOPPED': 
				return '已停止';
				
			default: 
				return value;
		}
		}
	}
}();

Schedule.Topology.init();