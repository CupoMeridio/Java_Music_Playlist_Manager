package it.unisa.java_music_playlist_manager.model;

/**
 * Rappresenta un insieme di tag predefiniti dal sistema.
 * Utilizza un'enumerazione per offrire categorie standard pronte all'uso,
 * garantendo coerenza visiva e testuale in tutta l'applicazione.
 */
public enum TagPredefined implements Tag{
   
    ROCK("🎸", "Rock & Alternative"),
    CHILL("🌊", "Relax & Ambient"),
    WORKOUT("💪", "Sport & Energia"),
    STUDY("📚", "Studio & Focus"),
    PARTY("🎉", "Festa & Dance"),
    FAVOURITE("⭐","Preferiti"),
    LOFI("☕", "Lo-Fi Beats");
    
    /** Icona associata alla categoria */
    private final String icon;
    
    /** Nome visualizzato della categoria */
    private final String name;
    
    /**
     * Costruttore dell'enum.
     * @param icon L'icona rappresentativa.
     * @param name Il nome descrittivo.
     */
    TagPredefined( String icon, String name){
        this.name = name;
        this.icon = icon;
    }
    
  
    @Override
    public String getIcon() {
        return this.icon;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.getIcon() + " " + this.getName();
    }
}
