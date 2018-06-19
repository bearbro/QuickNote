package cn.edu.zjut.quicknote.model;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.List;
import java.util.UUID;

import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.bean.Note;

import static org.litepal.crud.DataSupport.where;

/**
 * <pre>
 *     author : FaDai
 *     e-mail : i_fadai@163.com
 *     time   : 2017/06/05
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */

public class NoteModel {

    public void loadAllNoteList(LoadDataCallBack<Note> callBack) {
        List<Note> data = where("isPrivacy = ? and inRecycleBin = ?", "0", "0").order("createdTime desc").find(Note.class);
        callBack.onSucceed(data);
    }

    public void loadPrivacyNoteList(LoadDataCallBack<Note> callBack) {
        List<Note> data = where("isPrivacy = ? and inRecycleBin = ?", "1", "0").order("createdTime desc").find(Note.class);
        callBack.onSucceed(data);
    }

    public void loadRecycleBinNoteList(LoadDataCallBack<Note> callBack) {
        List<Note> data = DataSupport.where("inRecycleBin = ?", "1").order("createdTime desc").find(Note.class);
        callBack.onSucceed(data);
    }

    public void loadNormalNoteList(int folderId, LoadDataCallBack<Note> callBack) {
        List<Note> data = where("noteFolderId = ? and isPrivacy = ? and inRecycleBin = ?", folderId + "", "0", "0").order("createdTime desc").find(Note.class);
        callBack.onSucceed(data);
    }

    public void deleteNote(Note note) {
        String noteId=note.getNoteId();
        deleteNoteFile(noteId);
        note.delete();
    }

    private void deleteNoteFile(String noteId){
        File file= Utils.getContext().getExternalFilesDir(noteId);
        if(file.exists()) {
            FileUtils.deleteDir(file);
        }
    }
}