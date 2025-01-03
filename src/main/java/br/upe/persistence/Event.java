package br.upe.persistence;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event implements Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @NotNull
    @Column(unique = true)
    private String name;
    @NotNull
    private Date date;
    @NotNull
    private String description;
    @NotNull
    private String location;
    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    @NotNull
    private User ownerId;
    @OneToMany(mappedBy = "eventId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubEvent> subEvents;
    @OneToMany(mappedBy = "eventId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session> sessions;
    @OneToMany(mappedBy = "eventId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmitArticle> articles;

    public @NotNull User getOwnerId() {
        return ownerId;
    }

    public List<SubEvent> getSubEvents() {
        return subEvents;
    }

    public void setSubEvents(List<SubEvent> subEvents) {
        this.subEvents = subEvents;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public List<SubmitArticle> getArticles() {
        return articles;
    }

    public void setArticles(List<SubmitArticle> articles) {
        this.articles = articles;
    }

    public UUID getId() {

        return id;
    }

    public @NotNull String getName() {

        return name;
    }

    public @NotNull Date getDate() {

        return date;
    }

    public @NotNull String getDescription() {

        return description;
    }

    public @NotNull String getLocation() {

        return location;
    }

    // Setters

    public void setId(UUID id) {

        this.id = id;
    }

    public void setName(@NotNull String name) {

        this.name = name;
    }

    public void setDate(@NotNull Date date) {

        this.date = date;
    }

    public void setDescription(@NotNull String description) {

        this.description = description;
    }

    public void setLocation(@NotNull String location) {

        this.location = location;
    }

    public void setOwnerId(@NotNull User ownerId) {

        this.ownerId = ownerId;
    }
}
