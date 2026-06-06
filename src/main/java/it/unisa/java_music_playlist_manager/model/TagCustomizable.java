package it.unisa.java_music_playlist_manager.model;

public class TagCustomizable implements Tag{
    
    private  String icon;
    private  String name;

    public TagCustomizable(String icon, String name) {
        this.icon = icon;
        this.name = name;
    }

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
    
    
}
