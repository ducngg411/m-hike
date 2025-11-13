package com.example.m_hike.models;


public class Hike {
    private int id;
    private String name;
    private String location;
    private String date;
    private String parkingAvailable; // "Yes" or "No"
    private double length; // in kilometers
    private String difficulty; // "Easy", "Moderate", "Hard"
    private String description;

    // Other relevant fields

    private String estimatedDuration; // "3 hours"
    private int maxGroupSize;

    // Constructors with id (for retrieval)
    public Hike(int id, String name, String location, int maxGroupSize, String estimatedDuration, String description, String difficulty, double length, String parkingAvailable, String date) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.maxGroupSize = maxGroupSize;
        this.estimatedDuration = estimatedDuration;
        this.description = description;
        this.difficulty = difficulty;
        this.length = length;
        this.parkingAvailable = parkingAvailable;
        this.date = date;
    }

    // Constructors without id (for insertion)
    public Hike(String name, String location, String date, String parkingAvailable, double length, String difficulty, String description, String estimatedDuration, int maxGroupSize) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.parkingAvailable = parkingAvailable;
        this.length = length;
        this.difficulty = difficulty;
        this.description = description;
        this.estimatedDuration = estimatedDuration;
        this.maxGroupSize = maxGroupSize;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getParkingAvailable() {
        return parkingAvailable;
    }

    public void setParkingAvailable(String parkingAvailable) {
        this.parkingAvailable = parkingAvailable;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }
}
