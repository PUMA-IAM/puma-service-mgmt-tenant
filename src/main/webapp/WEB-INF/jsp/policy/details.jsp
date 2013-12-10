<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions'%>

<jsp:include page="../includes/header.jsp" />

<div class="container">
	<div class="row">
		<div class="col-md-12">
			<h2>Contents of ${policy.id}</h2>
			<pre class="prettyprint linenums"><c:out value="${policy.content}" /></pre>
		</div>
	</div>
	<div class="row">
		<a href="<c:url value="/policy/${tenant.id}"/>">Back to main policy set</a>
	</div>
</div>

<jsp:include page="../includes/footer.jsp" />