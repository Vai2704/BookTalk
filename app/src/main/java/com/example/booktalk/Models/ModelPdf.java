package com.example.booktalk.Models;

public class ModelPdf {
    //variables
    String uid,id, title, description, categoryId, url;
    long timestamp, viewsCount, downloadCount;

    boolean favorite;

    // empty Constructor, required for firebase
    public ModelPdf() {
    }

    //parameterized constructor


    public ModelPdf(String uid, String id, String title, String description, String categoryId, String url, long timestamp, long viewsCount, long downloadCount, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.viewsCount = viewsCount;
        this.downloadCount = downloadCount;
        this.favorite = favorite;
    }

    // getter/setter
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}