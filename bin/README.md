# Smart Health Web (Eclipse project)

Java web app: Servlets, JSP + JSTL, Hibernate, MySQL, iText, JavaMail, BCrypt.
All Java code and all frontend code (JSP, CSS) live in this one Eclipse project.
The Python prediction service is the separate `smart-health-ml` project (VS Code).

Import: File > Import > Maven > Existing Maven Projects > select this folder.
Run: right-click project > Run As > Run on Server > Apache Tomcat v9.0.

Files you may need to edit:
- `src/main/resources/hibernate.cfg.xml`  MySQL URL, user, password
- `src/main/resources/app.properties`     ML service URL and (optional) SMTP settings

Seeded logins (created on first start; change before real use):
- Admin: admin@clinic.test / Admin@123
- Doctors: asha@, rohan@, neha@, imran@, priya@, arjun@ clinic.test / Doctor@123
