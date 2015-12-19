var Configure = Configure || {};
Configure.Server = function(){
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
		
		if($('#dlgFind #server').val() != '')
			params["name[]"] = $('#dlgFind #name').getTags();
		
		if($('#dlgFind #name').val() != '')
			params["address[]"] = $('#dlgFind #address').getTags();
		
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
		$('#dlgOption #address').val('');
		$('#dlgOption #username').val('');
		$('#dlgOption #passwd').val('');
		$('#dlgOption').dialog('open');
	}
	
	var modify = function() {
		var selections = $table.bootstrapTable('getSelections');
		if(selections.length > 0) {
			optionType = 'modify';
			$('#dlgOption #name').val(selections[0].name);
			$('#dlgOption #address').val(selections[0].address);
			$('#dlgOption #username').val(selections[0].username);
			$('#dlgOption #passwd').val(selections[0].passwd);
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
		var address = $('#dlgOption #address').val();
		var username = $('#dlgOption #username').val();
		var passwd = $('#dlgOption #passwd').val();
		if(!validServerConfig(name, address, username, passwd)) {
			return ;
		} 
		
		var serverConfig = JSON.stringify({ name: name, address: address, username: username, passwd: passwd});
		$.ajax({
			url: context + '/configure/node/add',
			type: "POST", 
			contentType: "application/x-www-form-urlencoded; charset=utf-8", 
			data: { serverConfig: serverConfig }, 
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
		var address = $('#dlgOption #address').val();
		var username = $('#dlgOption #username').val();
		var passwd = $('#dlgOption #passwd').val();
		if(!validServerConfig(name, address, username, passwd)) {
			return ;
		} 
			
		var serverConfig = JSON.stringify({ id: ids[0], name: name, address: address, username: username, passwd: passwd});
		$.ajax({
			url: context + '/configure/node/update',
			type: "POST", 
			contentType: "application/x-www-form-urlencoded; charset=utf-8", 
			data: { serverConfig: serverConfig }, 
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
	
	var validServerConfig = function(name, address, username, passwd) {
		if($.trim(name) == '') {
			$.messager.alert('错误', '名称不能为空');
			return false;
		}
		
		if($.trim(address) == '') {
			$.messager.alert('错误', '地址不能为空');
			return false;
		}
		
		if($.trim(username) == '') {
			$.messager.alert('错误', '用户名不能为空');
			return false;
		}
		
		if($.trim(passwd) == '') {
			$.messager.alert('错误', '密码不能为空');
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
		$('#dlgFind #server').tagsInput({
			defaultText: '添加条件', 
			height: 34
		});
		
		$('#dlgFind #name').tagsInput({
			defaultText: '添加条件', 
			height: 34
		});
	}
	
	var initSelect2 = function() {
		$("#dlgOption #server").select2({
			placeholder: "Select a state",
			allowClear: true
		});
		
		$("#dlgFind #type").select2({
			placeholder: "Select a state",
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
		}
	}
}();

Configure.Server.init();