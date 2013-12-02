<!DOCTYPE html>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<meta charset="utf-8">
<title><c:out value="${title}" /></title>

<meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<!-- Bootstrap -->
<link href="<c:url value="/resources/css/bootstrap.css"/>"
	rel="stylesheet" media="screen" />
<link href="<c:url value="/resources/css/own-style.css"/>"
	rel="stylesheet" />
<link href="<c:url value="/resources/css/prettify.css"/>"
	rel="stylesheet" />

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="/resources/js/html5shiv.js"></script>
      <script src="/resources/js/respond.min.js"></script>
    <![endif]-->
</head>

<body>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="<c:url value="/resources/js/jquery.js"/>"></script>

	<div class="navbar navbar-default navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse"
					data-target=".navbar-collapse">
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="<c:url value="/${tenant.id}"/>">PUMA Tenant Management</a>
			</div>
			<!--
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li><a href="<c:url value="/tenants"/>">Tenants</a></li>
					<li><a href="<c:url value="/application-pdps"/>">Application PDPs</a></li>
					<li><a href="<c:url value="/central-puma-pdp"/>">Central PUMA PDP</a></li>
				</ul>

<%-- 				<c:choose> --%>
<%-- 					<c:when test="${empty user}"> --%>
<!-- 						<div class="navbar-form navbar-right"> -->
<!-- 							<a type="submit" class="btn btn-success" -->
<%-- 								href="<c:url value="/user/login"/>">Log in</a> --%>
<!-- 						</div> -->
<%-- 					</c:when> --%>
<%-- 					<c:otherwise> --%>
<!-- 						<ul class="nav navbar-nav navbar-right"> -->
<%-- 							<li class=""><a href="<c:url value="/docs"/>">View --%>
<!-- 									documents</a></li> -->
<%-- 							<li class=""><a href="<c:url value="/docs/create"/>">Create --%>
<!-- 									document</a></li> -->
<!-- 							<li class="dropdown"><a href="#" class="dropdown-toggle" -->
<%-- 								data-toggle="dropdown">Hello, <c:out value="${user}" /> <b --%>
<!-- 									class="caret"></b></a> -->
<!-- 								<ul class="dropdown-menu"> -->
<%-- 									<li><a href="<c:url value="/user/logout"/>">Log out</a></li> --%>
<!-- 								</ul></li> -->
<!-- 						</ul> -->
<%-- 					</c:otherwise> --%>
<%-- 				</c:choose> --%>
			<!-- </div>
			-->
			<!--/.nav-collapse -->
		</div>
	</div>


	<div class="container">
		<c:forEach var="msg" items="${msgs}" varStatus="status">
			<c:choose>
				<c:when test="${msg.type == 'success'}">
					<c:set var="alertClass" value="alert-success" />
				</c:when>
				<c:when test="${msg.type == 'failure'}">
					<c:set var="alertClass" value="alert-danger" />
				</c:when>
				<c:otherwise>
					<c:set var="alertClass" value="alert-info" />
				</c:otherwise>
			</c:choose>
			<div class="alert <c:out value="${alertClass}" /> alert-dismissable">
				<button type="button" class="close" data-dismiss="alert"
					aria-hidden="true">&times;</button>
				<c:out value="${msg.message}" />
			</div>
		</c:forEach>
	</div>