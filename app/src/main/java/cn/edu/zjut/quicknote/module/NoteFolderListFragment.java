package cn.edu.zjut.quicknote.module;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.adapter.NoteFolderAdapter;
import cn.edu.zjut.quicknote.bean.Note;
import cn.edu.zjut.quicknote.bean.NoteFolder;
import cn.edu.zjut.quicknote.constants.CacheManager;
import cn.edu.zjut.quicknote.constants.Constants;
import cn.edu.zjut.quicknote.constants.FolderListConstants;
import cn.edu.zjut.quicknote.constants.NoteListConstants;
import cn.edu.zjut.quicknote.model.LoadDataCallBack;
import cn.edu.zjut.quicknote.model.NoteFolderModel;
import cn.edu.zjut.quicknote.utils.ThemeUtils;
import cn.edu.zjut.quicknote.widget.MyDrawable;

public class NoteFolderListFragment extends Fragment implements View.OnClickListener {
    @BindView(R.id.rv_note_list_folder)
    RecyclerView mRvNoteFolder;   // 便签夹列表


    private RelativeLayout mRlAllFolder;
    private ImageView mIvAllIcon;
    private TextView mTvAllTitle;
    private TextView mTvAllCount;

    private RelativeLayout mRlRecycleFolder;
    private ImageView mIvRecycleIcon;
    private TextView mTvRecycleTitle;

    private NoteFolderModel mNoteFolderModel = new NoteFolderModel();
    private NoteFolderAdapter mFolderAdapter;

    private NoteListActivity mActivity;

    private Unbinder mUnbinder;

    private BaseQuickAdapter.OnItemClickListener mNoteItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            choiceFolder(position, false);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getActivity() != null;
        mActivity = (NoteListActivity) getActivity();
        mActivity.setFragment(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_forder_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mFolderAdapter = new NoteFolderAdapter();
        mFolderAdapter.addHeaderView(getFolderHeaderView());
        mFolderAdapter.addFooterView(getFolderFooterView());
        mFolderAdapter.addHeaderView(getFolderHeaderView2());
        mRvNoteFolder.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvNoteFolder.setAdapter(mFolderAdapter);

        mFolderAdapter.setOnItemClickListener(mNoteItemClickListener);
        return view;
    }

    public int initDatabase() {
        return mNoteFolderModel.initNoteFolderAndGetFolderId();
    }

    private View getFolderHeaderView() {
        return LayoutInflater.from(getActivity()).inflate(R.layout.layout_folder_hearder,
                null, false);
    }

    private View getFolderHeaderView2() {

        View headerView2 = LayoutInflater.from(getActivity()).inflate(R.layout.layout_folder_hearder_2, null, false);

        mRlAllFolder = headerView2.findViewById(R.id.rl_folder_all);
        mIvAllIcon = headerView2.findViewById(R.id.iv_folder_all_icon);
        mTvAllTitle = headerView2.findViewById(R.id.tv_folder_all_title);
        mTvAllCount = headerView2.findViewById(R.id.tv_folder_all_count);
        TextView tvToEdit = headerView2.findViewById(R.id.tv_folder_to_edit);

        mRlAllFolder.setOnClickListener(this);
        tvToEdit.setOnClickListener(this);

        return headerView2;
    }

    public int getCurrentFolderId() {
        switch (Constants.currentFolder) {
            case FolderListConstants.ITEM_ALL:
                return Constants.currentFolder;
            case FolderListConstants.ITEM_PRIMARY:
                return Constants.currentFolder;
            case FolderListConstants.ITEM_RECYCLE:
                return Constants.currentFolder;
            default:
                return mFolderAdapter.getData().get(Constants.currentFolder).getId();
        }
    }

    private View getFolderFooterView() {

        View foolderView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_folder_footer, null, false);

        mRlRecycleFolder = foolderView.findViewById(R.id.rl_folder_recycle_bin);
        mIvRecycleIcon = foolderView.findViewById(R.id.img_folder_recycle_bin_ic);
        mTvRecycleTitle = foolderView.findViewById(R.id.tv_folder_recycle_bin_title);

        mRlRecycleFolder.setOnClickListener(this);

        return foolderView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public void getFolders() {
        mNoteFolderModel.loadNoteFoldersList(new LoadDataCallBack<NoteFolder>() {
            @Override
            protected void onSucceed(List<NoteFolder> list) {
                mFolderAdapter.setNewData(list);
                FolderListConstants.noteFolderCount = 0;
                for (int i = 0; i < list.size(); i++) {
                    FolderListConstants.noteFolderCount += list.get(i).getNoteCount();
                }
                mTvAllCount.setText(String.valueOf(FolderListConstants.noteFolderCount));
            }
        });
    }

    public void choiceFolder(int pos, boolean isInit) {
        if (!isInit && pos == Constants.currentFolder) {
            return;
        }

        // 检查当前Folder是否存在
        pos = checkCurrentFolderIsExist(pos);

        switch (pos) {
            case FolderListConstants.ITEM_ALL:
                unChoiceFolder(Constants.currentFolder);
                CacheManager.setAndSaveCurrentFolder(pos);
                choiceItemAll();
                mActivity.showAllNote();
                break;
            case FolderListConstants.ITEM_RECYCLE:
                unChoiceFolder(Constants.currentFolder);
                Constants.currentFolder = pos;
                choiceItemRecycleBin();
                mActivity.showRecycleBinNote();
                // 私密便签不保存状态
                break;
            default:
                List<NoteFolder> data = mFolderAdapter.getData();
                unChoiceFolder(Constants.currentFolder);
                CacheManager.setAndSaveCurrentFolder(pos);
                mFolderAdapter.notifyItemChanged(mFolderAdapter.getHeaderLayoutCount() + pos);
                mActivity.showNormalNote(data.get(pos).getFolderName(), data.get(pos).getId());
                break;
        }
    }

