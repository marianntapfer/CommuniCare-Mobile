package com.example.communicare_mobile.model;

public class TileModel {
    private long id;
    private String label;
    private String viewCategory;
    private String viewRedirect;
    private boolean textToSpeech;
    private String drawable;

    public String getDrawable() { return drawable; }
    public void setDrawable (String drawable) { this.drawable = drawable;}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getViewCategory() {
        return viewCategory;
    }

    public void setViewCategory(String viewCategory) {
        this.viewCategory = viewCategory;
    }

    public String getViewRedirect() {
        return viewRedirect;
    }

    public void setViewRedirect(String viewRedirect) {
        this.viewRedirect = viewRedirect;
    }

    public boolean isTextToSpeech() {
        return textToSpeech;
    }

    public void setTextToSpeech(boolean textToSpeech) {
        this.textToSpeech = textToSpeech;
    }

    @Override
    public String toString() {
        return "TileModel{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", viewCategory='" + viewCategory + '\'' +
                ", viewRedirect='" + viewRedirect + '\'' +
                ", textToSpeech=" + textToSpeech +
                '}';
    }
}
