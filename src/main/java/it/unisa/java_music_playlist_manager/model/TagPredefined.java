package it.unisa.java_music_playlist_manager.model;

/**
 * Rappresenta un insieme di tag predefiniti dal sistema.
 * Utilizza un'enumerazione per offrire categorie standard pronte all'uso,
 * garantendo coerenza visiva e testuale in tutta l'applicazione.
 */
public enum TagPredefined implements Tag{
   
    ROCK("fas-guitar", "Energici"),
    CHILL("fas-water", "Relax"),
    WORKOUT("fas-dumbbell", "Sport"),
    STUDY("fas-book", "Concentrazione"),
    PARTY("fas-glass-cheers", "Festa"),
    FAVOURITE("fas-star", "Preferiti"),
    LOFI("fas-coffee", "Colazione"),
    ROAD_TRIP("fas-car", "Viaggi"),
    ROMANTIC("fas-heart", "Romantici"),
    NOSTALGIA("fas-moon", "Nostalgia"),
    SAD("fas-cloud-rain", "Malinconici"),
    SUMMER("fas-sun", "Estate"),
    SLEEP("fas-bed", "Buonanotte");
    
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
        return this.getName();
    }
}
