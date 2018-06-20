package cn.edu.zjut.quicknote.module;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.zjut.quicknote.MainApplication;
import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.adapter.NoteAdapter;
import cn.edu.zjut.quicknote.adapter.NoteBottomSheetFolderAdapter;
import cn.edu.zjut.quicknote.bean.Note;
import cn.edu.zjut.quicknote.bean.NoteFolder;
import cn.edu.zjut.quicknote.constants.CacheManager;
import cn.edu.zjut.quicknote.constants.Constants;
import cn.edu.zjut.quicknote.constants.FolderListConstants;
import cn.edu.zjut.quicknote.constants.NoteListConstants;
import cn.edu.zjut.quicknote.model.LoadDataCallBack;
import cn.edu.zjut.quicknote.model.NoteModel;
import cn.edu.zjut.quicknote.utils.ProgressDialogUtils;

import static cn.edu.zjut.quicknote.constants.NoteListConstants.isShowMultiSelectAction;

public class NoteListActivity extends AppCompatActivity {
    @BindView(R.id.rl_note_list_bottom_bar)
    RelativeLayout mRlBottomBar;

    @BindView(R.id.tv_note_list_delete)
    TextView mTvDelete;

    @BindView(R.id.tv_note_list_move)
    TextView mTvMove;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rv_note_list)
    RecyclerView mNoteListRv;

    @BindView(R.id.fab_note_list_add)
    FloatingActionButton mAddNoteFab;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    private NoteModel mNoteModel = new NoteModel();

    private MenuItem mSearchMenu;
    private MenuItem mShowModeMenu;
    private MenuItem mCheckAllMenu;

    private NoteFolderListFragment mFragment;

    private NoteAdapter mAdapter = new NoteAdapter();

    private ProgressDialogUtils mProgressDialog = new ProgressDialogUtils(this);

    BaseQuickAdapter.OnItemChildClickListener mOnRvClickListener = new BaseQuickAdapter.OnItemChildClickListener() {
        @Override
        public void onItemChildClick(BaseQuickAdapter adapter, View view, final int position) {
            if (isShowMultiSelectAction) {
                choiceNote(position);
            } else {
                if (Constants.currentFolder == FolderListConstants.ITEM_RECYCLE) {
                    new AlertDialog.Builder(NoteListActivity.this)
                            .setMessage("无法直接打开便签，是否恢复至原有便签夹？")
                            .setPositiveButton("恢复",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            recoverNote(position);
                                        }
                                    })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    toEditNoteForEdit(position);
                }
            }
        }
    };

    BaseQuickAdapter.OnItemChildLongClickListener mOnRvLongClickListener = new BaseQuickAdapter.OnItemChildLongClickListener() {
        @Override
        public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
            if (!NoteListConstants.isInSearch) {
                if (!isShowMultiSelectAction) {
                    isShowMultiSelectAction = true;
                    supportInvalidateOptionsMenu();
                    mAddNoteFab.hide();
                    showBottomBar();
                    setCheckMenuEnable();
                    setCheckMenuForFolderType();
                    choiceNote(position);
                    setTitle(String.valueOf(NoteListConstants.selectedCount));
                    mAdapter.notifyDataSetChanged();
                    mFragment.refreshFolderList();
                }
            }
            return true;
        }
    };

    private SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            setFilter(newText);
            return true;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        initData();
        initDrawer();
        initAdapter();
        mNoteListRv.setAdapter(mAdapter);
        mNoteListRv.setLayoutManager(
                new StaggeredGridLayoutManager(2,
                        StaggeredGridLayoutManager.VERTICAL));
        updateViews();
    }

    private void initDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initData() {
        if (Constants.isFirst) {
            int folderId = mFragment.initDatabase();
           // mNoteModel.initNote(folderId);
            CacheManager.setAndSaveIsFirst(false);
        }
    }

    private void updateViews() {
        mFragment.getFolders();
        mFragment.choiceFolder(Constants.currentFolder, true);
    }

    private void initAdapter() {
        mAdapter.setOnItemChildClickListener(mOnRvClickListener);
        mAdapter.setOnItemChildLongClickListener(mOnRvLongClickListener);
        mAdapter.setEmptyView(getRvEmptyView());
    }

    @OnClick(R.id.tv_note_list_delete)
    public void onDeleteNotes() {
        showDeleteDialog();
    }

    @OnClick(R.id.tv_note_list_move)
    public void onMoveNotes() {
        if (Constants.currentFolder == FolderListConstants.ITEM_RECYCLE) {
            new RecoverNotesTask().execute();
        } else {
            showMoveBottomSheet();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mSearchMenu = menu.findItem(R.id.menu_note_search);
        initSearchMenu(mSearchMenu);

        mShowModeMenu = menu.findItem(R.id.menu_note_show_mode);
        if (Constants.noteListShowMode == NoteListConstants.STYLE_LINEAR) {
            mShowModeMenu.setIcon(getResources()
                    .getDrawable(NoteListConstants.MODE_GRID));
        } else {
            mShowModeMenu.setIcon(getResources()
                    .getDrawable(NoteListConstants.MODE_LIST));
        }
        mCheckAllMenu = menu.findItem(R.id.menu_note_check_all);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isShowMultiSelectAction) {
            mSearchMenu.setVisible(false);
            mShowModeMenu.setVisible(false);
            mCheckAllMenu.setVisible(true);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowMultiSelectAction) {
                        cancelMultiSelectAction();
                    }
                }
            });
        } else {
            mSearchMenu.setVisible(true);
            mShowModeMenu.setVisible(true);
            mCheckAllMenu.setVisible(false);
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                    this, mDrawer, mToolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    if (isShowMultiSelectAction) {
                        cancelMultiSelectAction();
                    }
                    mAddNoteFab.show();
                }
            };
            mDrawer.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_note_show_mode:
                changeNoteRvLayoutManagerAndMenuIcon(item);
                break;
            case R.id.menu_note_check_all:
                doChoiceAllNote();
                break;
            case R.id.menu_note_search:
                mAddNoteFab.hide();
                NoteListConstants.isInSearch = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeNoteRvLayoutManagerAndMenuIcon(MenuItem item) {

        if (Constants.noteListShowMode == NoteListConstants.STYLE_LINEAR) {
            CacheManager.setAndSaveNoteListShowMode(NoteListConstants.STYLE_GRID);
            mNoteListRv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            mAdapter.notifyDataSetChanged();
            mFragment.refreshFolderList();
            item.setIcon(getResources().getDrawable(NoteListConstants.MODE_LIST));
        } else {
            CacheManager.setAndSaveNoteListShowMode(NoteListConstants.STYLE_LINEAR);
            mNoteListRv.setLayoutManager(new LinearLayoutManager(this));
            mAdapter.notifyDataSetChanged();
            mFragment.refreshFolderList();
            item.setIcon(MainApplication.mContext.getResources().getDrawable(NoteListConstants.MODE_GRID));
        }
    }

    public void doChoiceAllNote() {
        if (NoteListConstants.isChoiceAll) {
            NoteListConstants.isChoiceAll = false;
            for (int i = 0; i < mAdapter.mCheckList.size(); i++) {
                mAdapter.mCheckList.set(i, false);
            }
            setNoteSelectedCount(0);
            mAdapter.notifyDataSetChanged();
            setTitle(String.valueOf(NoteListConstants.selectedCount));
        } else {
            NoteListConstants.isChoiceAll = true;
            for (int i = 0; i < mAdapter.mCheckList.size(); i++) {
                mAdapter.mCheckList.set(i, true);
            }
            setNoteSelectedCount(mAdapter.mCheckList.size());
            mAdapter.notifyDataSetChanged();
            setTitle(String.valueOf(NoteListConstants.selectedCount));
        }
    }

    private void initSearchMenu(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        // 搜索View的文字改变事件
        searchView.setOnQueryTextListener(mQueryTextListener);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                cancelFilter();
                mAddNoteFab.show();
                NoteListConstants.isInSearch = false;
                return true;
            }
        });
    }

    public void setFilter(String text) {
        if (mAdapter.mAllDataList == null) {
            mAdapter.mAllDataList = new ArrayList<>();
            mAdapter.mAllDataList.addAll(mAdapter.getData());
        }
        if (mAdapter.mAllCheckList == null) {
            mAdapter.mAllCheckList = new ArrayList<>();
            mAdapter.mAllCheckList.addAll(mAdapter.mCheckList);
        }

        mAdapter.getData().clear();
        mAdapter.mCheckList.clear();
//        转换为小写
        String lowerCaseQuery = text.toLowerCase();
        //　此处使用倒叙进行检索，这样搜索出来的顺序是正序
        for (int i = mAdapter.mAllDataList.size() - 1; i >= 0; i--) {
            if (mAdapter.mAllDataList.get(i).getNoteContent().toLowerCase().contains(lowerCaseQuery)) {
                mAdapter.addData(mAdapter.mAllDataList.get(i));
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void cancelFilter() {
        NoteListConstants.isInSearch = false;
        if (mAdapter.mAllDataList != null || mAdapter.mAllCheckList != null) {
            mAdapter.getData().clear();
            mAdapter.mCheckList.clear();
            mAdapter.setNewData(mAdapter.mAllDataList);
            for (int i = 0; i < mAdapter.mAllDataList.size(); i++) {
                mAdapter.mCheckList.add(false);
            }
            mAdapter.mAllDataList = null;
            mAdapter.mAllCheckList = null;
            mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("InflateParams")
    private View getRvEmptyView() {
        return LayoutInflater.from(this).inflate(R.layout.layout_empty, null, false);
    }

    @OnClick(R.id.fab_note_list_add)
    public void onClickAddNoteFab() {
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("is_add", true);
        startActivityForResult(intent, NoteListConstants.REQUEST_CODE_ADD);
    }

    public void toEditNoteForEdit(int position) {
        Note note = mAdapter.getData().get(position);
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("is_add", false);
        intent.putExtra("note_id", note.getNoteId());
        intent.putExtra("note_content", note.getNoteContent());
        intent.putExtra("modified_time", note.getModifiedTime());
        if (note.getPath() != null) {
            intent.putExtra("path", note.getPath());
        }
        startActivityForResult(intent, NoteListConstants.REQUEST_CODE_EDIT);
    }

    public void choiceNote(int position) {
        boolean isChoice = mAdapter.mCheckList.get(position);
        if (isChoice) {
            setNoteSelectedCount(NoteListConstants.selectedCount - 1);
        } else {
            setNoteSelectedCount(NoteListConstants.selectedCount + 1);
        }
        mAdapter.mCheckList.set(position, !isChoice);
        mAdapter.notifyItemChanged(position);
        setTitle(String.valueOf(NoteListConstants.selectedCount));
    }


    public void setNoteSelectedCount(int count) {
        NoteListConstants.selectedCount = count;
        NoteListConstants.isChoiceAll =
                NoteListConstants.selectedCount == mAdapter.getData().size();
        if (NoteListConstants.selectedCount > 0) {
            setCheckMenuEnable();
        } else {
            setCheckMenuUnEnable();
        }
    }

    public void setCheckMenuEnable() {

        setButtonEnabled(true, mTvDelete);
        setButtonEnabled(true, mTvMove);
    }

    public void setCheckMenuUnEnable() {
        setButtonEnabled(false, mTvDelete);
        setButtonEnabled(false, mTvMove);
    }

    private void setButtonEnabled(boolean enabled, TextView view) {
        view.setEnabled(enabled);
        if (enabled) {
            view.setTextColor(getResources().getColor(R.color.white));
        } else {
            view.setTextColor(getResources().getColor(R.color.colorWhiteAlpha30));
        }
    }


    public void showBottomBar() {
        mRlBottomBar.setVisibility(View.VISIBLE);
        //        bottombar进行一个上移的动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(mRlBottomBar, "translationY", SizeUtils.dp2px(56), 0);
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        animator.start();
    }

    private void setCheckMenuForFolderType() {
        switch (Constants.currentFolder) {
            case FolderListConstants.ITEM_RECYCLE:
                mTvDelete.setText("删除");
                mTvMove.setText("恢复");

                mTvDelete.setVisibility(View.VISIBLE);
                mTvMove.setVisibility(View.VISIBLE);
                break;
            default:
                mTvDelete.setText("删除");
                mTvMove.setText("移动");
                mTvDelete.setVisibility(View.VISIBLE);
                mTvMove.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
//            case NoteListConstants.REQUEST_CODE_LOCK:
//                resultForLock(resultCode);
//                break;
            case NoteListConstants.REQUEST_CODE_ADD:
                addNote(data);
                break;
            case NoteListConstants.REQUEST_CODE_EDIT:
                updateNote(data);
                break;
            case NoteListConstants.REQUEST_CODE_EDIT_FOLDER:
                resultForEditFolder(resultCode, data);
                break;
        }
    }

    private void resultForEditFolder(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            boolean isCurrentFolderDeleted = data.getBooleanExtra("is_current_folder_deleted", false);
            if (isCurrentFolderDeleted) {
                Constants.currentFolder = FolderListConstants.ITEM_ALL;
            }
            mFragment.getFolders();
            mFragment.choiceFolder(Constants.currentFolder, true);
        }
    }

    private void updateNote(Intent data) {
        int position = data.getIntExtra("position", 0);
        String content = data.getStringExtra("note_content");
        long modifiedTime = data.getLongExtra("modified_time", 0);
        String path = data.getStringExtra("path");
        Note note = mAdapter.getData().get(position);

        if (TextUtils.isEmpty(content)) {
            removeNote(position);
            deleteNote(note, true);
            mAdapter.notifyDataSetChanged();
            mFragment.refreshFolderList();
        } else {
            note.setNoteContent(content);
            note.setModifiedTime(modifiedTime);
            note.setPath(path);
            note.save();
            mAdapter.notifyItemChanged(position);
        }
    }

    public void addNote(Intent data) {
        String noteId = data.getStringExtra("note_id");
        String content = data.getStringExtra("note_content");
        long modifiedTime = data.getLongExtra("modified_time", 0);
        String path = data.getStringExtra("path");
        Note note = new Note();
        note.setNoteId(noteId);
        note.setNoteContent(content);
        note.setCreatedTime(modifiedTime);
        note.setModifiedTime(modifiedTime);
        note.setPath(path);
        note.save();
        addNote2Folder(note);
        mAdapter.addData(note);
        mNoteListRv.scrollToPosition(0);
    }

    private void addNote2Folder(Note note) {
        switch (Constants.currentFolder) {
            case FolderListConstants.ITEM_ALL:
                mFragment.addNote2ALL(note);
                break;
            case FolderListConstants.ITEM_RECYCLE:
                break;
            default:
                mFragment.addNote2Normal(note);
                break;
        }
    }

    private void removeNote(int position) {
        if (mAdapter.mAllDataList != null) {  // 如果已进入搜索模式
            mAdapter.mAllDataList.remove(mAdapter.getData().get(position));
        }
        Note note = mAdapter.getData().get(position);
        mAdapter.getData().remove(position);    // 从Adapter的数据中删除
        mAdapter.mCheckList.remove(position);

        mFragment.removeNoteForFolder(note);   // 从便签夹中移除
    }

    private void deleteNote(Note note, boolean isRealDelete) {

        // 删除操作
        if (isRealDelete || Constants.currentFolder == FolderListConstants.ITEM_RECYCLE) {       // 当前便签夹是废纸篓，则直接永久删除
            deleteFile(note.getNoteId());
            note.delete();
        } else {
            if (Constants.isUseRecycleBin) { // 已启用废纸篓
                toRecycleBin(note);
            } else {                       // 已关闭废纸篓
                note.delete();
                deleteFile(note.getNoteId());
            }
        }
    }

    private void toRecycleBin(Note note) {
        note.setInRecycleBin(1);
        note.save();
    }

    public void setFragment(NoteFolderListFragment fragment) {
        mFragment = fragment;
    }

    public void showAllNote() {
        mNoteModel.loadAllNoteList(new LoadDataCallBack<Note>() {
            @Override
            protected void onSucceed(List<Note> list) {
                mAdapter.setNewData(list);
            }
        });
        mDrawer.closeDrawer(GravityCompat.START);
        setTitle("全部");
        NoteListConstants.selectedFolderName = "全部";
        mAddNoteFab.show();
    }

    public void showRecycleBinNote() {
        mNoteModel.loadRecycleBinNoteList(new LoadDataCallBack<Note>() {
            @Override
            protected void onSucceed(List<Note> list) {
                mAdapter.setNewData(list);
            }
        });
        mDrawer.closeDrawer(GravityCompat.START);
        setTitle("废纸篓");
        mAddNoteFab.hide();
    }

    public void recoverNote(int position) {
        Note note = mAdapter.getData().get(position);
        if (mAdapter.mAllDataList != null) {
            mAdapter.mAllDataList.remove(note);
        }
        mAdapter.getData().remove(position);
        mAdapter.mCheckList.remove(position);
        mFragment.recoverNote(note);
    }

    public void showNormalNote(String title, int folderId) {
        mNoteModel.loadNormalNoteList(folderId, new LoadDataCallBack<Note>() {
            @Override
            protected void onSucceed(List<Note> list) {
                mAdapter.setNewData(list);
            }
        });
        mDrawer.closeDrawer(GravityCompat.START);
        setTitle(title);
        mAddNoteFab.show();
    }

    public void showMoveBottomSheet() {
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
//                    获取contentView
        ViewGroup contentView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        View root = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_folder, contentView, false);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_bottom_sheet_folder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dialog.setContentView(root);

        recyclerView.setAdapter(getBottomSheetRvAdapter(dialog));

        dialog.show();
    }

    private NoteBottomSheetFolderAdapter getBottomSheetRvAdapter(final BottomSheetDialog dialog) {
        final NoteBottomSheetFolderAdapter folderAdapter = new NoteBottomSheetFolderAdapter();
        folderAdapter.setNewData(mFragment.getFolderList());
        folderAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                NoteFolder folder = folderAdapter.getData().get(position);
                new MoveNotesTask(folder).execute();
                dialog.cancel();
            }
        });
        return folderAdapter;
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除便签")
                .setMessage("确定删除选中的便签吗？")
                .setPositiveButton("删除",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteNotesTask().execute();
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    public void hideBottomBar() {
        // 下移动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(mRlBottomBar, "translationY", SizeUtils.dp2px(56));
        animator.setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRlBottomBar.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    public void cancelMultiSelectAction() {
        if (isShowMultiSelectAction) {
            isShowMultiSelectAction = false;
            // 更新toolbar菜单
            supportInvalidateOptionsMenu();
            // 隐藏BottomBar
            hideBottomBar();
            // 将所有便签的选中状态设为false
            NoteListConstants.isChoiceAll = false;
            for (int i = 0; i < mAdapter.mCheckList.size(); i++) {
                mAdapter.mCheckList.set(i, false);
            }
            setNoteSelectedCount(0);
            mAdapter.notifyDataSetChanged();
            setTitle(String.valueOf(NoteListConstants.selectedCount));
            // 显示已选中的便签夹名称
            setTitle(NoteListConstants.selectedFolderName);
            // 刷新便签RecyclerView
            mAdapter.notifyDataSetChanged();
            mFragment.refreshFolderList();
            // 显示添加按钮
            mAddNoteFab.show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteNotesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show("删除中...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = mAdapter.mCheckList.size() - 1; i >= 0; i--) {
                if (mAdapter.mCheckList.get(i)) {
                    Note note = mAdapter.getData().get(i);
                    removeNote(i);
                    deleteNote(note, false);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.hide();

            mAdapter.notifyDataSetChanged();
            mFragment.refreshFolderList();
            cancelMultiSelectAction();
        }

    }

    @SuppressLint("StaticFieldLeak")
    class RecoverNotesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show("恢复中...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = mAdapter.mCheckList.size() - 1; i >= 0; i--) {
                if (mAdapter.mCheckList.get(i)) {
                    recoverNote(i);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.hide();
            mAdapter.notifyDataSetChanged();
            mFragment.refreshFolderList();
            cancelMultiSelectAction();
        }


    }

    @SuppressLint("StaticFieldLeak")
    class MoveNotesTask extends AsyncTask<Void, Void, Boolean> {
        private NoteFolder mNoteFolder;

        MoveNotesTask(NoteFolder noteFolder) {
            mNoteFolder = noteFolder;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.show("移动中...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = mAdapter.mCheckList.size() - 1; i >= 0; i--) {
                if (mAdapter.mCheckList.get(i)) {
                    moveNote(i, mNoteFolder);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.hide();
            int count = NoteListConstants.selectedCount;
            cancelMultiSelectAction();
            mAdapter.notifyDataSetChanged();
            mFragment.refreshFolderList();
            Snackbar.make(mAddNoteFab,
                    "已将" + count + "条便签移动到" + mNoteFolder.getFolderName(),
                    Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }

        private void moveNote(int notePos, NoteFolder noteFolder) {
            Note note = mAdapter.getData().get(notePos);
            // 如果当前便签为全部便签，则不用从当前NoteRv中移除
            if (Constants.currentFolder == FolderListConstants.ITEM_ALL) {
                mFragment.moveNoteToFolder(note, noteFolder);
            } else {
                if (noteFolder.getId() != mFragment.getCurrentFolderId()) {
                    if (mAdapter.mAllDataList != null) {
                        mAdapter.mAllDataList.remove(note);
                    }
                    mAdapter.getData().remove(notePos);
                    mAdapter.mCheckList.remove(notePos);

                    mFragment.moveNoteToFolder(note, noteFolder);

                }
            }
        }
    }
}
