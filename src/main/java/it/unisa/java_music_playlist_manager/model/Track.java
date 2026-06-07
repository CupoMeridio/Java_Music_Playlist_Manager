package it.unisa.java_music_playlist_manager.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * La classe Track rappresenta un singolo brano musicale all'interno del sistema.
 * Contiene tutti i metadati associati a una canzone (titolo, autore, album, etc.)
 * e gestisce la validazione dei dati tramite i propri setter.
 * 
 * Pattern utilizzati:
 * - Composite (Leaf): Implementa l'interfaccia Playable fungendo da elemento "foglia".
 *   A differenza della Playlist, una Track non può contenere altri elementi Playable.
 * - Observer (Subject): Implementa l'interfaccia Subject. Poiché i metadati di una traccia
 *   possono essere modificati, la classe notifica i propri osservatori (es. la UI)
 *   per garantire che i dati visualizzati siano sempre aggiornati.
 */
public final class Track implements Playable, Subject {
    
    /** Lista degli osservatori registrati per questa specifica traccia (Pattern Observer) */
    private final List<Observer> observers = new ArrayList<>();
    
    /** Identificativo univoco della traccia, generato automaticamente */
    private final String id;
    
    /** Titolo del brano */
    private String title;
    
    /** Autore/Artista del brano */
    private String author;
    
    /** Album di appartenenza */
    private String album;
    
    /** Percorso assoluto o relativo del file audio nel filesystem */
    private String filePath;
    
    /** Durata del brano in secondi */
    private int duration;
    
    /** Genere musicale */
    private String genre;
    
    /** Anno di pubblicazione */
    private Integer year;
    
    /** Insieme di tag personalizzati associati alla traccia */
    private Set<Tag> tags;

    /**
     * Costruttore della classe Track.
     * Inizializza un nuovo brano musicale validando tutti i parametri tramite i setter.
     * 
     * @param title    Il titolo del brano (non può essere nullo o vuoto).
     * @param author   L'autore del brano (se nullo/vuoto diventa "Sconosciuto").
     * @param album    L'album del brano (se nullo/vuoto diventa "Sconosciuto").
     * @param duration La durata in secondi (se negativa diventa 0).
     * @param genre    Il genere musicale (se nullo/vuoto diventa "Generico").
     * @param year     L'anno di pubblicazione (non può essere nel futuro).
     * @param filePath Il percorso del file audio (non può essere nullo o vuoto).
     */
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

    /**
     * Aggiunge un tag alla traccia e notifica gli osservatori.
     * 
     * @param tag Il tag da aggiungere.
     */
    public void addTag( Tag tag){
        this.tags.add(tag);
        notifyObservers();
    }
    
    /**
     * Rimuove un tag dalla traccia e notifica gli osservatori.
     * 
     * @param tag Il tag da rimuovere.
     */
    public void removeTag(Tag tag){
        this.tags.remove(tag);
        notifyObservers();
    }
    
    /**
     * Rimuove tutti i tag associati alla traccia.
     */
    public void removeAllTags(){
        this.tags.clear();
        notifyObservers();
    }

    /**
     * Restituisce l'insieme dei tag associati.
     * 
     * @return Il set di tag della traccia.
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Imposta il titolo della traccia.
     * 
     * @param title Il nuovo titolo.
     * @throws IllegalArgumentException Se il titolo è nullo o composto solo da spazi.
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto o nullo.");
        }
        if (this.title == null || !this.title.equals(title.trim())) {
            this.title = title.trim();
            notifyObservers();
        }
    }

    /**
     * Imposta l'autore della traccia.
     * 
     * @param author Il nuovo autore.
     */
    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            this.author = "Sconosciuto";
        } else {
            this.author = author.trim();
        }
        notifyObservers();
    }

    /**
     * Imposta l'album della traccia.
     * 
     * @param album Il nuovo album.
     */
    public void setAlbum(String album) {
        if (album == null || album.trim().isEmpty()) {
            this.album = "Sconosciuto";
        } else {
            this.album = album.trim();
        }
        notifyObservers();
    }

    /**
     * Imposta la durata della traccia in secondi.
     * 
     * @param duration La durata. Se negativa, viene impostata a 0.
     */
    public void setDuration(int duration) {
        if (duration < 0) {
            this.duration = 0;
        } else {
            this.duration = duration;
        }
        notifyObservers();
    }

    /**
     * Imposta il genere della traccia.
     * 
     * @param genre Il nuovo genere.
     */
    public void setGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            this.genre = "Generico";
        } else {
            this.genre = genre.trim();
        }
        notifyObservers();
    }

    /**
     * Imposta l'anno di pubblicazione.
     * 
     * @param year L'anno.
     * @throws IllegalArgumentException Se l'anno è nel futuro.
     */
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

    /**
     * Imposta il percorso del file audio.
     * 
     * @param filePath Il nuovo percorso.
     * @throws IllegalArgumentException Se il percorso è nullo o vuoto.
     */
    public void setFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Il percorso del file non può essere vuoto.");
        }
        this.filePath = filePath;
        notifyObservers();
    }
    
    // --- Metodi dell'interfaccia Subject (Pattern Observer) ---

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

    /**
     * Implementazione del metodo dell'interfaccia Playable (Pattern Composite).
     * Una traccia, essendo un elemento foglia, restituisce una lista contenente solo se stessa.
     * 
     * @return Una lista immutabile contenente questa traccia.
     */
    @Override
    public List<Track> getTracks() {
        // Utilizziamo Collections.singletonList per efficienza: crea una lista immutabile con un solo elemento.
        return Collections.singletonList(this);
    }

    /**
     * Verifica l'uguaglianza tra due tracce basandosi sull'identificativo univoco (id).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(id, track.id);
    }

    /**
     * Genera l'hashcode basandosi sull'id univoco della traccia.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}