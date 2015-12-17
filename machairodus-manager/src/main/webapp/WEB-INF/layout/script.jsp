<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- BEGIN CORE PLUGINS -->
<script src="${pageContext.request.contextPath}/media/js/api/jquery-1.10.1.min.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/media/js/api/jquery-migrate-1.2.1.min.js" type="text/javascript"></script>
<!-- IMPORTANT! Load jquery-ui-1.10.1.custom.min.js before bootstrap.min.js to fix bootstrap tooltip conflict with jquery ui tooltip -->
<script src="${pageContext.request.contextPath}/media/js/api/jquery-ui-1.10.1.custom.min.js" type="text/javascript"></script>      
<script src="${pageContext.request.contextPath}/media/js/api/bootstrap.min.js" type="text/javascript"></script>
<!--[if lt IE 9]>
<script src="${pageContext.request.contextPath}/media/js/api/excanvas.min.js"></script>
<script src="${pageContext.request.contextPath}/media/js/api/respond.min.js"></script>  
<![endif]-->   
<script src="${pageContext.request.contextPath}/media/js/api/jquery.slimscroll.min.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/media/js/api/jquery.blockui.min.js" type="text/javascript"></script>  
<script src="${pageContext.request.contextPath}/media/js/api/jquery.cookie.min.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/media/js/api/jquery.uniform.min.js" type="text/javascript" ></script>
<!-- END CORE PLUGINS -->

<!-- APP INIT -->
<script src="${pageContext.request.contextPath}/media/js/app.js"></script>      
<script>
	var context = "${pageContext.request.contextPath}";
	var dialogExtend = function() {
		$('div.window-mask').bind('click', function() {
			$.each(['div.window-mask', 'div.panel.window', 'div.window-shadow'], function(idx, element) {
				$.each($(element), function(itemIdx, item) {
					if($(item).css('display') && $(item).css('display') == 'block')
						$(item).css({display: 'none'});
				});
			});
		});
	};
	
	jQuery(document).ready(function() {       
	   App.init();
	});
</script>
<!-- END APP INIT -->