<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link href="${pageContext.request.contextPath}/media/css/api/bootstrap-table.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/easyui.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/jquery.tagsinput.min.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/media/css/api/select2.min.css" type="text/css" rel="stylesheet">

<!-- BEGIN PAGE CONTENT-->          
<div class="container-fluid">
	<div class="row-fluid">
		<div class="row-fluid">
			<button id="btnQuery" class="btn black" type="button">查询</button>
			<button id="btnAdd" class="btn black" type="button">新增</button>
			<button id="btnRefresh" class="btn black" type="button">刷新</button>
			<button id="btnModify" class="btn black" type="button" disabled>修改</button>
			<button id="btnDelete" class="btn black" type="button" disabled>删除</button>
		</div>
		<p>
		<div class="row-fluid">
			<table id="configure-node-table" data-toggle="table" data-locale="zh-CN">
				<thead>
				<tr>
                    <th data-field="state" data-checkbox="true" />
					<th data-field="id" data-visible="false">ID</th>
					<th data-field="serverId" data-visible="false">SERVER_ID</th>
					<th data-field="serverName" data-width="100" data-align="center" data-valign="middle" data-sortable="true">服务器</th>
					<th data-field="name" data-width="100" data-align="center" data-valign="middle" data-sortable="true">节点名称</th>
					<th data-field="port" data-width="100" data-align="center" data-valign="middle" data-sortable="true">端口</th>
					<th data-field="jmxPort" data-width="100" data-align="center" data-valign="middle" data-sortable="true">JMX端口</th>
					<th data-field="type" data-width="100" data-align="center" data-valign="middle" data-sortable="true" data-formatter="Configure.Node.typeFormatter">类型</th>
					<th data-field="weight" data-width="100" data-align="center" data-valign="middle" data-sortable="true">权重</th>
					<th data-field="pid" data-width="100" data-align="center" data-valign="middle" data-sortable="true">进程号</th>
					<th data-field="createTime" data-width="100" data-align="center" data-valign="middle" data-sortable="true">创建时间</th>
					<th data-field="createUserName" data-width="100" data-align="center" data-valign="middle" data-sortable="true">创建人</th>
					<th data-field="modifyTime" data-width="100" data-align="center" data-valign="middle" data-sortable="true">修改时间</th>
					<th data-field="modifyUserName" data-width="100" data-align="center" data-valign="middle" data-sortable="true">修改人</th>
				</tr>
				</thead>
			</table>
		</div>
	</div>
</div>

<div id="dlgFind">
	<div class="modal-body">
		<div class="row-fluid">
			<form class="form-horizontal" action="#">
				<div class="control-group">
					<label class="control-label" for="server">服务器</label>
					<div class="controls">
						<select id="server" multiple="multiple" style="width: 312px;">
							<option value="" selected="selected"></option>
						</select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="name">节点名称</label>
					<div class="controls">
						<input type="text" class="m-wrap span12 tags" id="name">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="type">类型</label>
					<div class="controls">
						<select id="type" multiple="multiple" style="width: 312px;">
							<option value="1">均衡器</option>
							<option value="2">调度器</option>
							<option value="3">服务节点</option>
						</select>
					</div>
				</div>
			</form>
		</div>
	</div>
	<div class="modal-footer">
		<button id="btnFindEnter" data-dismiss="modal" class="btn black" type="button">确定</button>
		<button id="btnFindCancle" data-dismiss="modal" class="btn black" type="button">取消</button>
	</div>
</div>
<div id="dlgOption">
	<div class="modal-body">
		<div class="row-fluid">
			<form class="form-horizontal" action="#">
				<div class="control-group">
					<label class="control-label" for="server">服务器
						<span class="required">*</span>
					</label>
					<div class="controls">
						<select id="server" style="width: 405px;"></select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="name">名称
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="text" class="m-wrap span12" id="name">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="optionType">类型
						<span class="required">*</span>
					</label>
					<div class="controls">
						<select id="optionType" style="width: 405px;">
							<option value="" selected="selected"></option>
							<option value="1">均衡器</option>
							<option value="2">调度器</option>
							<option value="3">服务节点</option>
						</select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="port">端口
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="text" class="m-wrap span12" id="port">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="jmxPort">JMX端口</label>
					<div class="controls">
						<input type="text" class="m-wrap span12" id="jmxPort">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="weight">权重</label>
					<div class="controls">
						<input type="text" class="m-wrap span12" id="weight">
					</div>
				</div>
			</form>
		</div>
	</div>
	<div class="modal-footer">
		<button id="btnOptionEnter" data-dismiss="modal" class="btn black" type="button">确定</button>
		<button id="btnOptionCancle" data-dismiss="modal" class="btn black" type="button">取消</button>
	</div>
</div>
<!-- END PAGE CONTENT-->