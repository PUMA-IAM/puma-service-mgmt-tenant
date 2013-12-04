<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>

<jsp:include page="../includes/header.jsp" />

<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h1 class="page-header">
				<c:out value="Attribute families" />
			</h1>
			<c:choose>
				<c:when test="${empty families}">No families were added yet.</c:when>
				<c:otherwise>
					<table class="table table-hover">
						<thead>
							<tr>
								<th>Family</th>
								<th>Datatype</th>
								<th>Multiplicity</th>
								<th>Defined By</th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="family" items="${families}"
								varStatus="status">
								<tr>
									<td><c:out value="${family.name}" /></td>
									<td><c:out value="${family.dataType}" /></td>
									<td><c:out value="${family.multiplicity}" /></td>
									<td><c:out value="${family.definedBy.name}" /></td>
									<td>
									<c:choose>
										<c:when test="${family.definedBy.id eq tenant.id or tenant.name eq 'provider'}">
										<a class="btn btn-danger btn-sm"
								href="<c:url value="/attributes/${tenant.id}/${family.id}/delete"/>"><span
									class="glyphicon glyphicon-chevron-right"></span> Delete</a>
										</c:when>
										<c:otherwise>
								
										</c:otherwise>
									</c:choose>
									</td>
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
			<h1 class="page-header">Create Attribute Family</h1>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<form class="form-horizontal" role="form" method="post"
				action="<c:url value="/attributes/${tenant.id}/create-impl"/>">
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">Name</label>
					<div class="col-sm-10">
						<input name="name" class="form-control" id="input-name"
							placeholder="Attribute Family Name">
					</div>
				</div>
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">XACML identifier</label>
					<div class="col-sm-10">
						<input name="xacmlid" class="form-control" id="input-name"
							placeholder="">
					</div>
				</div>
				<div class="form-group">
						<label for="input-name" class="col-sm-2 control-label">Datatype</label>
						<div class="controls">
							<select name="datatype">
								<c:forEach items="${datatypes}" var="datatype">
									<option value="${datatype}">${datatype}</option>
								</c:forEach>
							</select>
						</div>
				</div>				
				<div class="form-group">
						<label for="input-name" class="col-sm-2 control-label">Multiplicity</label>
						<div class="controls">
							<select name="multiplicity">
								<c:forEach items="${multiplicityValues}" var="multiplicityValue">
									<option value="${multiplicityValue}">${multiplicityValue}</option>
								</c:forEach>
							</select>
						</div>
				</div>				
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<button type="submit" class="btn btn-default">Create
							attribute family</button>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>

<jsp:include page="../includes/footer.jsp" />