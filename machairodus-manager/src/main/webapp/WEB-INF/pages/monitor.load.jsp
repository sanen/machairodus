<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link href="${pageContext.request.contextPath}/media/css/api/easyui.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/media/css/api/select2.min.css" type="text/css" rel="stylesheet">
<style>
.select2-container {
	box-sizing: border-box;
    display: inline-block;
    margin: 0;
    position: relative;
    vertical-align: middle;
    z-index: 100;
}

.panel {
    border: 0 none;
    border-radius: 0;
    margin: 0 5px 0 0;
    overflow: hidden;
    text-align: left;
}
</style>
<!-- BEGIN PAGE CONTENT-->          
<div class="container-fluid">
	<div class="row-fluid" style="padding-left: 5px;">
		<div class="control-group">
			<span class="controls margin_right_5 float-left">
				<select id="node" style="width: 500px;">
					<option value="" selected="selected"></option>
				</select>
			</span>
			<a id="bar" href="#" style="outline: medium none; overflow: hidden; text-align: center; text-decoration: none; vertical-align: middle;">
				<span class="float-left" style="margin-left: 5px; margin-top:5px;">
					<label for="chkCpu" class="margin_right_5 float-left">CPU</label>
					<input id="chkCpu" type="checkbox" checked="checked">
				</span>
				<span class="float-left" style="margin-left: 5px; margin-top:5px;">
					<label for="chkMemory" class="margin_right_5 float-left">Memory</label>
					<input id="chkMemory" type="checkbox" checked="checked">
				</span>
				<span class="float-left" style="margin-left: 5px; margin-top:5px;">
					<label for="chkClasses" class="margin_right_5 float-left">Classes</label>
					<input id="chkClasses" type="checkbox" checked="checked">
				</span>
				<span class="float-left" style="margin-left: 5px; margin-top:5px;">
					<label for="chkThreads" class="margin_right_5 float-left">Threads</label>
					<input id="chkThreads" type="checkbox" checked="checked">
				</span>
			</a>
		</div>
	</div>
	<p>
	<div id="center" class="row-fluid" style="height: 600px;">
		<div id="area1" style="width: 100%; height: 50%; margin-left: 5px; margin-top: 5px;">
			<div id="pnlCpu" class="easyui-panel" style="width: 49.5%; height: 100%;" data-options="cls: 'float_left margin_right_5',closable:true" title="CPU">
				<div id="viewCpu"></div>
			</div>
			<div id="pnlMemory" class="easyui-panel" style="width: 49.5%; height: 100%;" data-options="cls: 'float_left margin_right_5',closable:true" title="Memory">
				<div id="viewMemory"></div>
			</div>
		</div>
		<div id="area2" style="width: 100%; height: 50%; margin-left: 5px; margin-top: 5px;">
			<div id="pnlClasses" class="easyui-panel" style="width: 49.5%; height: 100%;" data-options="cls: 'float_left margin_right_5',closable:true" title="Classes">
				<div id="viewClasses"></div>
			</div>
			<div id="pnlThreads" class="easyui-panel" style="width: 49.5%; height: 100%;" data-options="cls: 'float_left margin_right_5',closable:true" title="Threads">
				<div id="viewThreads"></div>
			</div>
		</div>
	</div>
</div>
<!-- END PAGE CONTENT-->