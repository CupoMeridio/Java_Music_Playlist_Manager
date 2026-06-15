package it.unisa.java_music_playlist_manager.model;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdateTrackCommand implements Command {
    private final Track track;

    // Nuovi dati
    private final String newTitle, newAuthor, newAlbum, newGenre, newFilePath;
    private final Integer newYear;
    private final List<Tag> newTags;

    // Vecchi dati da conservare per l'Undo
    private final String oldTitle, oldAuthor, oldAlbum, oldGenre, oldFilePath;
    private final Integer oldYear;
    private final Set<Tag> oldTags;

    public UpdateTrackCommand(Track track, String title, String author, String album, String genre, Integer year, String filePath, List<Tag> tags) {
        this.track = track;

        this.newTitle = title;
        this.newAuthor = author;
        this.newAlbum = album;
        this.newGenre = genre;
        this.newYear = year;
        this.newFilePath = filePath;
        this.newTags = tags != null ? tags : new ArrayList<>();

        // Memorizza lo stato precedente
        this.oldTitle = track.getTitle();
        this.oldAuthor = track.getAuthor();
        this.oldAlbum = track.getAlbum();
        this.oldGenre = track.getGenre();
        this.oldYear = track.getYear();
        this.oldFilePath = track.getFilePath();
        this.oldTags = new HashSet<>(track.getTags());
    }

    @Override
    public void execute() {
        applyState(newTitle, newAuthor, newAlbum, newGenre, newYear, newFilePath, newTags);
    }

    @Override
    public void undo() {
        applyState(oldTitle, oldAuthor, oldAlbum, oldGenre, oldYear, oldFilePath, new ArrayList<>(oldTags));
    }

    private void applyState(String title, String author, String album, String genre, Integer year, String filePath, List<Tag> tags) {
        track.setTitle(title);
        track.setAuthor(author);
        track.setAlbum(album);
        track.setGenre(genre);
        track.setYear(year);
        track.setFilePath(filePath);

        track.removeAllTags();
        tags.forEach(tag -> { if (tag != null) track.addTag(tag); });

        Library.getInstance().notifyObservers();
    }
}