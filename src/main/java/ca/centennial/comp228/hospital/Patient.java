package ca.centennial.comp228.hospital;

import java.time.LocalDate;

/**
 * Simple model for one row of the patients table.
 * Placeholder stage: no database mapping yet.
 */
public class Patient {

    private final int patientId;
    private final String name;
    private final int age;
    private final String gender;
    private final String phone;
    private final String diagnosis;
    private final LocalDate admissionDate;

    public Patient(int patientId, String name, int age, String gender,
                   String phone, String diagnosis, LocalDate admissionDate) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.diagnosis = diagnosis;
        this.admissionDate = admissionDate;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }
}
