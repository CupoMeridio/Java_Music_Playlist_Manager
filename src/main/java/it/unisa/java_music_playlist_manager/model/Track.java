
package it.unisa.java_music_playlist_manager.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Rappresenta una singola traccia musicale nel dominio dell'applicazione.
 * Contiene i metadati audio (titolo, artista, album, durata, genere, anno, tag, conteggio riproduzioni)
 * ed implementa {@link Playable} per l'inserimento in coda e {@link Subject} per la notifica dei cambiamenti.
 */
public final class Track implements Playable, Subject {
    
    @JsonIgnore
    private final List<Observer> observers = new ArrayList<>();
    
    private final String id;
    
    private String title;
    
    private String author;
    
    private String album;
    
    private String filePath;
    
    private int duration;
    
    private String genre;
    
    private Integer year;
    
    private Set<Tag> tags;

    private int playCount = 0;

    @JsonCreator
    public Track(
            @JsonProperty("id") String id,
            @JsonProperty("title") String title, 
            @JsonProperty("author") String author, 
            @JsonProperty("album") String album, 
            @JsonProperty("duration") int duration, 
            @JsonProperty("genre") String genre, 
            @JsonProperty("year") Integer year, 
            @JsonProperty("filePath") String filePath,
            @JsonProperty("tags") Set<Tag> tags,
            @JsonProperty("playCount") int playCount) {
        this.id = (id != null) ? id : UUID.randomUUID().toString();
        setTitle(title);
        setAuthor(author);
        setAlbum(album);
        setDuration(duration);
        setGenre(genre);
        setYear(year);
        setFilePath(filePath);
        this.tags = (tags != null) ? tags : new LinkedHashSet<>();
        this.playCount = playCount;
    }
    
    public Track(String title, String author, String album, int duration, String genre, Integer year, String filePath) {
        this(UUID.randomUUID().toString(), title, author, album, duration, genre, year, filePath, new LinkedHashSet<>(), 0);
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getAlbum() { return album; }
    public String getFilePath() { return filePath; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public Integer getYear() { return year; }
    public int getPlayCount() { return playCount; }
    public String getId() { return id; }

    public void incrementPlayCount() {
        this.playCount++;
        notifyObservers();
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        notifyObservers();
    }
    
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        notifyObservers();
    }
    
    public void removeAllTags() {
        this.tags.clear();
        notifyObservers();
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto o nullo.");
        }
        if (this.title == null || !this.title.equals(title.trim())) {
            this.title = title.trim();
            notifyObservers();
        }
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            this.author = "Sconosciuto";
        } else {
            this.author = author.trim();
        }
        notifyObservers();
    }

    public void setAlbum(String album) {
        if (album == null || album.trim().isEmpty()) {
            this.album = "Sconosciuto";
        } else {
            this.album = album.trim();
        }
        notifyObservers();
    }

    public void setDuration(int duration) {
        if (duration < 0) {
            this.duration = 0;
        } else {
            this.duration = duration;
        }
        notifyObservers();
    }

    public void setGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            this.genre = "Generico";
        } else {
            this.genre = genre.trim();
        }
        notifyObservers();
    }

    public void setYear(Integer year) {
        int thisYear = LocalDate.now().getYear();

        if (year != null) {
            if (year > thisYear) {
                throw new IllegalArgumentException("L'anno non può essere nel futuro.");
            } 
            if (year < 0) {
                year = null;
            }
        }
        this.year = year;

        notifyObservers();
    }

    public void setFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso del file non può essere vuoto.");
        }
        this.filePath = filePath;
        notifyObservers();
    }
    
    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    @Override
    @JsonIgnore
    public List<Track> getTracks() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(id, track.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
