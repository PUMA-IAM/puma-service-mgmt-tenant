<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>

<jsp:include page="../includes/header.jsp" />

<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h1 class="page-header">
				<c:out value="User details" />
			</h1>
			<h3>Basic information</h3>
			<p>
				<b>Name:</b> <c:out value="${selectedUser.loginName}" /> <br />
				<b>Tenant:</b> <c:out value="${selectedUser.tenant.name}" /> <br />
			</p>
			<h3>Attributes</h3>
			<c:choose>
				<c:when test="${empty selectedUserAttributes}">No attributes found for this user.</c:when>
				<c:otherwise>
					<table class="table table-hover">
						<thead>
							<tr>
								<th>Attribute</th>
								<th>Value</th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="attribute" items="${selectedUserAttributes}"
								varStatus="status">
								<tr>
									<td><c:out value="${attribute.family.name}" /></td>
									<td><c:out value="${attribute.value}" /></td>
									<td><a class="btn btn-danger btn-sm"
													href="<c:url value="/attributes/${tenant.id}/${selectedUser.id}/${attribute.id}/delete"/>"><span
															class="glyphicon glyphicon-chevron-right"></span> Delete</a></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	<!-- Assign an attribute to the user -->
	<div class="row">
		<div class="col-md-12">
			<h1 class="page-header">Assign Attribute</h1>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<form class="form-horizontal" role="form" method="post"
				action="<c:url value="/attributes/${tenant.id}/${selectedUser.id}/create-impl"/>">
				<div class="form-group">
						<label for="input-name" class="col-sm-2 control-label">Attribute family</label>
						<div class="controls">
							<select name="familyId" class="form-control">
								<c:forEach items="${families}" var="family">
									<option value="${family.id}">${family.name}</option>
								</c:forEach>
							</select>
						</div>
				</div>
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">Value</label>
					<div class="col-sm-10">
						<input name="value" class="form-control" id="input-name"
							placeholder="Value">
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<button type="submit" class="btn btn-default">Assign attribute</button>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>

<jsp:include page="../includes/footer.jsp" />