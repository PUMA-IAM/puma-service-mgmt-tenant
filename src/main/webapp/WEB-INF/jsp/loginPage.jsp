<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="includes/header.jsp" />

<div class="jumbotron">
	<div class="container">
		<h1>PUMA management</h1>
	</div>
</div>

<div class="container">
	<c:choose>
		<c:when test="${empty tenantId}">
			You have to be logged in to access this page. Please <a href="<c:url value="/login/proceed"/>">log in</a> first.
		</c:when>
		<c:otherwise>
			You have to be logged in to access this page. Please <a href="<c:url value="/login/${tenantId}"/>">log in</a> first.
		</c:otherwise>
	</c:choose>
</div>

<jsp:include page="includes/footer.jsp" />