    public void unChoiceFolder(int pos) {
        switch (pos) {
            case FolderListConstants.ITEM_ALL:

                mRlAllFolder.setSelected(false);
                mIvAllIcon.setBackgroundResource(R.drawable.ic_folder_un_selected);
                mTvAllTitle.setTextColor(mActivity.getResources().getColor(R.color.colorBlackAlpha87));
                mTvAllCount.setTextColor(mActivity.getResources().getColor(R.color.colorBlackAlpha54));
                break;
            case FolderListConstants.ITEM_RECYCLE:
                mRlRecycleFolder.setSelected(true);
                mIvRecycleIcon.setBackground(MyDrawable.getIcFolderSelectedDrawable(ThemeUtils.getColorPrimary(mActivity)));
                mTvRecycleTitle.setTextColor(ThemeUtils.getColorPrimary(mActivity));
                break;
            default:
                mFolderAdapter.notifyItemChanged(mFolderAdapter.getHeaderLayoutCount() + pos);
                break;
        }
    }

    public void choiceItemAll() {

        mRlAllFolder.setSelected(true);
        mIvAllIcon.setBackground(MyDrawable.getIcFolderSelectedDrawable(ThemeUtils.getColorPrimary(mActivity)));
        mTvAllTitle.setTextColor(ThemeUtils.getColorPrimary(mActivity));
        mTvAllCount.setTextColor(ThemeUtils.getColorPrimary(mActivity));
    }

    public void choiceItemRecycleBin() {
        mRlRecycleFolder.setSelected(true);
        mIvRecycleIcon.setBackground(MyDrawable.getIcFolderSelectedDrawable(ThemeUtils.getColorPrimary(mActivity)));
        mTvRecycleTitle.setTextColor(ThemeUtils.getColorPrimary(mActivity));
    }

    private int checkCurrentFolderIsExist(int pos) {
        switch (pos) {
            case FolderListConstants.ITEM_ALL:
                return pos;
            case FolderListConstants.ITEM_PRIMARY:
                return pos;
            case FolderListConstants.ITEM_RECYCLE:
                return pos;
            default:
                if (pos >= mFolderAdapter.getData().size()) {
                    return FolderListConstants.ITEM_ALL;
                } else
                    return pos;
        }
    }

    public void addNote2ALL(Note note) {
        NoteFolder folder = mFolderAdapter.getData().get(0);
        mNoteFolderModel.addNote2Folder(note, folder);
        mTvAllCount.setText(String.valueOf(++FolderListConstants.noteFolderCount));
        mFolderAdapter.notifyItemChanged(mFolderAdapter.getHeaderLayoutCount());
    }


    public void addNote2Normal(Note note) {
        NoteFolder folder = mFolderAdapter.getData().get(Constants.currentFolder);
        mNoteFolderModel.addNote2Folder(note, folder);
        mTvAllCount.setText(String.valueOf(++FolderListConstants.noteFolderCount));
        mFolderAdapter.notifyItemChanged(
                Constants.currentFolder + mFolderAdapter.getHeaderLayoutCount());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_folder_all:
                choiceFolder(FolderListConstants.ITEM_ALL, false);
                break;
            case R.id.rl_folder_recycle_bin:
                choiceFolder(FolderListConstants.ITEM_RECYCLE,false);
                break;
            case R.id.tv_folder_to_edit:
                toEditFolderActivity();
                break;
        }
    }

    private void toEditFolderActivity() {
        Intent intent = new Intent(getActivity(), NoteFolderManageActivity.class);
        intent.putExtra("current_folder_id", getCurrentFolderId());
        mActivity.startActivityForResult(intent, NoteListConstants.REQUEST_CODE_EDIT_FOLDER);
    }

    public void refreshFolderList() {
        mFolderAdapter.notifyDataSetChanged();
        mTvAllCount.setText(String.valueOf(FolderListConstants.noteFolderCount));
    }

    public void recoverNote(Note note) {
        note.setInRecycleBin(0);
        note.save();
        for (int i = 0; i < mFolderAdapter.getData().size(); i++) {
            NoteFolder folder = mFolderAdapter.getData().get(i);
            if (folder.getId() == note.getNoteFolderId()) {

                folder.setNoteCount(folder.getNoteCount() + 1);
                folder.save();

                FolderListConstants.noteFolderCount++;
                break;
            }
        }
    }

    public void removeNoteForFolder(Note note) {
        switch (Constants.currentFolder) {
            case FolderListConstants.ITEM_ALL:
                removeNoteForAllNoteAndNormal(note);
                break;
            case FolderListConstants.ITEM_RECYCLE:
                break;
            default:
                removeNoteForAllNoteAndNormal(note);
                break;
        }
    }

    private void removeNoteForAllNoteAndNormal(Note note) {
        for (int i = 0; i < mFolderAdapter.getData().size(); i++) {
            NoteFolder folder1 = mFolderAdapter.getData().get(i);
            if (folder1.getId() == note.getNoteFolderId()) {
                folder1.setNoteCount(folder1.getNoteCount() - 1);
                folder1.save();
                FolderListConstants.noteFolderCount--;
                break;
            }
        }
    }

    public List<NoteFolder> getFolderList() {
        return mFolderAdapter.getData();
    }

    public void moveNoteToFolder(Note note, NoteFolder noteFolder) {
        if (note.getNoteFolderId() != noteFolder.getId()) {
            removeNoteForFolder(note);
            noteFolder.setNoteCount(noteFolder.getNoteCount() + 1);
            noteFolder.save();
            note.setNoteFolderId(noteFolder.getId());
            note.save();
            FolderListConstants.noteFolderCount++;
        }
    }
}
