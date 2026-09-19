public class Appointment {
    private int appointmentId;
    private int patientId;
    private String patientName;
    private String doctor;
    private String date;

    public Appointment(int appointmentId, int patientId, String patientName, String doctor, String date) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctor = doctor;
        this.date = date;
    }

    public int getAppointmentId() { return appointmentId; }
    public int getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDoctor() { return doctor; }
    public String getDate() { return date; }
}
