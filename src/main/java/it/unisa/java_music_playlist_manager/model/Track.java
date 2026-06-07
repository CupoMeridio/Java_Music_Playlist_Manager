package it.unisa.java_music_playlist_manager.model;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe che rappresenta il singolo brano musicale, dotandolo di tutti i metadati
 * con controlli integrità inseriti nei setter e richiamati nel costruttore
 */


// temporaneamente non inserisco implement subject
// ma con il fatto ceh si possa modificare, dovrebbe avvisare il controller
// quando la track viene modificata
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe che rappresenta il singolo brano musicale, dotandolo di tutti i metadati
 * con controlli integrità inseriti nei setter e richiamati nel costruttore
 */


public final class Track implements Playable, Subject {
    
    private final List<Observer> observers = new ArrayList<>();
    
    private final String id;
    private String title;
    private String author;
    private String album;
    private String filePath;
    private int duration; // secondi
    private String genre;
    private Integer year;
    
    private Set<Tag> tags;

    // COSTRUTTORE CON CONTROLLI INTEGRITà
    public Track(String title, String author, String album, int duration, String genre, Integer year, String filePath) {
        this.id = UUID.randomUUID().toString();
        setTitle(title);
        setAuthor(author);
        setAlbum(album);
        setDuration(duration);
        setGenre(genre);
        setYear(year);
        setFilePath(filePath);
        tags = new LinkedHashSet<>();
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getAlbum() { return album; }
    public String getFilePath() { return filePath; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public Integer getYear() { return year; }


    public void addTag( Tag tag){
        this.tags.add(tag);
        notifyObservers();
    }
    
    public void removeTag(Tag tag){
        this.tags.remove(tag);
        notifyObservers();
    }
    
    public void removeAllTags(){
        this.tags.clear();
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
    public List<Track> getTracks() {
        // Una traccia restituisce semplicemente se stessa all'interno di una lista
        return Collections.singletonList(this); // utilizzato per l'ottimizzazione di memoria perchè è una lista con un solo elemento
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