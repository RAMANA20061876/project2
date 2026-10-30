package com.movieticket.model;

import java.time.LocalDate;
import java.time.Period;

public class User {

    private final String userId;
    private final String name;
    private final String phone;
    private final String email;
    private final LocalDate dateOfBirth;

    public User(String userId, String name, String phone, String email, LocalDate dateOfBirth) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    /** Age of the user on the given date. */
    public int getAgeOn(LocalDate date) {
        return Period.between(dateOfBirth, date).getYears();
    }

    @Override
    public String toString() {
        return userId + " - " + name + " (" + email + ")";
    }
}
