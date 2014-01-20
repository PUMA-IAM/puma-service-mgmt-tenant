<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="includes/header.jsp" />

<div class="jumbotron">
	<div class="container">
		<h1>PUMA Management for ${tenant.name}</h1>
	</div>
</div>

<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h1 class="page-header">Basic Management</h1>
		</div>
	</div>
	<div class="row">
		<div class="col-md-4">
			<a class="btn btn-default btn-lg" role="button" href="<c:url value="/configuration/${tenant.id}"/>">Configuration &raquo;</a>
		</div>
		<div class="col-md-4">
			<a class="btn btn-default btn-lg" role="button" href="<c:url value="/policy/${tenant.id}"/>">Policies &raquo;</a>
		</div>
		<div class="col-md-4">
			<a class="btn btn-default btn-lg" role="button" href="<c:url value="/configuration/info/${tenant.id}"/>">Subtenants &raquo;</a>
		</div>
	</div>
	<c:choose>
		<c:when test="${local}">
			<div class="row">
				<div class="col-md-12">
					<h1 class="page-header">User management</h1>
				</div>
			</div>
			<div class="row">
				<div class="col-md-4">
					<a class="btn btn-default btn-lg" role="button" href="<c:url value="/users/${tenant.id}"/>">Users &raquo;</a>
				</div>
				<div class="col-md-4">
					<a class="btn btn-default btn-lg" role="button" href="<c:url value="/attributes/${tenant.id}"/>">Attribute families &raquo;</a>
				</div>
				<div class="col-md-4">
					<a class="btn btn-default btn-lg" role="button" href="<c:url value="/groups/${tenant.id}"/>">Groups &raquo;</a>
				</div>
			</div>
		</c:when>
		<c:otherwise>
		</c:otherwise>
	</c:choose>
</div>

<jsp:include page="includes/footer.jsp" />
