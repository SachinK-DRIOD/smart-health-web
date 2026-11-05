<h1>Book an appointment</h1>
<form method="post" action="${ctx}/appointment/book" class="card">
  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
  <fieldset>
    <legend>Doctor</legend>
    <div class="doctors">
      <c:forEach var="d" items="${doctors}">
        <label class="doc">
          <input type="radio" name="doctorId" value="${d.id}" required ${param.doctorId eq d.id ? 'checked' : ''}>
          <span><strong><c:out value="${d.user.name}"/></strong><br>
            <c:out value="${d.specialization}"/>
            <c:if test="${d.specialization eq recommended}"><em class="tag">Suggested for you</em></c:if></span>
        </label>
      </c:forEach>
    </div>
  </fieldset>
  <div class="row">
    <label>Date <input type="date" name="date" min="${minDate}" required value="<c:out value='${param.date}'/>"></label>
    <label>Time
      <select name="slot" required>
        <option value="">Choose</option>
        <c:forEach var="t" items="${slots}"><option value="${t}" ${param.slot eq t ? 'selected' : ''}>${t}</option></c:forEach>
      </select>
    </label>
  </div>
  <button type="submit" class="btn">Confirm booking</button>
</form>
