package com.example.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProfileBuilder {
    private final Profile profile;

    public ProfileBuilder() {
        this.profile = new Profile();
        // Initialize defaults
        this.profile.setUuid(UUID.randomUUID().toString());
        this.profile.setType(ProfileType.USER);
        this.profile.setBarcodeType(BarcodeType.CODE_128);
        this.profile.setIssueDate(LocalDate.now());
        this.profile.setExpiryDate(LocalDate.now().plusYears(1));
    }

    public static ProfileBuilder builder() {
        return new ProfileBuilder();
    }

    public ProfileBuilder type(ProfileType type) {
        this.profile.setType(type);
        return this;
    }

    public ProfileBuilder fullName(String fullName) {
        this.profile.setFullName(fullName);
        return this;
    }

    public ProfileBuilder registrationNumber(String registrationNumber) {
        this.profile.setRegistrationNumber(registrationNumber);
        return this;
    }

    public ProfileBuilder department(String department) {
        this.profile.setDepartment(department);
        return this;
    }

    public ProfileBuilder title(String title) {
        this.profile.setTitle(title);
        return this;
    }

    public ProfileBuilder email(String email) {
        this.profile.setEmail(email);
        return this;
    }

    public ProfileBuilder phone(String phone) {
        this.profile.setPhone(phone);
        return this;
    }

    public ProfileBuilder bloodGroup(String bloodGroup) {
        this.profile.setBloodGroup(bloodGroup);
        return this;
    }

    public ProfileBuilder photoFileName(String photoFileName) {
        this.profile.setPhotoFileName(photoFileName);
        return this;
    }

    public ProfileBuilder photoContentType(String photoContentType) {
        this.profile.setPhotoContentType(photoContentType);
        return this;
    }

    public ProfileBuilder template(Template template) {
        this.profile.setTemplate(template);
        return this;
    }

    public ProfileBuilder barcodeType(BarcodeType barcodeType) {
        this.profile.setBarcodeType(barcodeType);
        return this;
    }

    public ProfileBuilder issueDate(LocalDate issueDate) {
        this.profile.setIssueDate(issueDate);
        return this;
    }

    public ProfileBuilder expiryDate(LocalDate expiryDate) {
        this.profile.setExpiryDate(expiryDate);
        return this;
    }

    public ProfileBuilder dateOfBirth(LocalDate dateOfBirth) {
        this.profile.setDateOfBirth(dateOfBirth);
        return this;
    }

    public Profile build() {
        // Ensure UUID and registration number are set before returning
        if (this.profile.getUuid() == null) {
            this.profile.setUuid(UUID.randomUUID().toString());
        }
        if (this.profile.getRegistrationNumber() == null) {
            // Generate a default registration number if not provided
            String year = String.valueOf(LocalDate.now().getYear());
            String dept = this.profile.getDepartment() != null ? this.profile.getDepartment().toUpperCase() : "GEN";
            if (dept.length() > 4) {
                dept = dept.substring(0, 4);
            }
            int randomNum = (int) (Math.random() * 900) + 100; // 100 to 999
            this.profile.setRegistrationNumber(year + "-" + dept + "-" + randomNum);
        }
        return this.profile;
    }
}
