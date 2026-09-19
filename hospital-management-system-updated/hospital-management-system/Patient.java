public class Patient {
    private int id;
    private String name;
    private int age;
    private String disease;
    private String gender;
    private String contact;

    public Patient(int id, String name, int age, String disease) {
        this(id, name, age, disease, "", "");
    }

    public Patient(int id, String name, int age, String disease, String gender, String contact) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.disease = disease;
        this.gender = gender;
        this.contact = contact;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDisease() { return disease; }
    public String getGender() { return gender; }
    public String getContact() { return contact; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setDisease(String disease) { this.disease = disease; }
    public void setGender(String gender) { this.gender = gender; }
    public void setContact(String contact) { this.contact = contact; }
}
