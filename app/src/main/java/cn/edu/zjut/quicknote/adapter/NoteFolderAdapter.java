package cn.edu.zjut.quicknote.adapter;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import cn.edu.zjut.quicknote.MainApplication;
import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.bean.NoteFolder;
import cn.edu.zjut.quicknote.constants.Constants;
import cn.edu.zjut.quicknote.utils.ThemeUtils;
import cn.edu.zjut.quicknote.widget.MyDrawable;

/**
 * <pre>
 *     author : FaDai
 *     e-mail : i_fadai@163.com
 *     time   : 2017/06/02
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */

public class NoteFolderAdapter extends BaseQuickAdapter<NoteFolder, BaseViewHolder> {

    public NoteFolderAdapter() {
        super(R.layout.item_folder);
    }

    @Override
    protected void convert(BaseViewHolder helper, NoteFolder item) {
        helper.setText(R.id.tv_folder_list_title, item.getFolderName())
                .setText(R.id.tv_folder_list_count, item.getNoteCount() + "");


        RelativeLayout rlItem=helper.getView(R.id.rl_folder_root);
        TextView tvTitle=helper.getView(R.id.tv_folder_list_title);
        TextView tvCount=helper.getView(R.id.tv_folder_list_count);
        ImageView ivIcon=helper.getView(R.id.iv_folder_list_ic);

        if(Constants.currentFolder == helper.getLayoutPosition()-getHeaderLayoutCount()){
            rlItem.setSelected(true);
            tvTitle.setTextColor(ThemeUtils.getColorPrimary(mContext));
            tvCount.setTextColor(ThemeUtils.getColorPrimary(mContext));
            ivIcon.setBackground(MyDrawable.getIcFolderSelectedDrawable(ThemeUtils.getColorPrimary(mContext)));
        } else {
            rlItem.setSelected(false);
            tvTitle.setTextColor(MainApplication.getContext().getResources().getColor(R.color.colorBlackAlpha87));
            tvCount.setTextColor(MainApplication.getContext().getResources().getColor(R.color.colorBlackAlpha54));
            ivIcon.setBackgroundResource(R.drawable.ic_folder_un_selected);
        }
    }


}
