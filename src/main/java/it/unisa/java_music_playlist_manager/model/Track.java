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
    private int duration; // secondi
    private String genre;
    private int year;
    
    private Set<Tag> tags;

    // COSTRUTTORE CON CONTROLLI INTEGRITà
    public Track(String title, String author, int duration, String genre, int year) {
        this.id = UUID.randomUUID().toString();
        setTitle(title);
        setAuthor(author);
        setDuration(duration);
        setGenre(genre);
        setYear(year);
        tags = new LinkedHashSet();
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }


    public void addTag( Tag tag){
        this.tags.add(tag);
        notifyObservers();
    }
    
    public void removeTag(Tag tag){
        this.tags.remove(tag);
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
            throw new IllegalArgumentException("L'autore non può essere vuoto o nullo.");
        }
        if (this.author == null || !this.author.equals(author.trim())) {
            this.author = author.trim();
            notifyObservers();
        }
    }

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("La durata deve essere maggiore di zero secondi.");
        }
        if (this.duration != duration) {
            this.duration = duration;
            notifyObservers();
        }
    }

    public void setGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Il genere non può essere vuoto o nullo.");
        }
        if (this.genre == null || !this.genre.equals(genre.trim())) {
            this.genre = genre.trim();
            notifyObservers();
        }
    }

    public void setYear(int year) {
        int thisYear = LocalDate.now().getYear();
        if (year < 0 || year > thisYear) {
            throw new IllegalArgumentException("Non puoi inserire tracce dal futuro o con anni negativi.");
        }
        if (this.year != year) {
            this.year = year;
            notifyObservers();
        }
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