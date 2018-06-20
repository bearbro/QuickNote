package cn.edu.zjut.quicknote.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.bean.NoteFolder;



public class NoteBottomSheetFolderAdapter extends BaseQuickAdapter<NoteFolder, BaseViewHolder> {

    public NoteBottomSheetFolderAdapter() {
        super(R.layout.item_note_bottom_folder);
    }

    @Override
    protected void convert(BaseViewHolder helper, NoteFolder item) {
        helper.setText(R.id.tv_folder_title_bottom_sheet,item.getFolderName());
    }


}
