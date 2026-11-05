<h1>Your details</h1>
<form method="post" action="${ctx}/patient/form" class="card narrow">
  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
  <label>Age <input type="number" name="age" min="0" max="120" required value="<c:out value='${p.age}'/>"></label>
  <label>Gender
    <select name="gender" required>
      <option value="">Choose</option>
      <c:forEach var="g" items="${genders}"><option value="${g}" ${p.gender eq g ? 'selected' : ''}><c:out value="${g}"/></option></c:forEach>
    </select>
  </label>
  <label>Phone <input type="tel" name="phone" required maxlength="20" value="<c:out value='${p.phone}'/>"></label>
  <label>Medical history, allergies and current medicines
    <textarea name="history" rows="5" maxlength="2000"><c:out value="${p.medicalHistory}"/></textarea></label>
  <button type="submit" class="btn">Save and continue</button>
</form>
