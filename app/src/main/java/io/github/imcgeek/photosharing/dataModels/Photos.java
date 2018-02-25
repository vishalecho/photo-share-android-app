package io.github.imcgeek.photosharing.dataModels;

/**
 * Created by imcgeek on 25/2/18.
 */

public class Photos {
    private String PhotoName;
    private String PhotoCaption;
    private String PhotoLocation;
    private String PhotoCreatedDateTime;
    private String PhotoDescription;
    private String PhotoURL;
    private String PhotoAddedBy;

    public Photos() {
    }

    public Photos(String photoName, String photoCaption, String photoLocation, String photoCreatedDateTime, String photoDescription, String photoURL, String groupCreatedBy) {
        PhotoName = photoName;
        PhotoCaption = photoCaption;
        PhotoLocation = photoLocation;
        PhotoCreatedDateTime = photoCreatedDateTime;
        PhotoDescription = photoDescription;
        PhotoURL = photoURL;
        PhotoAddedBy = groupCreatedBy;
    }

    public String getPhotoName() {
        return PhotoName;
    }

    public void setPhotoName(String photoName) {
        PhotoName = photoName;
    }

    public String getPhotoCaption() {
        return PhotoCaption;
    }

    public void setPhotoCaption(String photoCaption) {
        PhotoCaption = photoCaption;
    }

    public String getPhotoLocation() {
        return PhotoLocation;
    }

    public void setPhotoLocation(String photoLocation) {
        PhotoLocation = photoLocation;
    }

    public String getPhotoCreatedDateTime() {
        return PhotoCreatedDateTime;
    }

    public void setPhotoCreatedDateTime(String photoCreatedDateTime) {
        PhotoCreatedDateTime = photoCreatedDateTime;
    }

    public String getPhotoDescription() {
        return PhotoDescription;
    }

    public void setPhotoDescription(String photoDescription) {
        PhotoDescription = photoDescription;
    }

    public String getPhotoURL() {
        return PhotoURL;
    }

    public void setPhotoURL(String photoURL) {
        PhotoURL = photoURL;
    }

    public String getPhotoAddedBy() {
        return PhotoAddedBy;
    }

    public void setPhotoAddedBy(String groupCreatedBy) {
        PhotoAddedBy = groupCreatedBy;
    }
}
