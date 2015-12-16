var Configure = Configure || {};
Configure.Server = function(){
	var $table = $('#configure-server-table'), 
	$btnQuery = $('#btnQuery'), 
	$btnAdd = $('#btnAdd'), 
	$btnModify = $('#btnModify'), 
	$btnDelete = $('#btnDelete'), 
	selections = []
	;
	
	var query = function() {
		$('#dlgFind #name').val('');
		$('#dlgFind #address').val('');
		$('#dlgFind').dialog('open');
	}
	
	var add = function() {
		$('#dlgOption #type').val('add');
		$('#dlgOption #name').val('');
		$('#dlgOption #address').val('');
		$('#dlgOption #username').val('');
		$('#dlgOption #passwd').val('');
		$('#dlgOption').dialog('open');
	}
	
	var modify = function() {
		var selections = $table.bootstrapTable('getSelections');
		if(selections.length > 0) {
			$('#dlgOption #type').val('modify');
			$('#dlgOption #name').val(selections[0].name);
			$('#dlgOption #address').val(selections[0].address);
			$('#dlgOption #username').val(selections[0].username);
			$('#dlgOption #passwd').val(selections[0].passwd);
			$('#dlgOption').dialog('open');
		} else {
			
		}
	}
	
	var _delete = function() {
		console.log(selections);
	}
	
	var initTable = function() {
		$table.bootstrapTable({
			method: 'post',
			cache: false,
			pagination: true,
			pageSize: 20,
			pageNumber:1,
			pageList: [10, 20, 50, 100, 200, 500],
			clickToSelect: true,
			singleSelect: true,
			data: [
			       {id: 1, name: '123', address: 'localhost'}
			]
		});
		
		$table.on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table', function () {
			$btnDelete.prop('disabled', !$table.bootstrapTable('getSelections').length);
			$btnModify.prop('disabled', !$table.bootstrapTable('getSelections').length);
			selections = getIdSelections();
		});

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
		$btnDelete.bind('click', _delete);
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