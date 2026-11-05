<h1>Admin overview</h1>
<h2>Users (<c:out value="${fn:length(users)}"/>)</h2>
<div class="tablewrap"><table>
  <thead><tr><th>Name</th><th>Email</th><th>Role</th></tr></thead>
  <tbody><c:forEach var="u" items="${users}">
    <tr><td><c:out value="${u.name}"/></td><td><c:out value="${u.email}"/></td><td><c:out value="${u.role}"/></td></tr>
  </c:forEach></tbody>
</table></div>
<h2>All appointments (<c:out value="${fn:length(appointments)}"/>)</h2>
<div class="tablewrap"><table>
  <thead><tr><th>Date</th><th>Time</th><th>Patient</th><th>Doctor</th><th>Status</th><th>Reminder</th></tr></thead>
  <tbody><c:forEach var="a" items="${appointments}">
    <tr>
      <td><fmt:formatDate value="${a.appointmentDate}" pattern="d MMM yyyy"/></td>
      <td><c:out value="${a.timeSlot}"/></td>
      <td><c:out value="${a.patient.user.name}"/></td>
      <td><c:out value="${a.doctor.user.name}"/></td>
      <td><span class="status ${fn:toLowerCase(a.status)}"><c:out value="${a.status}"/></span></td>
      <td>${a.reminderSent ? 'Sent' : 'Pending'}</td>
    </tr>
  </c:forEach></tbody>
</table></div>
