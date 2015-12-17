var Configure = Configure || {};
Configure.Server = function(){
	var $table = $('#configure-server-table'), 
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
		
		params.name = $('#dlgFind #name').val();
		params.address = $('#dlgFind #address').val();
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
		
	}
	
	var modifyRequest = function() {
		
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
					$table.bootstrapTable('remove', {
		                field: 'id',
		                values: selections
		            });
					
					updateButtonStatus();
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
			url: context + '/configure/server/find',
			queryParams: function(params) { return queryParams(params); }, 
			contentType: 'application/x-www-form-urlencoded; charset=utf-8'
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
	
	return {
		init: function() {
			initTable();
			initButtonEvent();
			initDialog();
		}
	}
}();

Configure.Server.init();