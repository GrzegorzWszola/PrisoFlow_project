package com.example.Objects;
import java.time.LocalDate;

public class Prison {
    private Integer id;
    private String name;
    private String location;
    private Integer capacity;
    private String securityLevel;
    private LocalDate date;
    private Integer numOfCells;
    private Boolean isActive;

    public Prison() {};
    
    public Prison(Integer id, String name, String location, Integer capacity, String securityLevel, LocalDate date,
            Integer numOfCells, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.securityLevel = securityLevel;
        this.date = date;
        this.numOfCells = numOfCells;
        this.isActive = isActive;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
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
    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    public String getSecurityLevel() {
        return securityLevel;
    }
    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public Integer getNumOfCells() {
        return numOfCells;
    }
    public void setNumOfCells(Integer numOfCells) {
        this.numOfCells = numOfCells;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    
}
