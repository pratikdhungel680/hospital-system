import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access layer for patients and appointments.
 * Returns data (lists/objects) rather than printing, so both a console UI
 * and a Swing GUI can consume it.
 */
public class PatientDAO {

    public void addPatient(Patient p) throws HospitalException {
        String sql = "INSERT INTO patients (id, name, age, disease, gender, contact) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getName());
            ps.setInt(3, p.getAge());
            ps.setString(4, p.getDisease());
            ps.setString(5, p.getGender());
            ps.setString(6, p.getContact());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new HospitalException("Patient ID already exists!");
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage());
        }
    }

    public void updatePatient(Patient p) throws HospitalException {
        String sql = "UPDATE patients SET name=?, age=?, disease=?, gender=?, contact=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getDisease());
            ps.setString(4, p.getGender());
            ps.setString(5, p.getContact());
            ps.setInt(6, p.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) throw new HospitalException("Patient not found!");
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage());
        }
    }

    public void deletePatient(int id) throws HospitalException {
        String deleteAppts = "DELETE FROM appointments WHERE patient_id = ?";
        String deletePatient = "DELETE FROM patients WHERE id = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(deleteAppts);
                 PreparedStatement ps2 = con.prepareStatement(deletePatient)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                int rows = ps2.executeUpdate();
                if (rows == 0) {
                    con.rollback();
                    throw new HospitalException("Patient not found!");
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new HospitalException("Database error: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage());
        }
    }

    public List<Patient> getAllPatients() throws HospitalException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new HospitalException("Unable to fetch patients: " + e.getMessage());
        }
        return patients;
    }

    public Patient searchPatient(int id) throws HospitalException {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                } else {
                    throw new HospitalException("Patient not found!");
                }
            }
        } catch (SQLException e) {
            throw new HospitalException("Search error: " + e.getMessage());
        }
    }

    public List<Patient> searchByName(String name) throws HospitalException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE name LIKE ? ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) patients.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new HospitalException("Search error: " + e.getMessage());
        }
        return patients;
    }

    public void bookAppointment(int patientId, String doctor, String date) throws HospitalException {
        String checkSql = "SELECT id FROM patients WHERE id = ?";
        String insertSql = "INSERT INTO appointments (patient_id, doctor, appointment_date) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setInt(1, patientId);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) throw new HospitalException("Patient does not exist!");
                }
            }
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, patientId);
                ps.setString(2, doctor);
                ps.setDate(3, Date.valueOf(date));
                ps.executeUpdate();
            }
        } catch (IllegalArgumentException e) {
            throw new HospitalException("Invalid date! Use YYYY-MM-DD.");
        } catch (SQLException e) {
            throw new HospitalException("Appointment error: " + e.getMessage());
        }
    }

    public List<Appointment> getAllAppointments() throws HospitalException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.patient_id, p.name, a.doctor, a.appointment_date " +
                     "FROM appointments a JOIN patients p ON a.patient_id = p.id ORDER BY a.appointment_date";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getString("doctor"),
                        rs.getDate("appointment_date").toString()
                ));
            }
        } catch (SQLException e) {
            throw new HospitalException("Unable to fetch appointments: " + e.getMessage());
        }
        return list;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("disease"),
                rs.getString("gender"),
                rs.getString("contact")
        );
    }
}
