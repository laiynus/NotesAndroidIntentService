package by.khrapovitsky.notesclient.model;

import java.io.Serializable;

public class Archive implements Serializable{

    private String imgPath;
    private String txtPath;

    public Archive(String imgPath, String txtPath) {
        this.imgPath = imgPath;
        this.txtPath = txtPath;
    }

    public Archive(){
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getTxtPath() {
        return txtPath;
    }

    public void setTxtPath(String txtPath) {
        this.txtPath = txtPath;
    }

    @Override
    public String toString() {
        return "Archive{" +
                "imgPath='" + imgPath + '\'' +
                ", txtPath='" + txtPath + '\'' +
                '}';
    }
}
