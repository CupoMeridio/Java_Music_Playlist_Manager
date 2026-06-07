package it.unisa.java_music_playlist_manager.model;

/**
 * Rappresenta un tag personalizzato creato dall'utente.
 * A differenza dei tag predefiniti, permette di definire nomi e icone arbitrari
 * per una classificazione flessibile della propria libreria.
 */
public class TagCustomizable implements Tag{
    
    /** Icona scelta dall'utente (es. emoji o carattere speciale) */
    private  String icon;
    
    /** Nome dell'etichetta personalizzata */
    private  String name;

    /**
     * Costruttore completo per un tag personalizzato.
     * @param icon L'icona da visualizzare.
     * @param name Il nome dell'etichetta.
     */
    public TagCustomizable(String icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    /**
     * Costruttore con icona di default (simbolo di insieme vuoto).
     * @param name Il nome dell'etichetta.
     */
    public TagCustomizable(String name) {
        this.name = name;
        this.icon = "∅";
    }

    
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getIcon() {
        return this.icon;
    }
    
     @Override
    public String toString() {
        return this.getIcon() + " " + this.getName();
    }
}
