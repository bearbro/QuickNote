package cn.edu.zjut.quicknote.utils;

import android.app.ProgressDialog;
import android.content.Context;


public class ProgressDialogUtils {

    private ProgressDialog mProgressDialog;
    private Context mContext;

    public ProgressDialogUtils(Context context){
        this.mContext=context;
    }

    public void show(String message){
        if(mProgressDialog==null)
            mProgressDialog=new ProgressDialog(mContext);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    public void hide(){
        if(mProgressDialog!=null)
            mProgressDialog.cancel();
    }
}
