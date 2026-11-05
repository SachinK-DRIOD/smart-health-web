<%@ page isErrorPage="true" %>
<h1>Something went wrong</h1>
<section class="card">
  <c:choose>
    <c:when test="${pageContext.errorData.statusCode eq 404}"><p>We could not find that page.</p></c:when>
    <c:when test="${pageContext.errorData.statusCode eq 403}"><p>You do not have access to that page, or your session expired.</p></c:when>
    <c:otherwise><p>An unexpected error occurred. Try again, and if it continues, contact the clinic.</p></c:otherwise>
  </c:choose>
  <a class="btn" href="${ctx}/dashboard">Back to dashboard</a>
</section>
