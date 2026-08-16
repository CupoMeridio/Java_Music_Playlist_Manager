package it.unisa.java_music_playlist_manager.model;

/**
 * Creatore concreto (GoF Concrete Creator) per la generazione di playlist automatiche filtrate per tag.
 */
public class TagPlaylistGenerator extends PlaylistGenerator {

    private final Tag tag;

    /**
     * Costruisce il generatore specificando il tag di filtraggio.
     *
     * @param tag Il tag da utilizzare come filtro.
     */
    public TagPlaylistGenerator(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Il tag di filtraggio non può essere nullo.");
        }
        this.tag = tag;
    }

    @Override
    public Playable createPlaylist(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della playlist non può essere vuoto.");
        }
        return new AutomaticPlaylistByTag(title, tag);
    }
}
