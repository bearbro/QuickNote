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

/**
 * <pre>
 *     author : FaDai
 *     e-mail : i_fadai@163.com
 *     time   : 2017/06/02
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */

public class MainApplication extends LitePalApplication {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        init();
        initBmob();
        getCacheData();
        setUpdateForVersionCode1();
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

    // 为了兼容1.0.1版本，将其的缓存信息进行备份修改
    private void setUpdateForVersionCode1(){
        // 1.0.1版本使用的key值，如果是false，说明之前是V1.0.1版本
        boolean isFirst= PreferencesUtil.getBoolean("isFirst",true);
        if(!isFirst){
            boolean isGrid= PreferencesUtil.getBoolean("is_grid",false);
            boolean isUseRecycleBin=PreferencesUtil.getBoolean("recycle_bin",false);

            CacheManager.setAndSaveIsFirst(false);
            CacheManager.setAndSaveCurrentFolder(FolderListConstants.ITEM_ALL);
            CacheManager.setAndSaveIsUseRecycleBin(isUseRecycleBin);
            if(isGrid){
                CacheManager.setAndSaveNoteListShowMode(NoteListConstants.MODE_GRID);
            } else {
                CacheManager.setAndSaveNoteListShowMode(NoteListConstants.MODE_LIST);
            }
            // isLock、lockPassword key值一样；主题key值不用修改。

        }
    }
}
