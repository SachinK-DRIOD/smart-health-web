<h1>Sign in</h1>
<form method="post" action="${ctx}/login" class="card narrow">
  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
  <label>Email <input type="email" name="email" required autocomplete="username" value="<c:out value='${param.email}'/>"></label>
  <label>Password <input type="password" name="password" required autocomplete="current-password"></label>
  <button type="submit" class="btn">Sign in</button>
  <p class="muted">New here? <a href="${ctx}/register">Create an account</a></p>
</form>
