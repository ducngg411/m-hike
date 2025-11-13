package com.example.m_hike.models;

public class Observation {
    private int id;
    private int hikeId; // Foreign key to Hike
    private String observation;
    private String time;
    private String comment;

    public Observation(int id, int hikeId, String observation, String time, String comment) {
        this.id = id;
        this.hikeId = hikeId;
        this.observation = observation;
        this.time = time;
        this.comment = comment;
    }

    public Observation(int hikeId, String observation, String time, String comment) {
        this.hikeId = hikeId;
        this.observation = observation;
        this.time = time;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHikeId() {
        return hikeId;
    }

    public void setHikeId(int hikeId) {
        this.hikeId = hikeId;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
