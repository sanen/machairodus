var Configure = Configure || {};
Configure.Service = function(){
	var $table = $('#configure-service-table'), 
	$btnQuery = $('#btnQuery'), 
	$btnAdd = $('#btnAdd'), 
	$btnModify = $('#btnModify'), 
	$btnRefresh = $('#btnRefresh'), 
	$btnDelete = $('#btnDelete'),
	$btnAssign = $('#btnAssign'), 
	
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
		
		if($('#dlgFind #name').val() != '')
			params["name[]"] = $('#dlgFind #name').getTags();
		
		if($('#dlgFind #uri').val() != '')
			params["uri[]"] = $('#dlgFind #uri').getTags();
		
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
		$('#dlgOption #name').val('');
		$('#dlgOption #uri').val('');
		$('#dlgOption #options').val('');
		$('#dlgOption #type').val(null).trigger("change");
		$('#dlgOption #description').val('');
		$('#dlgOption').dialog('open');
	}
	
	var modify = function() {
		var selections = $table.bootstrapTable('getSelections');
		if(selections.length > 0) {
			optionType = 'modify';
			$('#dlgOption #name').val(selections[0].name);
			$('#dlgOption #uri').val(selections[0].uri);
			$('#dlgOption #options').val(selections[0].options);
			$('#dlgOption #type').val(selections[0].type).trigger("change");
			$('#dlgOption #description').val(selections[0].description);
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
		var name = $('#dlgOption #name').val();
		var uri = $('#dlgOption #uri').val();
		var options = $('#dlgOption #options').val();
		var type = $('#dlgOption #type').val();
		var description = $('#dlgOption #description').val();
		if(!validSchedulerConfig(name, uri, type)) {
			return ;
		} 
		
		var schedulerConfig = JSON.stringify({ name: name, uri: uri, options: options, type: type, description: description });
		$.ajax({
			url: context + '/configure/service/add',
			type: "POST", 
			contentType: "application/x-www-form-urlencoded; charset=utf-8", 
			data: { schedulerConfig: schedulerConfig }, 
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
			
		var name = $('#dlgOption #name').val();
		var uri = $('#dlgOption #uri').val();
		var options = $('#dlgOption #options').val();
		var type = $('#dlgOption #type').val();
		var description = $('#dlgOption #description').val();
		if(!validSchedulerConfig(name, uri, type)) {
			return ;
		} 
			
		var schedulerConfig = JSON.stringify({ id: ids[0], name: name, uri: uri, options: options, type: type, description: description });
		$.ajax({
			url: context + '/configure/service/update',
			type: "POST", 
			contentType: "application/x-www-form-urlencoded; charset=utf-8", 
			data: { schedulerConfig: schedulerConfig }, 
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
	
	var validSchedulerConfig = function(name, uri, type) {
		if($.trim(name) == '') {
			$.messager.alert('错误', '名称不能为空');
			return false;
		}
		
		if($.trim(uri) == '') {
			$.messager.alert('错误', '服务URI不能为空');
			return false;
		}
		
		if($.trim(type) == '') {
			$.messager.alert('错误', '请求类型不能为空');
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
						url: context + '/configure/service/delete',
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
	
	var assign = function() {
		var selections = getIdSelections();
		if(selections.length > 0) {
			$('#dlgAssign').dialog('refresh', context + '/configure/service/assign/' + selections[0]);
			$('#dlgAssign').dialog('open');
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
			url: context + '/configure/service/find',
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
		$btnAssign.prop('disabled', isSelected);
	}
	
	var getIdSelections = function() {
        return $.map($table.bootstrapTable('getSelections'), function (row) {
            return row.id;
        });
    }
	
	var initButtonEvent = function() {
		$btnQuery.bind('click', query);
		$btnAdd.bind('click', add);
		$btnModify.bind('click', modify);
		$btnRefresh.bind('click', refresh);
		$btnDelete.bind('click', _delete);
		$btnAssign.bind('click', assign);
		
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
		
		$('#dlgAssign').dialog({
		    title: '分配节点',
		    width: 910,
		    height: 269,
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
		
		$('#dlgFind #uri').tagsInput({
			defaultText: '添加条件', 
			height: 34
		});
	}
	
	var initSelect2 = function() {
		$("#dlgOption #type").select2({
			placeholder: "请选择类型",
			allowClear: true
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
		
		typeFormatter: function(value, row) {
			switch(value) {
				case 0: 
					return 'GET';
				case 1: 
					return 'POST';
				default: 
					return value;
			}
		}, 
		
		getIdSelection: function() {
			var selections = getIdSelections();
			if(selections.length > 0) {
				return selections[0];
			}
			
			return null;
		}
	}
}();

Configure.Service.init();