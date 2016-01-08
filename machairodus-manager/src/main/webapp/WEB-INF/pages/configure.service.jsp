<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link href="${pageContext.request.contextPath}/media/css/api/bootstrap-table.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/easyui.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/jquery.tagsinput.min.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/media/css/api/select2.min.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/media/css/api/multi-select.css" type="text/css" rel="stylesheet">

<style>
.form-horizontal .control-label {
	width: 70px;
}
.form-horizontal .controls {
	margin-left: 75px;
}

</style>

<!-- BEGIN PAGE CONTENT-->          
<div class="container-fluid">
	<div class="row-fluid">
		<div class="row-fluid">
			<button id="btnQuery" class="btn black" type="button">查询</button>
			<button id="btnAdd" class="btn black" type="button">新增</button>
			<button id="btnRefresh" class="btn black" type="button">刷新</button>
			<button id="btnModify" class="btn black" type="button" disabled>修改</button>
			<button id="btnDelete" class="btn black" type="button" disabled>删除</button>
			<button id="btnAssign" class="btn black" type="button" disabled>分配节点</button>
		</div>
		<p>
		<div class="row-fluid">
			<table id="configure-service-table" data-toggle="table" data-locale="zh-CN">
				<thead>
				<tr>
                    <th data-field="state" data-checkbox="true" />
					<th data-field="id" data-visible="false">ID</th>
					<th data-field="name" data-width="100" data-align="center" data-valign="middle" data-sortable="true">任务名称</th>
					<th data-field="uri" data-width="100" data-align="center" data-valign="middle" data-sortable="true">服务URI</th>
					<th data-field="options" data-width="100" data-align="center" data-valign="middle" data-sortable="true">参数</th>
					<th data-field="type" data-width="100" data-align="center" data-valign="middle" data-sortable="true" data-formatter="Configure.Service.typeFormatter">请求类型</th>
					<th data-field="description" data-width="100" data-align="center" data-valign="middle" data-sortable="true">任务描述</th>
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
					<label class="control-label" for="name">任务名称</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12 tags" id="name">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="uri">服务URI</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12 tags" id="uri">
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
					<label class="control-label" for="name">任务名称
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="name">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="uri">服务URI
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="uri">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="options">参数</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="options">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="type">请求类型
						<span class="required">*</span>
					</label>
					<div class="controls">
						<select id="type" style="width: 395px;">
							<option value="" selected="selected"></option>
							<option value="0">GET</option>
							<option value="1">POST</option>
						</select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="description">任务描述</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="description">
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
<div id="dlgAssign"></div>
<!-- END PAGE CONTENT-->