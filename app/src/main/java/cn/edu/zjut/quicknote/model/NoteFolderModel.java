package cn.edu.zjut.quicknote.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import cn.edu.zjut.quicknote.bean.Note;
import cn.edu.zjut.quicknote.bean.NoteFolder;


public class NoteFolderModel {

    private NoteModel mNoteModel=new NoteModel();

    public int initNoteFolderAndGetFolderId() {

        NoteFolder folder1 = new NoteFolder();
        folder1.setFolderName("随手记");
        folder1.setNoteCount(0);//todo
        folder1.save();

        NoteFolder noteFolder2 = new NoteFolder();
        noteFolder2.setFolderName("生活");
        noteFolder2.setNoteCount(0);
        noteFolder2.save();

        NoteFolder noteFolder3 = new NoteFolder();
        noteFolder3.setFolderName("工作");
        noteFolder3.save();

        NoteFolder folder= DataSupport.where("folderName = ? ","随手记").find(NoteFolder.class).get(0);
        return folder.getId();
    }

    public void loadNoteFoldersList(LoadDataCallBack<NoteFolder> callBack) {
        List<NoteFolder> list = DataSupport.findAll(NoteFolder.class);
        callBack.onSucceed(list);
    }

    public void deleteNoteFolder(NoteFolder folder) {
        int folderId=folder.getId();
        List<Note> list=new ArrayList<Note>();
        list= DataSupport.where("NoteFolderId = ? and inRecycleBin = ?",folderId+"","0").find(Note.class);
        for(int i=0;i<list.size();i++){
            Note note=list.get(i);
            mNoteModel.deleteNote(note);
        }
        folder.delete();
    }

    public void deleteNoteFolders() {
    }

    public void addNote2Folder(Note note, NoteFolder folder) {

        note.setNoteFolderId(folder.getId());
        note.save();

        folder.setNoteCount(folder.getNoteCount()+1);
        folder.save();
    }


}
