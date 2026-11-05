<h1>Hello, <c:out value="${sessionScope.user.name}"/></h1>
<ol class="steps" aria-label="Visit progress">
  <c:forEach var="s" items="${steps}" varStatus="st">
    <li class="${st.count lt step ? 'done' : (st.count eq step ? 'now' : '')}"><c:out value="${s}"/></li>
  </c:forEach>
</ol>
<section class="card">
  <c:choose>
    <c:when test="${step eq 1}">
      <h2>Start with your details</h2>
      <p>Tell us your age, contact number and any medical history so the doctor has context.</p>
      <a class="btn" href="${ctx}/patient/form">Add your details</a>
    </c:when>
    <c:when test="${step eq 2}">
      <h2>Check your symptoms</h2>
      <p>Pick what you are feeling and get a suggested condition and the specialist to see.</p>
      <a class="btn" href="${ctx}/symptoms">Check symptoms</a>
    </c:when>
    <c:when test="${step eq 3}">
      <h2>Book your appointment</h2>
      <p>Your last result suggests <strong><c:out value="${record.specialist}"/></strong>.</p>
      <a class="btn" href="${ctx}/appointment/book">Book appointment</a>
      <a class="btn ghost" href="${ctx}/result">View result</a>
    </c:when>
    <c:otherwise>
      <h2>You are booked</h2>
      <p>Download your visit report or manage your appointments.</p>
      <a class="btn" href="${ctx}/appointments">View appointments</a>
      <a class="btn ghost" href="${ctx}/symptoms">New symptom check</a>
    </c:otherwise>
  </c:choose>
</section>
