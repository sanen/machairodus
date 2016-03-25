<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link href="${pageContext.request.contextPath}/media/css/api/easyui.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/select2.min.css" type="text/css" rel="stylesheet">

<style>
.form-horizontal .control-label {
	width: 70px;
}
.form-horizontal .controls {
	margin-left: 75px;
}

div.div100 {
	height: 100%; width: 100%;
}

</style>
<!-- BEGIN PAGE CONTENT-->          
<div class="container-fluid">
	<div class="row-fluid">
		<div class="row-fluid">
			<button id="btnAdd" class="btn black" type="button">添加节点</button>
		</div>
	</div>
	<p>
	<div id="monitor" class="row-fluid"></div>
</div>

<div id="dlgOption">
	<div class="modal-body">
		<div class="row-fluid">
			<form class="form-horizontal" action="#">
				<div class="control-group">
					<label class="control-label" for="node">服务节点
						<span class="required">*</span>
					</label>
					<div class="controls">
						<select id="node" style="width: 305px;"></select>
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