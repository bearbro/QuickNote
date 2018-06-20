package cn.edu.zjut.quicknote;

import android.content.Context;

import com.blankj.utilcode.util.Utils;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

import cn.bmob.v3.Bmob;
import cn.edu.zjut.quicknote.constants.CacheManager;
import cn.edu.zjut.quicknote.constants.Constants;
import cn.edu.zjut.quicknote.constants.FolderListConstants;
import cn.edu.zjut.quicknote.constants.NoteListConstants;
import cn.edu.zjut.quicknote.utils.PreferencesUtil;

public class MainApplication extends LitePalApplication {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        init();
        initBmob();
        getCacheData();

    }

    private void init(){
        Utils.init(getApplicationContext());
        Connector.getDatabase();
}

    private void initBmob(){
        Bmob.initialize(this,getResources().getString(R.string.bmob_app_id));
    }

    private void getCacheData(){
        Constants.isFirst= PreferencesUtil.getBoolean(Constants.IS_FIRST,true);
        Constants.currentFolder= PreferencesUtil.getInt(Constants.CURRENT_FOLDER, FolderListConstants.ITEM_ALL);
        Constants.noteListShowMode=PreferencesUtil.getInt(Constants.NOTE_LIST_SHOW_MODE, NoteListConstants.STYLE_GRID);
        Constants.theme=PreferencesUtil.getInt(Constants.THEME,Constants.theme);
        Constants.isUseRecycleBin=PreferencesUtil.getBoolean(Constants.IS_USE_RECYCLE,Constants.isUseRecycleBin);
        Constants.isLocked=PreferencesUtil.getBoolean(Constants.IS_LOCKED,Constants.isLocked);
        Constants.lockPassword=PreferencesUtil.getString(Constants.LOCK_PASSWORD,"");
    }



}
