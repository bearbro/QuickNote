package cn.edu.zjut.quicknote.bean;

import org.litepal.crud.DataSupport;



public class NoteFolder extends DataSupport {
    private int id;
    private String folderName;
    private int noteCount;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNoteCount() {
        return noteCount;
    }

    public void setNoteCount(int noteCount) {
        this.noteCount = noteCount;
    }
}
