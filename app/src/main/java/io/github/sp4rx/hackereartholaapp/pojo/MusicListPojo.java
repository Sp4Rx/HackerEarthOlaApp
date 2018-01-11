package io.github.sp4rx.hackereartholaapp.pojo;

/**
 * Created by suvajit.<br>
 *     Object model for the songs
 */

public class MusicListPojo {
    private int id;
    private String song;
    private String url;
    private String artists;
    private String coverImage;
    private boolean isPlaying;
    private boolean isFavourite;
    private int playedCount;

    /**
     * Empty Constructor
     */
    public MusicListPojo() {
    }

    /**
     * @param song       Song Name
     * @param url        Url of the song
     * @param artists    Artist Names
     * @param coverImage Cover Image of the Song
     */
    public MusicListPojo(String song, String url, String artists, String coverImage) {
        this.song = song;
        this.url = url;
        this.artists = artists;
        this.coverImage = coverImage;
    }

    /**
     * @param id         Local index
     * @param song       Song Name
     * @param url        Url of the song
     * @param artists    Artist Names
     * @param coverImage Cover Image of the Song
     */
    public MusicListPojo(int id, String song, String url, String artists, String coverImage) {
        this.id = id;
        this.song = song;
        this.url = url;
        this.artists = artists;
        this.coverImage = coverImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public int getPlayedCount() {
        return playedCount;
    }

    public void setPlayedCount(int playedCount) {
        this.playedCount = playedCount;
    }
}
