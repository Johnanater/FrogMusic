package com.johnanater.frogmusic;

public class CustomMusic
{
    public String type;
    public String itemName;
    public int itemId;
    public String soundFile;
    public String soundPath;
    public String texturePath;
    public String textureFile;

    public CustomMusic(String type, String itemName, int itemId, String soundFile, String soundPath, String texturePath)
    {
        this.type = type;
        this.itemName = itemName;
        this.itemId = itemId;
        this.soundFile = soundFile;
        this.soundPath = soundPath;
        this.texturePath = texturePath;
    }
}
