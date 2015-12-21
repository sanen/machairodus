var Configure = Configure || {};
Configure.Node = function(){
	var $table = $('#configure-node-table'), 
	$btnQuery = $('#btnQuery'), 
	$btnAdd = $('#btnAdd'), 
	$btnModify = $('#btnModify'), 
	$btnRefresh = $('#btnRefresh'), 
	$btnDelete = $('#btnDelete'), 
	
	$btnFindEnter = $('#btnFindEnter'),
	$btnFindCancle = $('#btnFindCancle'),
	$btnOptionEnter = $('#btnOptionEnter'), 
	$btnOptionCancle = $('#btnOptionCancle')
	init = true, 
	optionType = ''
	;
	
	var query = function() {
		$('#dlgFind').dialog('open');
	}
	
	var queryParams = function(params) {
		if(init) {
			params.init = true;
			init = false;
		}
		
		if($('#dlgFind #server').val() != null && $('#dlgFind #server').val() != '')
			params["server[]"] = $('#dlgFind #server').val();
		
		if($('#dlgFind #name').val() != '')
			params["node[]"] = $('#dlgFind #name').getTags();
		
		if($('#dlgFind #type').val() != null && $('#dlgFind #type').val() != '')
			params["type"] = $('#dlgFind #type').val();
		
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
		optionType = 'add';
		reinitServerSelect();
		$('#dlgOption #server').val(null).trigger("change"); 
		$('#dlgOption #name').val('');
		$('#dlgOption #optionType').val(null).trigger("change");
		$('#dlgOption #port').val('');
		$('#dlgOption #jmxPort').val('');
		$('#dlgOption #weight').val('');
		$('#dlgOption').dialog('open');
	}
	
	var modify = function() {
		var selections = $table.bootstrapTable('getSelections');
		if(selections.length > 0) {
			optionType = 'modify';
			$('#dlgOption #server').val(selections[0].serverId).trigger("change"); 
			reinitServerSelect(selections[0]);
			
			$('#dlgOption #name').val(selections[0].name);
			$('#dlgOption #optionType').val(selections[0].type).trigger("change");
			$('#dlgOption #port').val(selections[0].port);
			$('#dlgOption #jmxPort').val(selections[0].jmxPort);
			$('#dlgOption #weight').val(selections[0].weight);
			$('#dlgOption').dialog('open');
			
		} else {
			updateButtonStatus();
		}
	}
	
	var optionEnter = function() {
		if(optionType) {
			switch(optionType) {
			case 'add': 
				addRequest();
				break;
			case 'modify':
				modifyRequest();
				break;
			}
		}
	}
	
	var addRequest = function() {
		var serverId = $('#dlgOption #server').val();
		var name = $('#dlgOption #name').val();
		var type = $('#dlgOption #optionType').val();
		var port = $('#dlgOption #port').val();
		var jmxPort = $('#dlgOption #jmxPort').val();
		var weight = $('#dlgOption #weight').val();
		if(!validNodeConfig(serverId, name, type, port)) {
			return ;
		} 
		
		var nodeConfig = JSON.stringify({ serverId: serverId, name: name, type: type, port: port, jmxPort: jmxPort, weight: weight });
		$.ajax({
			url: context + '/configure/node/add',
			type: "POST", 
			contentType: "application/x-www-form-urlencoded; charset=utf-8", 
			data: { nodeConfig: nodeConfig }, 
			success: function(data) {
				if(data.status) {
					if(data.status == '2001') {
						$('#dlgOption').dialog('close');
						$table.bootstrapTable('append', [ data.item ]);
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
	
	var modifyRequest = function() {
		var ids = getIdSelections();
		if(ids.length == 0) {
			$.messager.alert('错误', '请选择一行数据进行修改');
			return ;
		}
			
		var serverId = $('#dlgOption #server').val();
		var serverName = $('#dlgOption #server').text();
		var selections = $table.bootstrapTable('getSelections');
		if(selections.length > 0) {
			if(serverId == null) {
				serverId = selections[0].serverId;
				serverName = selections[0].serverName;
			}
		}
		
		var name = $('#dlgOption #name').val();
		var type = $('#dlgOption #optionType').val();
		var port = $('#dlgOption #port').val();
		var jmxPort = $('#dlgOption #jmxPort').val();
		var weight = $('#dlgOption #weight').val();
		if(!validNodeConfig(serverId, name, type, port)) {
			return ;
		} 
			
		var nodeConfig = JSON.stringify({ id: ids[0], serverId: serverId, serverName: serverName, name: name, type: type, port: port, jmxPort: jmxPort, weight: weight });
		$.ajax({
			url: context + '/configure/node/update',
			type: "POST", 
			contentType: "application/x-www-form-urlencoded; charset=utf-8", 
			data: { nodeConfig: nodeConfig }, 
			success: function(data) {
				if(data.status) {
					if(data.status == '2001') {
						$('#dlgOption').dialog('close');
						$table.bootstrapTable('updateByUniqueId', {
							id: ids[0], 
							row: data.item
						});
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
	
	var validNodeConfig = function(server, name, type, port) {
		if($.trim(server) == '') {
			$.messager.alert('错误', '服务器不能为空');
			return false;
		}
		
		if($.trim(name) == '') {
			$.messager.alert('错误', '名称不能为空');
			return false;
		}
		
		if($.trim(type) == '') {
			$.messager.alert('错误', '类型不能为空');
			return false;
		}
		
		if($.trim(port) == '') {
			$.messager.alert('错误', '端口不能为空');
			return false;
		}
		
		return true;
	}
	
	var optionCancle = function() {
		$('#dlgOption').dialog('close');
	}
	
	var refresh = function() {
		$table.bootstrapTable('refresh');
		setTimeout(function() {
			updateButtonStatus();
		}, 100);
	}
	
	var _delete = function() {
		var selections = getIdSelections();
		if(selections.length > 0) {
			$.messager.confirm('提示', '确定要删除选中选吗?', function(check) {
				if(check) {
					$.ajax({
						url: context + '/configure/node/delete',
						type: "POST", 
						contentType: "application/x-www-form-urlencoded; charset=utf-8", 
						data: { id: selections[0] }, 
						success: function(data) {
							if(data.status) {
								if(data.status == '2001') {
									$table.bootstrapTable('remove', {
						                field: 'id',
						                values: selections
						            });
									
									updateButtonStatus();
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
			});
		} else {
			updateButtonStatus();
		}
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
			singleSelect: true,
			sidePagination: 'server', 
			url: context + '/configure/node/find',
			queryParams: function(params) { return queryParams(params); }, 
			contentType: 'application/x-www-form-urlencoded; charset=utf-8', 
			uniqueId: 'id'
		});
		
		$table.on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
			updateButtonStatus();
		});

	}
	
	var updateButtonStatus = function() {
		var isSelected = !$table.bootstrapTable('getSelections').length;
		$btnDelete.prop('disabled', isSelected);
		$btnModify.prop('disabled', isSelected);
	}
	
	var getIdSelections = function() {
        return $.map($table.bootstrapTable('getSelections'), function (row) {
            return row.id
        });
    }
	
	var initButtonEvent = function() {
		$btnQuery.bind('click', query);
		$btnAdd.bind('click', add);
		$btnModify.bind('click', modify);
		$btnRefresh.bind('click', refresh);
		$btnDelete.bind('click', _delete);
		
		$btnFindEnter.bind('click', findEnter);
		$btnFindCancle.bind('click', findCancle);
		$btnOptionEnter.bind('click', optionEnter);
		$btnOptionCancle.bind('click', optionCancle);
	}
	
	var initDialog = function() {
		$('#dlgFind').dialog({
		    title: '查询',
		    width: 500,
		    closed: true,
		    cache: false,
		    modal: true
		});
		
		$('#dlgOption').dialog({
		    title: '数据操作',
		    width: 500,
		    closed: true,
		    cache: false,
		    modal: true
		});
		
		dialogExtend();
	}
	
	var initTagsInput = function() {
		$('#dlgFind #name').tagsInput({
			defaultText: '添加条件', 
			height: 34
		});
	}
	
	var initSelect2 = function() {
		$("#dlgFind #server").select2({
			placeholder: "选择服务器",
			ajax: {
				url: context + '/configure/server/find/simple',
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
		
		reinitServerSelect(null);
		
		$("#dlgFind #type").select2({
			placeholder: "请选择类型"
		});
		
		$("#dlgOption #optionType").select2({
			placeholder: "请选择类型",
			allowClear: true
		});
	}
	
	var reinitServerSelect = function(item) {
		if(item) {
			$('#dlgOption #server').select2({
				placeholder: "选择服务器",
				allowClear: true, 
				ajax: {
				    url: context + '/configure/server/find/simple',
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
				  initSelection: function(element, callback) {  
					  var id = item.serverId;
					  if (id !== "") {
						  $.ajax(context + '/configure/server/find/id', {
							  data : { id : id },
							  dataType : "json"
						  }).done(function(data) {
							  callback(data.rows);
						  });
					  }  
			      }, 
				  escapeMarkup: function (markup) { return markup; }, 
				  templateResult: formatRepo, 
				  templateSelection: formatRepoSelection
			});
		} else {
			$('#dlgOption #server').select2({
				placeholder: "选择服务器",
				allowClear: true, 
				ajax: {
				    url: context + '/configure/server/find/simple',
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
	}
	
	var formatRepo = function(repo) {
	      if (repo.loading) return repo.text;

	      repo.text = repo.name;
	      return "<span id='"+repo.id+"' name='"+repo.name+"' address='"+repo.address+"'>" + repo.name + " - " + repo.address + "</span>";
	}

    var formatRepoSelection = function(repo) {
    	if(repo.name) 
    		return repo.name;
    	
    	return repo.text;
    }
    
    var initInputMask = function() {
    	$('#dlgOption #port').inputmask({ mask: '9', repeat: 5, greedy: false });
    	$('#dlgOption #jmxPort').inputmask({ mask: '9', repeat: 5, greedy: false });
    	$('#dlgOption #weight').inputmask({ mask: '9', repeat: 3, greedy: false });
    }
	
	return {
		init: function() {
			initTagsInput();
			initSelect2();
			initInputMask();
			initButtonEvent();
			initDialog();
			initTable();
		},
	
		typeFormatter: function(value, row) {
			switch(value) {
				case 1: 
					return '均衡器';
				case 2: 
					return '调度器';
				case 3: 
					return '服务节点';
				default: 
					return value;
			}
		}
	}
}();

Configure.Node.init();