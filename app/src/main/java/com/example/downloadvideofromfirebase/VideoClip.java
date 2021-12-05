package com.example.downloadvideofromfirebase;

public class VideoClip {

    public static int GREETING = 1;
    public static int REMINDER = 2;
    public static int COMFORT = 3;

    private String videoName;
    private String videoUri;
    private String author;
    private int category;
    private long timeStampUploaded;
    private String stringStorageReference;
//    private StorageReference storageReference;

    public VideoClip() { }

    public VideoClip(String videoName, String videoUri, String author, int category, long timeStampUploaded,
                     String stringStorageReference) {
        this.videoName = videoName;
        this.videoUri = videoUri;
        this.author = author;
        this.category = category;
        this.timeStampUploaded = timeStampUploaded;
        this.stringStorageReference = stringStorageReference;
     //   this.storageReference = storageReference;
    }

    public VideoClip(String videoName, String videoUri) {
        if (videoName.trim().equals("")){
            videoName ="Not baptized";
        }
        this.videoName = videoName;
        this.videoUri = videoUri;
        category = GREETING;
    }

    @Override
    public String toString() {
        return "VideoClip{" +
                "videoName='" + videoName + '\'' +
                ", videoUri='" + videoUri + '\'' +
                ", author='" + author + '\'' +
                ", category=" + category +
                ", timeStamp=" + timeStampUploaded +
                ", storageReference='" + stringStorageReference + '\'' +
                '}';
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public long getTimeStampUploaded() {
        return timeStampUploaded;
    }

    public void setTimeStampUploaded(long timeStampUploaded) {
        this.timeStampUploaded = timeStampUploaded;
    }

    public String getStringStorageReference() {
        return stringStorageReference;
    }

    public void setStringStorageReference(String stringStorageReference) {
        this.stringStorageReference = stringStorageReference;
    }
    /*
    public StorageReference getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(StorageReference storageReference) {
        this.storageReference = storageReference;
    }
    */

}
