package it.unisa.java_music_playlist_manager.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe astratta che fa da base comune per tutti i nodi Composite (Playlist).
 * Centralizza l'identificativo, il titolo e la logica di calcolo della durata.
 */
public abstract class Playlist implements Playable {
    
    private final String id;
    private String title;
    private int playCount = 0;

    /**
     * Costruttore comune a tutte le playlist. Garantisce che ogni playlist
     * abbia un ID univoco e un titolo valido fin dalla nascita.
     * * @param title Il titolo della playlist.
     */
    public Playlist(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo della playlist non può essere vuoto.");
        }
        this.id = UUID.randomUUID().toString();
        this.title = title.trim();
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Il titolo non può essere vuoto.");
        }
        this.title = title.trim();
    }

    public String getId() {
        return this.id;
    }

    public int getPlayCount() {
        return this.playCount;
    }

    public void incrementPlayCount() {
        this.playCount++;
    }

    /* 
     * OPERAZIONI COMPOSITE
     * Di base sono disabilitate (lanciano eccezione). In questo modo, le playlist 
     * automatiche sono protette di default senza dover scrivere codice duplicato.
     * Le sovrascriverà solo ManualPlaylist.
     */

    public void add(Playable element) {
        throw new UnsupportedOperationException("Operazione non supportata: non puoi modificare manualmente questa playlist.");
    }

    public void remove(Playable element) {
        throw new UnsupportedOperationException("Operazione non supportata: non puoi modificare manualmente questa playlist.");
    }

    public void removeTrack(Track track) {
        throw new UnsupportedOperationException("Operazione non supportata: non puoi modificare manualmente questa playlist.");
    }

    public void addTrack(Track track) {
        throw new UnsupportedOperationException("Operazione non supportata: non puoi modificare manualmente questa playlist.");
    }

    public void moveElement(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Operazione non supportata: non puoi modificare manualmente questa playlist.");
    }

    /**
     * Indica se questa playlist supporta modifiche manuali (aggiunta/rimozione brani).
     * @return true se è modificabile manualmente, false altrimenti
     */
    public boolean isManuallyEditable() {
        return false;
    }

    /**
     * Forza le sottoclassi a implementare la logica con cui espongono i brani,
     * rispettando il contratto dell'interfaccia Playable.
     */
    @Override
    public abstract List<Track> getTracks();  

    /**
     * Esempio di codice centralizzato (evita duplicazione): 
     * Qualsiasi sia il tipo di playlist, la durata totale si calcola sempre 
     * sommando la durata dei brani restituiti da getTracks().
     * * @return La durata totale in secondi.
     * @return 
     */
    public int getDuration() {
        return getTracks().stream()
                .mapToInt(Track::getDuration)
                .sum();
    }

    /* * Centralizzazione dei criteri di uguaglianza basati sull'ID univoco.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(id, playlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getTrackCount() {
        return getTracks().size();
    }

    @Override
    public String toString() {
        return title;
    }
}