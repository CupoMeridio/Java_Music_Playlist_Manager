package it.unisa.java_music_playlist_manager.model;

public enum TagPredefined implements Tag{
   
    ROCK("🎸", "Rock & Alternative"),
    CHILL("🌊", "Relax & Ambient"),
    WORKOUT("💪", "Sport & Energia"),
    STUDY("📚", "Studio & Focus"),
    PARTY("🎉", "Festa & Dance"),
    FAVOURITE("⭐","Preferiti"),
    LOFI("☕", "Lo-Fi Beats");
    
    private final String icon;
    private final String name;
    
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
        return this.getIcon()+ this.getName();
    }
    
    
}
