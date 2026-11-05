<h1>Your result</h1>
<section class="card">
  <p class="muted">You reported: <c:out value="${record.symptoms}"/></p>
  <c:choose>
    <c:when test="${not empty record.predictedDisease}">
      <h2 class="big"><c:out value="${record.predictedDisease}"/></h2>
      <fmt:formatNumber var="pct" value="${record.confidence * 100}" maxFractionDigits="0" groupingUsed="false"/>
      <div class="bar" role="img" aria-label="Model confidence ${pct} percent"><span style="width:${pct}%"></span></div>
      <p class="muted">Model confidence: ${pct}%. This is an estimate, not a diagnosis.</p>
    </c:when>
    <c:otherwise>
      <h2 class="big">No prediction available</h2>
      <p>The prediction service did not respond, so we could not estimate a condition. Your symptoms are saved and you can still book a visit.</p>
    </c:otherwise>
  </c:choose>
  <p>Suggested specialist: <strong><c:out value="${record.specialist}"/></strong></p>
  <a class="btn" href="${ctx}/appointment/book">Book with a doctor</a>
  <a class="btn ghost" href="${ctx}/symptoms">Change symptoms</a>
</section>
