package com.example.demo.music;

import com.google.api.services.youtube.model.VideoContentDetailsRegionRestriction;

import java.io.Serializable;
import java.util.Objects;

public class Song implements Serializable {

    private final Integer id;
    private final String band;
    private final String title;
    private final String prefix;
    private final String gameTitle;
    private final String src_id;
    private final String description;
    private Status status;
    private VideoContentDetailsRegionRestriction regionRestriction;

    public Song(Integer id, String band, String title, String prefix, String gameTitle, String src_id, String description) {
        this.id = id;
        this.band = band;
        this.title = title;
        this.prefix = prefix;
        this.gameTitle = gameTitle;
        this.description = description;
        this.src_id = src_id;
    }

    public Integer getId() {
        return id;
    }

    public String getBand() {
        return band;
    }

    public String getTitle() {
        return title;
    }

    public String getSrc_id() {
        return src_id;
    }

    public Status getStatus() {
        return status;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public VideoContentDetailsRegionRestriction getRegionRestriction() {
        return regionRestriction;
    }

    public void setRegionRestriction(VideoContentDetailsRegionRestriction regionRestriction) {
        this.regionRestriction = regionRestriction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Song{" + "id=").append(id).append(", band='").append(band).append('\'').append(", title='").append(title).append('\'').append(", prefix=").append(prefix).append('\'').append(", gameTitle=").append(gameTitle).append(", src_id='").append(src_id).append('\'').append(", description='").append(description).append('\'');
        if (status != null) {
            stringBuilder.append(", status=").append(status);
        }
        if (regionRestriction != null) {
            stringBuilder.append(", regionRestriction=").append(regionRestriction).append('}');
        }
        return stringBuilder.toString();
    }

    public enum Status {
        WORKING,
        RESTRICTED,
        DELETED,
        TOO_LONG
    }


}
