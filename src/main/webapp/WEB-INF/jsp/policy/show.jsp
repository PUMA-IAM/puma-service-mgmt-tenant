<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>

<jsp:include page="../includes/header.jsp" />

<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h1>Policy</h1>
			<c:choose>
				<c:when test="${empty policies}">No policies were added yet.</c:when>
				<c:otherwise>
					<table class="table table-hover">
						<thead>
							<tr>
								<th>Policy id</th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="selectedPolicy" items="${policies}"
								varStatus="status">
								<tr>
									<td><label>
									<input type="checkbox" onchange="checkboxListener.call(this, ${tenant.id}, '${selectedPolicy.id}')" <c:if test="${selectedPolicy.enabled}">checked</c:if> />
									<c:out value="${selectedPolicy.id}" />
									</label></td>
									<td><a class="btn btn-primary btn-sm"
											href="<c:url value="/policy/${tenant.id}/info/${selectedPolicy.id}"/>"><span
									class="glyphicon glyphicon-chevron-right"></span> View details</a>
								<a class="btn btn-danger btn-sm"
								href="<c:url value="/policy/${tenant.id}/${selectedPolicy.id}/delete"/>"><span
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
			<h2>Current</h2>
			<pre class="prettyprint linenums"><c:out value="${policySet}" /></pre>
		</div>
	</div>

	<div class="row">
		<div class="col-md-12">
			<h2>Create new policy</h2>
			<form role="form" method="post"
				action="<c:url value="/policy/${tenant.id}/create-impl"/>">
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">Policy id</label>
					<div class="col-sm-10">
						<input name="id" class="form-control" id="input-name"
							placeholder="id">
					</div>
				</div>
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">Specification</label>
					<div class="col-sm-10">					
					<textarea class="form-control" style="width: 100%; height: 500px;"
						name="policy"><c:out value="${skeleton}" /></textarea>
					</div>
				</div>
				<div class="form-group">
					<button type="submit" class="btn btn-default">Add to policy set</button>
				</div>
			</form>
		</div>
	</div>
</div>

<script type="text/javascript">
function checkboxListener(tenantId, policyId) {
	location.href = "/mgmt/tenant/policy/" + tenantId + "/" + policyId + (this.checked ? "/enable" : "/disable")
}
</script>
<jsp:include page="../includes/footer.jsp" />