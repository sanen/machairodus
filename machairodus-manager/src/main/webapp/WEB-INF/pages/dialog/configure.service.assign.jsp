<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.machairodus.mappers.domain.NodeConfig,java.util.List" %>
<div style="margin: 5px;">
	<select multiple="multiple" id="nodeConfig" name="nodeConfig[]">
	<%
	List<NodeConfig> unselect = (List<NodeConfig>) request.getAttribute("unselect");
	if(unselect != null && !unselect.isEmpty()) {
		for(NodeConfig cfg : unselect) {
			String text = cfg.getServerAddress() + ":" + cfg.getPort() + ":" + cfg.getJmxPort();
			%><option value='<%=cfg.getId() %>'><%=text %></option><%
		}
	}
	
	List<NodeConfig> select = (List<NodeConfig>) request.getAttribute("select");
	if(select != null && !select.isEmpty()) {
		for(NodeConfig cfg : select) {
			String text = cfg.getServerAddress() + ":" + cfg.getPort() + ":" + cfg.getJmxPort();
			%><option value='<%=cfg.getId() %>' selected><%=text %></option><%
		}
	}
	%>
	</select>
</div>

<script>
	$().ready(function() {
		$('#nodeConfig').multiSelect({
			selectableHeader: "<input type='text' class='search-input' autocomplete='off'>",
			selectionHeader: "<input type='text' class='search-input' autocomplete='off'>",
			afterInit: function(ms){
				var that = this,
			    $selectableSearch = that.$selectableUl.prev(),
				$selectionSearch = that.$selectionUl.prev(),
			    selectableSearchString = '#'+that.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)',
			    selectionSearchString = '#'+that.$container.attr('id')+' .ms-elem-selection.ms-selected';

			    that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
			    .on('keydown', function(e){
			      if (e.which === 40){
			        that.$selectableUl.focus();
			        return false;
			      }
			    });

			    that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
			    .on('keydown', function(e){
			    	if (e.which == 40){
			        	that.$selectionUl.focus();
			       		return false;
			      	}
			    });
			},
			afterSelect: function(values) {
				ajax(Configure.Service.getIdSelection(), values[0], 'select')
			},
			afterDeselect: function(values) {
				ajax(Configure.Service.getIdSelection(), values[0], 'deselect')
			}
		});
		
		var ajax = function(id, value, type) {
			$.ajax({
				url: context + '/configure/service/assign/' + id + '/' + value + '/' + type,
				type: "POST", 
				contentType: "application/x-www-form-urlencoded; charset=utf-8", 
				success: function(data) {
					if(data.status) {
						if(data.status == '2099') {
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
</script>