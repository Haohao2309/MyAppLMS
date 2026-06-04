// ── model/Teacher.java ────────────────────────────────────────
package com.example.myapplms.model;

public class Teacher {

    private final Integer teacherId;
    private final Integer userId;
    private final String  firstName;
    private final String  lastName;
    private final String  birthDate;
    private final String  location;
    private final String  phone;
    private final String  bio;
    private final String  degree;

    public Teacher(Integer teacherId, Integer userId,
                   String firstName, String lastName,
                   String birthDate, String location,
                   String phone, String bio, String degree) {
        this.teacherId = teacherId;
        this.userId    = userId;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.birthDate = birthDate;
        this.location  = location;
        this.phone     = phone;
        this.bio       = bio;
        this.degree    = degree;
    }

    public Integer getTeacherId() { return teacherId; }
    public Integer getUserId()    { return userId;    }
    public String  getFirstName() { return firstName; }
    public String  getLastName()  { return lastName;  }
    public String  getBirthDate() { return birthDate; }
    public String  getLocation()  { return location;  }
    public String  getPhone()     { return phone;     }
    public String  getBio()       { return bio;       }
    public String  getDegree()    { return degree;    }

    public String getFullName() {
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName  != null ? lastName.trim()  : "";
        return (fn + " " + ln).trim();
    }
}