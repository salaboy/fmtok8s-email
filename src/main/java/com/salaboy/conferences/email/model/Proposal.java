package com.salaboy.conferences.email.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Proposal {
    private String title;
    private String description;
    private String author;
    private String email;
    private boolean approved = false;

    public Proposal(String title, String description, String author, String email, boolean approved) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.email = email;
        this.approved = approved;
    }

    public Proposal() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return "Proposal{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", email='" + email + '\'' +
                ", approved=" + approved +
                '}';
    }
}
