package it.unisa.java_music_playlist_manager.model;

/**
 * L'interfaccia Tag definisce il contratto per le etichette che possono essere associate alle tracce.
 * Permette di classificare i brani oltre i metadati standard (genere, album, etc.).
 * 
 * In questo progetto, i tag sono composti da un nome descrittivo e da un'icona visuale.
 */
public interface Tag {
    
    /**
     * Restituisce il nome identificativo del tag.
     * @return Il nome del tag.
     */
    public String getName();
    
    /**
     * Restituisce l'icona o il simbolo associato al tag.
     * @return L'icona del tag come stringa (es. emoji).
     */
    public String getIcon();
}