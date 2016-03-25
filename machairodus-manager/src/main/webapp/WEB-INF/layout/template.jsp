<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<!DOCTYPE html>

<!--[if IE 8]> <html lang="zh-CN" class="ie8"> <![endif]-->
<!--[if IE 9]> <html lang="zh-CN" class="ie9"> <![endif]-->
<!--[if !IE]><!--> <html lang="zh-CN"> <!--<![endif]-->

<!-- BEGIN HEAD -->
<head>
	<tiles:insertAttribute name="meta" />
	<title>${title}</title>
	<tiles:insertAttribute name="css" />
</head>
<!-- END HEAD -->

<!-- BEGIN BODY -->
<body class="page-header-fixed">
	<tiles:insertAttribute name = "header" />

	<!-- BEGIN CONTAINER -->
	<div class="page-container row-fluid">
		<tiles:insertAttribute name = "sidebar" />
		<!-- BEGIN PAGE -->
		<div class="page-content">
			<tiles:insertAttribute name = "navigate" />
			<tiles:insertAttribute name = "content" />
		</div>
	</div>
	<!-- END CONTAINER -->

	<!-- BEGIN JAVASCRIPTS(Load javascripts at bottom, this will reduce page load time) -->
	<tiles:insertAttribute name = "script" />
	<tiles:insertAttribute name = "content.js" />
	<tiles:insertAttribute name = "footer" />
</body>
<!-- END BODY -->

</html>