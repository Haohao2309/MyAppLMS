package com.example.myapplms.model;

/**
 * Domain / UI Model — chỉ chứa field cần thiết cho màn Profile.
 * Tách biệt hoàn toàn khỏi DTO mạng và Entity database.
 *
 * Tầng dữ liệu:
 *   [Network DTO]       [UI Model]
 *   StudentResponse ──→ Student   (qua StudentMapper)
 */
public class Student {

    private final Integer studentId;
    private final Integer userId;
    private final String  firstName;
    private final String  lastName;
    private final String  birthDate;   // "yyyy-MM-dd" — String để hiển thị trực tiếp
    private final String  location;
    private final String  phone;
    private final String  bio;
    private final String  school;

    public Student(Integer studentId, Integer userId,
                   String firstName, String lastName,
                   String birthDate, String location,
                   String phone, String bio, String school) {
        this.studentId = studentId;
        this.userId    = userId;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.birthDate = birthDate;
        this.location  = location;
        this.phone     = phone;
        this.bio       = bio;
        this.school    = school;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Integer getStudentId() { return studentId; }
    public Integer getUserId()    { return userId;    }
    public String  getFirstName() { return firstName; }
    public String  getLastName()  { return lastName;  }
    public String  getBirthDate() { return birthDate; }
    public String  getLocation()  { return location;  }
    public String  getPhone()     { return phone;     }
    public String  getBio()       { return bio;       }
    public String  getSchool()    { return school;    }

    /** Tiện ích: Tên đầy đủ, dùng trực tiếp trong UI. */
    public String getFullName() {
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName  != null ? lastName.trim()  : "";
        return (fn + " " + ln).trim();
    }

    @Override
    public String toString() {
        return "Student{id=" + studentId + ", name='" + getFullName() + "', school='" + school + "'}";
    }
}