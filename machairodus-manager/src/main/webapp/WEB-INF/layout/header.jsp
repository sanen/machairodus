<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<!-- BEGIN HEADER -->
<div class="header navbar navbar-inverse navbar-fixed-top">
	<!-- BEGIN TOP NAVIGATION BAR -->
	<div class="navbar-inner">
		<div class="container-fluid">
			<!-- BEGIN LOGO -->
			<a class="brand" href="index">
				<img src="${pageContext.request.contextPath}/media/image/logo.png" alt="logo" />
			</a>
			<!-- END LOGO -->

			<!-- BEGIN RESPONSIVE MENU TOGGLER -->
			<a href="javascript:;" class="btn-navbar collapsed" data-toggle="collapse" data-target=".nav-collapse">
				<img src="${pageContext.request.contextPath}/media/image/menu-toggler.png" alt="" />
			</a>          
			<!-- END RESPONSIVE MENU TOGGLER -->            

			<!-- BEGIN TOP NAVIGATION MENU -->              
			<ul class="nav pull-right">
				<!-- BEGIN NOTIFICATION DROPDOWN -->   
				<li class="dropdown" id="header_notification_bar">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown">
						<i class="icon-warning-sign"></i>
						<!-- <span class="badge">0</span> -->
					</a>
					<ul class="dropdown-menu extended notification">
						<li>
							<p>您有{num}个新的通知</p>
						</li>
						<li>
							<a href="#">
								<span class="label label-success"><i class="icon-plus"></i></span>
								通知样例
								<span class="time">Just now</span>
							</a>
						</li>
						<li class="external">
							<a href="#">查看所有通知 <i class="m-icon-swapright"></i></a>
						</li>
					</ul>
				</li>
				<!-- END NOTIFICATION DROPDOWN -->

				<!-- BEGIN INBOX DROPDOWN -->
				<li class="dropdown" id="header_inbox_bar">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown">
					<i class="icon-envelope"></i>
					<!-- <span class="badge">0</span> -->
					</a>
					<ul class="dropdown-menu extended inbox">
						<li>
							<p>您有{num}个新的消息</p>
						</li>
						<li>
							<a href="inbox.html?a=view">
								<span class="subject">
									<span class="from">{发件人}</span>
									<span class="time">{时间}</span>
								</span>
								<span class="message">
								{消息内容}
								</span>  
							</a>
						</li>
						<li class="external">
							<a href="inbox.html">查看所有消息 <i class="m-icon-swapright"></i></a>
						</li>
					</ul>
				</li>
				<!-- END INBOX DROPDOWN -->

				<!-- BEGIN TODO DROPDOWN -->
				<li class="dropdown" id="header_task_bar">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown">
					<i class="icon-tasks"></i>
					<!-- <span class="badge">0</span> -->
					</a>
					<ul class="dropdown-menu extended tasks">
						<li>
							<p>您有{num}个在执行的任务</p>
						</li>
						<li>
							<a href="#">
								<span class="task">
									<span class="desc">{任务名}</span>
									<span class="percent">{进度}</span>
								</span>
								<span class="progress progress-success ">
									<!-- 进度: width -->
									<span style="width: 30%;" class="bar"></span>
								</span>
							</a>
						</li>
						<li class="external">
							<a href="#">查看所有任务 <i class="m-icon-swapright"></i></a>
						</li>
					</ul>
				</li>
				<!-- END TODO DROPDOWN -->

				<!-- BEGIN USER LOGIN DROPDOWN -->
				<li class="dropdown user" style="padding-top: 5px;">
					<a href="#" class="dropdown-toggle" data-toggle="dropdown">
						<span class="username"><shiro:principal/></span>
						<i class="icon-angle-down"></i>
					</a>
					<ul class="dropdown-menu">
						<li><a href="javascript:;"><i class="icon-user"></i> 个人档案</a></li>
						<li><a href="javascript:;"><i class="icon-calendar"></i> 日历</a></li>
						<li><a href="javascript:;"><i class="icon-envelope"></i> 收件箱</a></li>
						<li><a href="javascript:;"><i class="icon-tasks"></i> 任务</a></li>
						<li class="divider"></li>
						<li><a href="javascript:;"><i class="icon-lock"></i> 锁定</a></li>
						<li><a href="${pageContext.request.contextPath}/permissions/users/logout"><i class="icon-key"></i> 退出</a></li>
					</ul>
				</li>
				<!-- END USER LOGIN DROPDOWN -->
			</ul>
			<!-- END TOP NAVIGATION MENU --> 
		</div>
	</div>
	<!-- END TOP NAVIGATION BAR -->
</div>
<!-- END HEADER -->