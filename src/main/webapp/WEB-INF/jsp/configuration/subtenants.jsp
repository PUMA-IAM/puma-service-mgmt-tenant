<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>

<jsp:include page="../includes/header.jsp" />

<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h1 class="page-header">
				<c:out value="Subtenants" />
			</h1>
			<c:choose>
				<c:when test="${empty tenant.subtenants}">No subtenants were added yet.</c:when>
				<c:otherwise>
					<table class="table table-hover">
						<thead>
							<tr>
								<th>Name</th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="subtenant" items="${tenant.subtenants}"
								varStatus="status">
								<tr>
									<td><c:out value="${subtenant.name}" /></td>
									<td>
								<a class="btn btn-danger btn-sm"
								href="<c:url value="/configuration/${tenant.id}/${subtenant.id}/delete"/>"><span
									class="glyphicon glyphicon-chevron-right"></span> Delete</a></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</c:otherwise>
			</c:choose>
		</div>
	</div>

	<div class="row">
		<div class="col-md-12">
			<h1 class="page-header">New subtenant</h1>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<form class="form-horizontal" role="form" method="post"
				action="<c:url value="/configuration/${tenant.id}/create-impl"/>">
				<div class="form-group">
					<label for="input-tname" class="col-sm-2 control-label">Name</label>
					<div class="col-sm-10">
						<input name="tenantName" class="form-control" id="input-tname"
							placeholder="Name">
					</div>
				</div>
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">Administrator name</label>
					<div class="col-sm-10">
						<input name="userName" class="form-control" id="input-name"
							placeholder="Name">
					</div>
				</div>
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">Administrator Password</label>
					<div class="col-sm-10">
						<input name="password" class="form-control" id="input-name"
							placeholder="Password">
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<button type="submit" class="btn btn-default">Create
							subtenant</button>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>

<jsp:include page="../includes/footer.jsp" />