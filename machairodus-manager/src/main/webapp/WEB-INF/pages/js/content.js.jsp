<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.machairodus.manager.util.Definition" %>
<% 
String _definition;
if((_definition = (String) request.getAttribute("definition")) == null) { %>
	<script src="${pageContext.request.contextPath}/media/js/index.js"></script>
<% } else { 
	Definition definition = Definition.value(_definition);
	switch(definition) {
		case INDEX: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/index.js"></script>
			<%
			break;
		case PERMISSION_USER:
			%>
			<script src="${pageContext.request.contextPath}/media/js/permission.user.js"></script>
			<%
			break;
		case PERMISSION_ROLE: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/permission.role.js"></script>
			<%
			break;
		case PERMISSION_FUNC: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/permission.func.js"></script>
			<%
			break;
		case CONFIGURE_SERVER:
			%>
			<script src="${pageContext.request.contextPath}/media/js/api/jquery.tagsinput.js"></script>
			<script src="${pageContext.request.contextPath}/media/js/api/bootstrap-table.js"></script>
			<%-- <script src="${pageContext.request.contextPath}/media/js/api/bootstrap-table-zh-CN.js"></script> --%>
			<script src="${pageContext.request.contextPath}/media/js/api/jquery.easyui.min.js"></script>
			<script src="${pageContext.request.contextPath}/media/js/configure.server.js"></script>
			<%
			break;
		case CONFIGURE_NODE: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/api/jquery.tagsinput.js"></script>
			<script src="${pageContext.request.contextPath}/media/js/api/bootstrap-table.js"></script>
			<%-- <script src="${pageContext.request.contextPath}/media/js/api/bootstrap-table-zh-CN.js"></script> --%>
			<script src="${pageContext.request.contextPath}/media/js/api/jquery.easyui.min.js"></script>
			<script src="${pageContext.request.contextPath}/media/js/api/select2.js"></script>
			<script src="${pageContext.request.contextPath}/media/js/configure.node.js"></script>
			<%
			break;
		case CONFIGURE_SERVICE: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/configure.service.js"></script>
			<%
			break;
		case SCHEDULE_BALANCER: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/schedule.balancer.js"></script>
			<%
			break;
		case SCHEDULE_SCHEDULER: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/schedule.scheduler.js"></script>
			<%
			break;
		case MONITOR_LOAD: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/monitor.load.js"></script>
			<%
			break;
		case STATISTICS_SCHEDULER: 
			%>
			<script src="${pageContext.request.contextPath}/media/js/statistics.scheduler.js"></script>
			<%
			break;
		default: 
			break;
	}
} %>
