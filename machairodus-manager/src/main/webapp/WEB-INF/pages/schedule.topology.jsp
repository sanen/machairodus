<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link href="${pageContext.request.contextPath}/media/css/api/bootstrap-table.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/easyui.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/select2.min.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/media/css/api/jquery.tagsinput.min.css" type="text/css" rel="stylesheet">

<!-- BEGIN PAGE CONTENT-->          
<div class="container-fluid">
	<div class="row-fluid">
		<div class="row-fluid">
			<button id="btnQuery" class="btn black" type="button">查询</button>
			<button id="btnAdd" class="btn black" type="button">新增</button>
			<button id="btnAppend" class="btn black" type="button">追加</button>
			<button id="btnRefresh" class="btn black" type="button">刷新</button>
			<button id="btnBatchStart" class="btn black" type="button">批量启动</button>
			<button id="btnBatchStop" class="btn black" type="button">批量停止</button>
			<button id="btnBatchRemove" class="btn black" type="button">批量移除</button>
		</div>
		<p>
		<div class="row-fluid">
			<table id="schedule-topology-table" data-toggle="table" data-locale="zh-CN">
				<thead>
				<tr>
                    <th data-field="state" data-checkbox="true" />
                    <th data-field="id" data-visible="false">ID</th>
                    <th data-field="nodeAddress" data-width="100" data-align="center" data-valign="middle" data-sortable="true">节点地址</th>
                    <th data-field="nodeName" data-width="100" data-align="center" data-valign="middle" data-sortable="true">节点名称</th>
                    <th data-field="group" data-width="100" data-align="center" data-valign="middle" data-sortable="true">任务组</th>
					<th data-field="quartzId" data-width="100" data-align="center" data-valign="middle" data-sortable="true">任务ID</th>
					<th data-field="status" data-width="100" data-align="center" data-valign="middle" data-sortable="true" data-formatter="Schedule.Topology.statusFormatter">状态</th>
					<th data-field="operate" data-width="200" data-align="center" data-valign="middle" data-formatter="Schedule.Topology.operateFormatter">操作</th>
				</tr>
				</thead>
			</table>
		</div>
	</div>
</div>
<!-- END PAGE CONTENT-->

<div id="dlgFind">
	<div class="modal-body">
		<div class="row-fluid">
			<form class="form-horizontal" action="#">
				<div class="control-group">
					<label class="control-label" for="node">节点</label>
					<div class="controls">
						<select id="node" multiple="multiple" style="width: 312px;"></select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="group">任务组</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12 tags" id="group">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="quartzId">任务ID</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12 tags" id="quartzId">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="status">状态</label>
					<div class="controls">
						<select id="status" multiple="multiple" style="width: 312px;">
							<option value="started">已启动</option>
							<option value="stopping">停止中</option>
							<option value="stopped">已停止</option>
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