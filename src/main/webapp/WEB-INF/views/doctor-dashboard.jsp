<h1>Appointments for <c:out value="${sessionScope.user.name}"/></h1>
<c:if test="${empty appointments}"><section class="card"><p>No appointments are booked with you yet.</p></section></c:if>
<c:forEach var="a" items="${appointments}">
  <section class="card visit ${fn:toLowerCase(a.status)}">
    <div class="head">
      <h2><fmt:formatDate value="${a.appointmentDate}" pattern="EEE, d MMM"/>, <c:out value="${a.timeSlot}"/></h2>
      <span class="status ${fn:toLowerCase(a.status)}"><c:out value="${a.status}"/></span>
    </div>
    <p><strong><c:out value="${a.patient.user.name}"/></strong>, <c:out value="${a.patient.age}"/>, <c:out value="${a.patient.gender}"/>,
       <c:out value="${a.patient.phone}"/></p>
    <c:if test="${not empty a.patient.medicalHistory}"><p class="muted">History: <c:out value="${a.patient.medicalHistory}"/></p></c:if>
    <c:if test="${not empty a.symptomRecord}">
      <p>Symptoms: <c:out value="${a.symptomRecord.symptoms}"/></p>
      <c:if test="${not empty a.symptomRecord.predictedDisease}">
        <p>Model estimate: <c:out value="${a.symptomRecord.predictedDisease}"/>
          (<fmt:formatNumber value="${a.symptomRecord.confidence}" type="percent"/>)</p>
      </c:if>
    </c:if>
    <c:if test="${a.status ne 'CANCELLED'}">
      <form method="post" action="${ctx}/doctor/notes">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="id" value="${a.id}">
        <label>Diagnosis and notes <textarea name="notes" rows="3" maxlength="2000"><c:out value="${a.doctorNotes}"/></textarea></label>
        <label class="inline"><input type="checkbox" name="complete" value="1" ${a.status eq 'COMPLETED' ? 'checked' : ''}> Mark visit as completed</label>
        <button type="submit" class="btn">Save notes</button>
        <a class="btn ghost" href="${ctx}/report/pdf?id=${a.id}">Download report</a>
      </form>
    </c:if>
  </section>
</c:forEach>
