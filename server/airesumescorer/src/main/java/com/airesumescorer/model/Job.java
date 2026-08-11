package com.airesumescorer.model;
import jakarta.persistence.*;

@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //automatically increments
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
