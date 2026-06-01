package it.unisa.java_music_playlist_manager.model;

/**
 * Classe che rappresenta il singolo brano musicale, dotandolo di tutti i metadati
 * con controlli integrità inseriti nei setter e richiamati nel costruttore
 */


// temporaneamente non inserisco implement subject
// ma con il fatto ceh si possa modificare, dovrebbe avvisare il controller
// quando la track viene modificata
public final class Track {
    
    private String title;
    private String author;
    private int duration; // secondi
    private String genre;
    private int year;

    // COSTRUTTORE CON CONTROLLI INTEGRITà
    public Track(String title, String author, int duration, String genre, int year) {
        setTitle(title);
        setAuthor(author);
        setDuration(duration);
        setGenre(genre);
        setYear(year);
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }


    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto o nullo.");
        }
        this.title = title.trim();
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("L'autore non può essere vuoto o nullo.");
        }
        this.author = author.trim();
    }

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("La durata deve essere maggiore di zero secondi.");
        }
        this.duration = duration;
    }

    public void setGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException("Il genere non può essere vuoto o nullo.");
        }
        this.genre = genre.trim();
    }

    public void setYear(int year) {
        if (year < 0 || year > 2026) {
            throw new IllegalArgumentException("Non puoi inserire tracce dal futuro o con anni negativi.");
        }
        this.year = year;
    }
}