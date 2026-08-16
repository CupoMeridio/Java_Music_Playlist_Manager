
package it.unisa.java_music_playlist_manager.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TagPredefined.class, name = "predefined"),
})

/**
 * Interfaccia comune per i tag e le etichette categoriche applicabili alle tracce.
 */
public interface Tag {
    
    /** Restituisce il nome descrittivo del tag. */
    public String getName();

    /** Restituisce il nome dell'icona associata al tag (es. FontAwesome). */
    public String getIcon();
}
