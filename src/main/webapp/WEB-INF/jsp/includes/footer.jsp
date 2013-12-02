<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="container">
	<hr>

	<footer>
		<p>&copy; eDocs Inc. 2013</p>
	</footer>
</div>

<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="<c:url value="/resources/js/bootstrap.js"/>"></script>

<script src="<c:url value="/resources/js/prettify.js"/>"></script>
<script>
// make code pretty
window.prettyPrint && prettyPrint()
</script>

</body>
</html>