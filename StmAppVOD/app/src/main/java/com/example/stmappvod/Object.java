package com.example.stmappvod;

public class Object {

    private String thumbnail;
    private String channelName;
    private String channelID;

    public Object(String thumbnail, String channelName, String channelID) {
        this.thumbnail = thumbnail;
        this.channelName = channelName;
        this.channelID = channelID;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }
}
