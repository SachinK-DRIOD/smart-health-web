package com.smarthealth.filter;

import com.smarthealth.model.User;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/** Session check, role-based access control and CSRF protection for every request. */
@WebFilter("/*")
public class AuthFilter implements Filter {
    private static final SecureRandom RNG = new SecureRandom();
    private static final Set<String> PUBLIC = Set.of("/", "/index.jsp", "/login", "/register");
    private static final Set<String> PATIENT_ONLY = Set.of("/patient/form", "/symptoms", "/result",
            "/appointment/book", "/appointments", "/appointment/cancel");

    public static String newToken() {
        byte[] b = new byte[24];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        String path = req.getServletPath();

        if (path.startsWith("/css/") || path.startsWith("/js/")) { chain.doFilter(rq, rs); return; }

        HttpSession session = req.getSession(true);
        if (session.getAttribute("csrf") == null) session.setAttribute("csrf", newToken());

        if ("POST".equals(req.getMethod())) {
            String sent = req.getParameter("csrf");
            String expected = (String) session.getAttribute("csrf");
            if (sent == null || !MessageDigest.isEqual(sent.getBytes(), expected.getBytes())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Your session expired. Reload the page and try again.");
                return;
            }
        }

        if (PUBLIC.contains(path)) { chain.doFilter(rq, rs); return; }

        User user = (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String role = user.getRole();
        boolean allowed;
        if (PATIENT_ONLY.contains(path)) allowed = "PATIENT".equals(role);
        else if (path.startsWith("/doctor/")) allowed = "DOCTOR".equals(role);
        else if (path.equals("/admin")) allowed = "ADMIN".equals(role);
        else if (path.equals("/report/pdf")) allowed = "PATIENT".equals(role) || "DOCTOR".equals(role);
        else allowed = true; // /dashboard, /logout
        if (!allowed) { resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have access to this page."); return; }

        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(rq, rs);
    }
}
