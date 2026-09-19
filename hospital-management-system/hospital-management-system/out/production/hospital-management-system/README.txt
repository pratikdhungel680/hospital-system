HOSPITAL MANAGEMENT SYSTEM — Swing GUI Edition
================================================

SETUP
-----
1. Import the database:
   - Open phpMyAdmin (or run: mysql -u root -p < hospital_db.sql)
   - This creates hospital_db, its tables (patients, appointments, users),
     and a default admin login (see below).

2. Add the MySQL Connector/J .jar to your IntelliJ project libraries
   (File > Project Structure > Libraries > +).

3. Run HospitalManagement.java. It launches the login screen.

DEFAULT LOGIN
-------------
   Username: admin
   Password: admin123

Change this password (delete the seed row in `users` and re-register, or add
a "change password" feature) before using this in anything beyond a demo —
the seed hash/salt pair is public since it ships in this file.

You can also click "New here? Create an account" on the login screen to
register your own username/password instead of using the seed account.

WHAT'S NEW IN THIS VERSION
---------------------------
- Full Swing GUI replacing the console menu: a branded login screen, a
  dashboard with live patient/appointment counts, and dedicated screens for
  adding patients, browsing/searching/editing/deleting patients, and booking
  appointments.
- Login system: a `users` table with SHA-256 + random-salt password hashing
  (PasswordUtil.java) — no plaintext passwords stored. Includes registration.
- Patient records now also capture gender and contact number.
- Patients can be edited and deleted (deleting a patient also removes their
  appointments, wrapped in a transaction so it can't leave orphaned rows).
- Search now works by ID or by partial name.
- Appointments have their own screen: a booking form plus a live table of
  all booked appointments joined with patient names.
- DBConnection now checks the connection on startup and shows a clear error
  dialog (missing driver / DB not running / bad credentials) instead of
  crashing silently.
- All SQL still uses PreparedStatement — no string-concatenated queries.

FILES
-----
Core / data:
  HospitalManagement.java   - entry point (launches the GUI)
  DBConnection.java         - JDBC connection + startup health check
  Patient.java               - patient model (+gender, +contact)
  Appointment.java           - appointment model
  User.java                  - authenticated user model
  PatientDAO.java             - patient/appointment database access
  UserDAO.java                - login + registration database access
  PasswordUtil.java           - SHA-256 salted password hashing
  HospitalException.java      - custom checked exception

GUI:
  LoginFrame.java            - login + registration screen
  MainFrame.java              - app shell (sidebar navigation)
  DashboardPanel.java          - overview / stat cards
  AddPatientPanel.java          - add-patient form
  PatientListPanel.java          - patient table, search, edit, delete
  EditPatientDialog.java          - edit-patient modal
  AppointmentPanel.java            - book + view appointments
  UITheme.java                      - shared colors/fonts/spacing
  RoundedButton.java, RoundedTextField.java,
  RoundedPasswordField.java, RoundedCard.java  - themed UI components

DATABASE
--------
  hospital_db.sql — patients, appointments, users tables + default admin seed

NOTE ON COMPILATION
--------------------
This was written and reviewed carefully but could not be compiled in the
sandbox it was produced in (no JDK available there, only a JRE). Compile it
in IntelliJ / your own JDK before relying on it, and let me know if you hit
any build errors — happy to fix them.
