<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>

<jsp:include page="../includes/header.jsp" />

<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h1 class="page-header">Configure tenant</h1>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<form class="form-horizontal" role="form" method="post"
				action="<c:url value="/configuration/${tenant.id}/modify-impl"/>">
				<div class="form-group">
					<label for="input-name" class="col-sm-2 control-label">Name</label>
					<div class="col-sm-10">
						<input name="name" class="form-control" id="input-name"
							value="${tenant.name}">
					</div>
				</div>
				<div class="form-group">
					<label for="input-mgmt-type" class="col-sm-2 control-label">Management
						type</label>
					<div class="col-sm-10">
						<c:choose>
							<c:when test="${empty managementValues}">
							</c:when>
							<c:otherwise>
								<c:forEach var="mgmtValue" items="${managementValues}" varStatus="status">
									<div class="radio">
										<label>
											<input type="radio" name="mgmt-type" id="mgmt-type-option-${mgmtValue}" value="${mgmtValue}">
												${mgmtValue.description}
										</label>
									</div>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
				<div class="form-group">
					<label for="input-authn-endpoint" class="col-sm-2 control-label">IdP
						end-point</label>
					<div class="col-sm-10">
						<input name="authn-endpoint" class="form-control"
							id="input-authn-endpoint" value="${tenant.authnRequestEndpoint}" disabled>
					</div>
				</div>
				<div class="form-group">
					<label for="input-authn-endpoint" class="col-sm-2 control-label">IdP
						public key</label>
					<div class="col-sm-10">
						<input name="idp-public-key" class="form-control"
							id="input-idp-public-key" value="${tenant.identityProviderPublicKey}" disabled>
					</div>
				</div>
				<div class="form-group">
					<label for="input-attr-endpoint" class="col-sm-2 control-label">Attribute
						service end-point</label>
					<div class="col-sm-10">
						<input name="attr-endpoint" class="form-control"
							id="input-attr-endpoint" value="${tenant.attrRequestEndpoint}" disabled>
					</div>
				</div>
				<div class="form-group">
					<label for="input-authz-endpoint" class="col-sm-2 control-label">Authorization
						end-point</label>
					<div class="col-sm-10">
						<input name="authz-endpoint" class="form-control"
							id="input-authz-endpoint" value="${tenant.authzRequestEndpoint}" disabled>
					</div>
				</div>

				<div class="form-group">
					<label for="exampleInputFile" class="col-sm-2 control-label">New logo</label>
					<div class="col-sm-10">
						<input name="file" type="file" id="input-logo">
					</div>
				</div>

				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<button type="submit" class="btn btn-default">Modify
							tenant</button>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>

<script>
	(function($) {
		$(function() {
			$('input:radio[name="mgmt-type"]').change(function() {
				if ($(this).val() == "Locally") {
					$("#input-authn-endpoint").prop('disabled', true);
					$("#input-idp-public-key").prop('disabled', true);
					$("#input-attr-endpoint").prop('disabled', true);
					$("#input-authz-endpoint").prop('disabled', true);
				} else if ($(this).val() == "FederatedAuthentication") {
					$("#input-authn-endpoint").prop('disabled', false);
					$("#input-idp-public-key").prop('disabled', false);
					$("#input-attr-endpoint").prop('disabled', false);
					$("#input-authz-endpoint").prop('disabled', true);
				} else { // fedauthz
					$("#input-authn-endpoint").prop('disabled', false);
					$("#input-idp-public-key").prop('disabled', false);
					$("#input-attr-endpoint").prop('disabled', false);
					$("#input-authz-endpoint").prop('disabled', false);
				}
			});
		});
	})(jQuery);
</script>

<jsp:include page="../includes/footer.jsp" />