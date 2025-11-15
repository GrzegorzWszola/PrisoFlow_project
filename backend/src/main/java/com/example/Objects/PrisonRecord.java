package com.example.Objects;

public class PrisonRecord {
    private int prisonId;
    private String name;
    private String location;
    private Integer capacity;
    private String securityLevel;
    private String wardenName;
    private String contactPhone;
    private String contactEmail;
    private Integer establishedYear;
    private String notes;

    // Getters and Setters
    public int getPrisonId() { return prisonId; }
    public void setPrisonId(int prisonId) { this.prisonId = prisonId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public String getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
    
    public String getWardenName() { return wardenName; }
    public void setWardenName(String wardenName) { this.wardenName = wardenName; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public Integer getEstablishedYear() { return establishedYear; }
    public void setEstablishedYear(Integer establishedYear) { this.establishedYear = establishedYear; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}