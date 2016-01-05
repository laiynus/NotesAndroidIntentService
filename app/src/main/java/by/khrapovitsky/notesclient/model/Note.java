package by.khrapovitsky.notesclient.model;

public class Note {
    private String archiveName;
    private String txtPath;
    private String imgPath;
    private String dateModify;

    public Note() {
    }

    public Note(String archiveName, String txtPath, String imgPath, String dateModify) {
        this.archiveName = archiveName;
        this.txtPath = txtPath;
        this.imgPath = imgPath;
        this.dateModify = dateModify;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public String getTxtPath() {
        return txtPath;
    }

    public void setTxtPath(String txtPath) {
        this.txtPath = txtPath;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getDateModify() {
        return dateModify;
    }

    public void setDateModify(String dateModify) {
        this.dateModify = dateModify;
    }
}
