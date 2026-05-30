package com.example.myapplms.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO — gửi lên server khi UPDATE thông tin sinh viên.
 * Dùng cho: PUT /api/students/{id}
 *
 * Chỉ chứa các field người dùng được phép chỉnh sửa.
 * (studentId, userId không đưa vào body — đã nằm trong path/header)
 */
public class StudentRequest {

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("birthDate")
    private String birthDate;   // "yyyy-MM-dd"

    @SerializedName("location")
    private String location;

    @SerializedName("phone")
    private String phone;

    @SerializedName("bio")
    private String bio;

    @SerializedName("school")
    private String school;

    // ── Constructor đầy đủ ────────────────────────────────────────────────────

    public StudentRequest(String firstName, String lastName, String birthDate,
                          String location, String phone, String bio, String school) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.birthDate = birthDate;
        this.location  = location;
        this.phone     = phone;
        this.bio       = bio;
        this.school    = school;
    }

    // ── Builder pattern — tiện dùng khi chỉ update một số field ──────────────

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String firstName, lastName, birthDate, location, phone, bio, school;

        public Builder firstName(String v) { this.firstName = v; return this; }
        public Builder lastName(String v)  { this.lastName  = v; return this; }
        public Builder birthDate(String v) { this.birthDate = v; return this; }
        public Builder location(String v)  { this.location  = v; return this; }
        public Builder phone(String v)     { this.phone     = v; return this; }
        public Builder bio(String v)       { this.bio       = v; return this; }
        public Builder school(String v)    { this.school    = v; return this; }

        public StudentRequest build() {
            return new StudentRequest(firstName, lastName, birthDate,
                    location, phone, bio, school);
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String getFirstName() { return firstName; }
    public void   setFirstName(String v) { this.firstName = v; }

    public String getLastName()  { return lastName;  }
    public void   setLastName(String v)  { this.lastName  = v; }

    public String getBirthDate() { return birthDate; }
    public void   setBirthDate(String v) { this.birthDate = v; }

    public String getLocation()  { return location;  }
    public void   setLocation(String v)  { this.location  = v; }

    public String getPhone()     { return phone;     }
    public void   setPhone(String v)     { this.phone     = v; }

    public String getBio()       { return bio;       }
    public void   setBio(String v)       { this.bio       = v; }

    public String getSchool()    { return school;    }
    public void   setSchool(String v)    { this.school    = v; }
}