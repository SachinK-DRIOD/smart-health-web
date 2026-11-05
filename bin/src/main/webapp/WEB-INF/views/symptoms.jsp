<h1>What are you feeling?</h1>
<form method="post" action="${ctx}/symptoms" class="card">
  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
  <fieldset>
    <legend>Select all that apply</legend>
    <div class="chips">
      <c:forEach var="s" items="${symptomList}">
        <label class="chip"><input type="checkbox" name="symptom" value="${s}"><span><c:out value="${s}"/></span></label>
      </c:forEach>
    </div>
  </fieldset>
  <button type="submit" class="btn">Get my result</button>
</form>
