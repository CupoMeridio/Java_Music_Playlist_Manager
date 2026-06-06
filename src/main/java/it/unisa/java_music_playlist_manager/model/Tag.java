package it.unisa.java_music_playlist_manager.model;

/**
 * Rappresenta un tag associato a un brano musicale.
 * Ogni tag è identificato da un nome e da un'icona (es. un carattere Unicode o un'emoji).
 */
public interface Tag {
    
    /**
     * Restituisce il nome identificativo del tag.
     * * @return il nome del tag
     */
    public String getName();
    
    /**
     * Restituisce la rappresentazione grafica o l'icona del tag.
     * * @return l'icona del tag sotto forma di stringa
     */
    public String getIcon();
}