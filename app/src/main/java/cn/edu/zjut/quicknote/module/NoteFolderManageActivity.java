package cn.edu.zjut.quicknote.module;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.blankj.utilcode.util.SnackbarUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.adapter.EditFolderAdapter;
import cn.edu.zjut.quicknote.bean.NoteFolder;
import cn.edu.zjut.quicknote.constants.EditFolderConstants;
import cn.edu.zjut.quicknote.model.LoadDataCallBack;
import cn.edu.zjut.quicknote.model.NoteFolderModel;
import cn.edu.zjut.quicknote.utils.ThemeUtils;

public class NoteFolderManageActivity extends AppCompatActivity
        implements BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemChildClickListener, View.OnClickListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rv_folder_folder)
    RecyclerView mRvFolder;

    @BindView(R.id.fab_folder_add)
    FloatingActionButton mFabAdd;

    private float mScrollLastY;
    private float mTouchSlop;
    private ProgressDialog mProgressDialog;

    // 当前已选中的便签夹
    private int mCurrentFolderId;
    // 当前已选中的便签夹是否已被删除
    private boolean mIsCurrentFolderDeleted = false;
    // 是否对便签夹做了修改
    private int mResultCode = Activity.RESULT_CANCELED;

    private EditFolderAdapter mEditFolderAdapter;

    private NoteFolderModel mNoteFolderModel = new NoteFolderModel();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_folder);

        ButterKnife.bind(this);
        initViews();
    }

    protected void initViews() {
        setSupportActionBar(mToolbar);

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        mCurrentFolderId = getIntent().getIntExtra("current_folder_id", 0);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("编辑便签夹");

        mRvFolder.setLayoutManager(new LinearLayoutManager(this));

        mEditFolderAdapter = new EditFolderAdapter();
        mEditFolderAdapter.setOnItemChildClickListener(this);
        mEditFolderAdapter.setOnItemClickListener(this);
        mEditFolderAdapter.bindToRecyclerView(mRvFolder);

        mFabAdd.setOnClickListener(this);
        mNoteFolderModel.loadNoteFoldersList(new LoadDataCallBack<NoteFolder>() {
            @Override
            protected void onSucceed(List<NoteFolder> list) {
                mEditFolderAdapter.setNewData(list);
            }
        });
    }

    public void showSnackbar() {
        SnackbarUtils.with(mRvFolder)
                .setMessage("请选择要删除的便签夹")
                .setBgColor(ThemeUtils.getColorPrimary(this))
                .show();
    }

    public void scrollToItem(int position) {
        mRvFolder.scrollToPosition(position);
    }

    @Override
    public void onBackPressed() {
        EditFolderConstants.selectedCount = 0;
        EditFolderConstants.selectedItem = -1;
        EditFolderConstants.isNewFolder = false;

        Intent intent = new Intent();
        intent.putExtra("is_current_folder_deleted", mIsCurrentFolderDeleted);
        setResult(mResultCode, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folder, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_folder_delete);
        if (EditFolderConstants.selectedCount > 0) {
            item.getIcon().setAlpha(255);
        } else {
            item.getIcon().setAlpha(85);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_folder_delete:
                if (EditFolderConstants.selectedCount > 0) {
                    showDeleteDialog();
                } else {
                    showSnackbar();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除便签夹")
                .setMessage("确定删除选中的便签夹吗？")
                .setPositiveButton("删除",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteFoldersTask().execute();
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        boolean isChecked = mEditFolderAdapter.mCheckList.get(position);
        if (isChecked) {
            EditFolderConstants.selectedCount--;
        } else {
            EditFolderConstants.selectedCount++;
        }
        mEditFolderAdapter.mCheckList.set(position, !isChecked);
        mEditFolderAdapter.notifyItemChanged(position);
    }

    private void editItem(int position) {
        EditFolderConstants.selectedItem = position;
        mEditFolderAdapter.notifyItemChanged(position);

        EditText editText = (EditText) mEditFolderAdapter.getViewByPosition(position, R.id.et_edit_folder_name);
        editText.selectAll();
        setFoucus(editText);
        mFabAdd.hide();
    }

    public void setFoucus(View view) {
//        获取 接受焦点的资格
        view.setFocusable(true);
//        获取 焦点可以响应点触的资格
        view.setFocusableInTouchMode(true);
//        请求焦点
        view.requestFocus();
//        弹出键盘
        InputMethodManager manager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(0, 0);
        manager.showSoftInput(view, 0);
    }

    private void verifyName(int position, String newName) {
        // 新名字为空
        if (newName.isEmpty()) {
            TextInputLayout textInputLayout = (TextInputLayout) mEditFolderAdapter.getViewByPosition(position, R.id.textinput_edit_folder_name);
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError("便签夹名为空");
            return;
        }

        // 名字重复
        for (int i = 0; i < mEditFolderAdapter.getData().size(); i++) {
            if (i != position && mEditFolderAdapter.getData().get(i).getFolderName().equals(newName)) {
                TextInputLayout textInputLayout = (TextInputLayout) mEditFolderAdapter.getViewByPosition(position, R.id.textinput_edit_folder_name);
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("已存在");
                return;
            }
        }

        saveNewNameToFolder(position, newName);
    }

    private void saveNewNameToFolder(int position, String newName) {
        NoteFolder folder = mEditFolderAdapter.getData().get(position);
        folder.setFolderName(newName);
        folder.save();

        EditFolderConstants.selectedItem = -1;
        mEditFolderAdapter.notifyItemChanged(position);
        closeKeyboard(mEditFolderAdapter.getViewByPosition(position, R.id.et_edit_folder_name));

        mFabAdd.show();
    }

    public void closeKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager.isActive()) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        mResultCode=Activity.RESULT_OK;
        if (EditFolderConstants.selectedItem == position) {    // 如果当前item正在编辑
            EditText et = (EditText) mEditFolderAdapter.getViewByPosition(position, R.id.et_edit_folder_name);
            String newName = et.getText().toString();
            verifyName(position,newName);
        } else if(EditFolderConstants.selectedItem!=-1){       // 已有其他Item正在被编辑
            int i=EditFolderConstants.selectedItem;
            EditFolderConstants.selectedItem=-1;
            mEditFolderAdapter.notifyItemChanged(i);
            editItem(position);
        } else{                                              // 无任何item正在被编辑
            editItem(position);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_folder_add:
                addFolder();
                break;
        }
    }

    public void addFolder() {
        mResultCode = Activity.RESULT_OK;
        mFabAdd.hide();
        NoteFolder folder = new NoteFolder();
        // 已有item处于编辑状态，则让其恢复为正常状态
        if (EditFolderConstants.selectedItem != -1) {
            int i = EditFolderConstants.selectedItem;
            EditFolderConstants.selectedItem = -1;
            mEditFolderAdapter.notifyItemChanged(i);
        }

        String newName = getNewFolderName();
        folder.setFolderName(newName);
        folder.save();
        mEditFolderAdapter.addData(folder);
        scrollToItem(mEditFolderAdapter.getData().size() - 1);

        // 新建便签夹的弹出键盘在Adapter中设置（因为RecyclerView的scrollToItem需要一定的时间）
        EditFolderConstants.isNewFolder = true;
        EditFolderConstants.selectedItem = mEditFolderAdapter.getData().size() - 1;
    }

    private String getNewFolderName() {

        List<NoteFolder> list = new ArrayList<>();

        // 找出所有包含新建便签夹的名字
        for (int i = 0; i < mEditFolderAdapter.getData().size(); i++) {
            NoteFolder folder = mEditFolderAdapter.getData().get(i);
            if (folder.getFolderName().contains("新建便签夹")) {
                list.add(folder);
            }
        }

        // 从新建便签夹1 -> 新建便签夹2 依次尝试
        int n = 1;
        while (true) {
            String newName;
            if (n == 1) {
                newName = "新建便签夹";
            } else {
                newName = "新建便签夹" + n;
            }
            int i;
            for (i = 0; i < list.size(); i++) {
                if (list.get(i).getFolderName().equals(newName)) {
                    break;
                }
            }
//            没有包含新建便签夹的项 或者 已有的项都与newName不一样
            if (list.size() == 0 || i == list.size()) {
                return newName;
            }
            n++;
        }
    }

    class DeleteFoldersTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            mResultCode = Activity.RESULT_OK;
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(NoteFolderManageActivity.this);
            }
            mProgressDialog.setMessage("删除中...");
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // 当前正在编辑的便签夹是否被删除
            boolean isSelectedDeleted = false;
            for (int i = mEditFolderAdapter.mCheckList.size() - 1; i >= 0; i--) {
                if (mEditFolderAdapter.mCheckList.get(i)) {
                    if (i == EditFolderConstants.selectedItem) {
                        isSelectedDeleted = true;
                    }
                    NoteFolder folder = mEditFolderAdapter.getData().get(i);

                    // 判断删除的是否是当前主页已选中的便签夹
                    if (folder.getId() == mCurrentFolderId)
                        mIsCurrentFolderDeleted = true;

                    mEditFolderAdapter.getData().remove(i);
                    mEditFolderAdapter.mCheckList.remove(i);
                    mNoteFolderModel.deleteNoteFolder(folder);
                }
            }
            return isSelectedDeleted;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (mProgressDialog != null)
                mProgressDialog.cancel();
            EditFolderConstants.selectedCount = 0;

            // 判断当前编辑的便签是否被删除
            if (aBoolean) {
                mFabAdd.show();
                EditFolderConstants.selectedItem = -1;
            }
            mEditFolderAdapter.notifyDataSetChanged();
        }
    }
}
