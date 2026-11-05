<h1>Your appointments</h1>
<c:choose>
  <c:when test="${empty appointments}">
    <section class="card"><p>You have no appointments yet.</p>
      <a class="btn" href="${ctx}/appointment/book">Book appointment</a></section>
  </c:when>
  <c:otherwise>
    <div class="tablewrap"><table>
      <thead><tr><th>Date</th><th>Time</th><th>Doctor</th><th>Status</th><th></th></tr></thead>
      <tbody>
      <c:forEach var="a" items="${appointments}">
        <tr>
          <td><fmt:formatDate value="${a.appointmentDate}" pattern="EEE, d MMM yyyy"/></td>
          <td><c:out value="${a.timeSlot}"/></td>
          <td><c:out value="${a.doctor.user.name}"/><br><small><c:out value="${a.doctor.specialization}"/></small></td>
          <td><span class="status ${fn:toLowerCase(a.status)}"><c:out value="${a.status}"/></span></td>
          <td class="acts">
            <c:if test="${a.status ne 'CANCELLED'}"><a href="${ctx}/report/pdf?id=${a.id}">Download report</a></c:if>
            <c:if test="${a.status eq 'BOOKED'}">
              <form method="post" action="${ctx}/appointment/cancel">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="id" value="${a.id}">
                <button type="submit" class="link danger">Cancel</button>
              </form>
            </c:if>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table></div>
  </c:otherwise>
</c:choose>
