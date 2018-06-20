package cn.edu.zjut.quicknote.constants;

import cn.edu.zjut.quicknote.utils.PreferencesUtil;



public class CacheManager {

    /**
     *   修改并保存 是否是第一次进入的缓存
     */
    public static void setAndSaveIsFirst(boolean isFirst){
        Constants.isFirst=isFirst;
        PreferencesUtil.saveBoolean(Constants.IS_FIRST,isFirst);
    }

    /**
     *   修改并保存 当前便签夹
     */
    public static void setAndSaveCurrentFolder(int currentFolder){
        Constants.currentFolder=currentFolder;
        PreferencesUtil.saveInt(Constants.CURRENT_FOLDER,currentFolder);
    }

    /**
     *  便签列表显示模式
     */
    public static void setAndSaveNoteListShowMode(int showMode){
        Constants.noteListShowMode=showMode;
        PreferencesUtil.saveInt(Constants.NOTE_LIST_SHOW_MODE,showMode);
    }

    /**
     *   是否已启用废纸篓
     */
    public static void setAndSaveIsUseRecycleBin(boolean isUse){
        Constants.isUseRecycleBin=isUse;
        PreferencesUtil.saveBoolean(Constants.IS_USE_RECYCLE,isUse);
    }

    /**
     *   是否设置了私密密码
     */
    public static void setAndSaveIsLocked(boolean isLocked){
        Constants.isLocked=isLocked;
        PreferencesUtil.saveBoolean(Constants.IS_LOCKED,isLocked);
    }

    /**
     *   私密密码
     */
    public static void setAndSaveLockPassword(String lockPassword){
        Constants.lockPassword=lockPassword;
        PreferencesUtil.saveString(Constants.LOCK_PASSWORD,lockPassword);
    }
}
