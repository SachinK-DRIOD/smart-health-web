<h1>Create your account</h1>
<form method="post" action="${ctx}/register" class="card narrow">
  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
  <label>Full name <input type="text" name="name" required maxlength="100" autocomplete="name" value="<c:out value='${param.name}'/>"></label>
  <label>Email <input type="email" name="email" required maxlength="150" autocomplete="email" value="<c:out value='${param.email}'/>"></label>
  <label>Password <input type="password" name="password" required minlength="8" autocomplete="new-password">
    <small>At least 8 characters, with a letter and a number.</small></label>
  <label>Confirm password <input type="password" name="confirm" required autocomplete="new-password"></label>
  <button type="submit" class="btn">Create account</button>
  <p class="muted">Already registered? <a href="${ctx}/login">Sign in</a></p>
</form>
