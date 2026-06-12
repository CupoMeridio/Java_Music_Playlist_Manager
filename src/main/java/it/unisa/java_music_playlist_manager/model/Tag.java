
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

public interface Tag {
    
    public String getName();
    public String getIcon();
}
