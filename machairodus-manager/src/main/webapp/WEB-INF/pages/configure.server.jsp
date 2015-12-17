<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link href="${pageContext.request.contextPath}/media/css/api/bootstrap-table.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/easyui.css" rel="stylesheet" type="text/css"/>
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
			<table id="configure-server-table" data-toggle="table" data-locale="zh-CN">
				<thead>
				<tr>
                    <th data-field="state" data-checkbox="true" />
					<th data-field="id" data-visible="false">ID</th>
					<th data-field="name" data-width="100" data-align="center" data-valign="middle" data-sortable="true">名称</th>
					<th data-field="address" data-width="100" data-align="center" data-valign="middle" data-sortable="true">地址</th>
					<th data-field="username" data-width="100" data-align="center" data-valign="middle" data-sortable="true">用户名</th>
					<th data-field="passwd" data-width="100" data-align="center" data-valign="middle" data-sortable="true">密码</th>
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
					<label class="control-label" for="name">名称</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="name">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="address">地址</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="address">
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
					<label class="control-label" for="name">名称
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="name">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="address">地址
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="address">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="username">用户名
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="text" placeholder="" class="m-wrap span12" id="username">
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="passwd">密码
						<span class="required">*</span>
					</label>
					<div class="controls">
						<input type="password" placeholder="" class="m-wrap span12" id="passwd">
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