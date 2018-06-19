package cn.edu.zjut.quicknote.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.bean.Note;
import cn.edu.zjut.quicknote.constants.Constants;
import cn.edu.zjut.quicknote.constants.EditNoteConstants;
import cn.edu.zjut.quicknote.constants.NoteListConstants;
import cn.edu.zjut.quicknote.utils.DateUtils;

public class NoteAdapter extends BaseQuickAdapter<Note, BaseViewHolder> {
    public List<Boolean> mCheckList = new ArrayList<>();
    public List<Boolean> mAllCheckList;
    public List<Note> mAllDataList;

    public void addData(@NonNull Note data) {
        addData(0, data);
        mCheckList.add(false);
        notifyDataSetChanged();
    }

    @Override
    public void setNewData(List<Note> data) {
        super.setNewData(data);
        mCheckList.clear();
        for (int i = 0; i < data.size(); i++) {
            mCheckList.add(false);
        }
    }

    public NoteAdapter() {
        super(R.layout.item_note);
    }


    @Override
    protected void convert(BaseViewHolder helper, Note item) {
        if (isLinearLayoutManager())
            setLinearLayout(helper, item);
        else if (isGridLayoutManager())
            setGridLayout(helper, item);
    }

    private boolean isLinearLayoutManager() {
        return Constants.noteListShowMode == NoteListConstants.STYLE_LINEAR;
    }

    private boolean isGridLayoutManager() {
        return Constants.noteListShowMode == NoteListConstants.STYLE_GRID;
    }

    private void setGridLayout(BaseViewHolder helper, Note item) {

        helper.addOnClickListener(R.id.cv_note_list_grid);
        helper.addOnLongClickListener(R.id.cv_note_list_grid);

        helper.getView(R.id.ll_note_list_linear).setVisibility(View.GONE);
        helper.getView(R.id.cv_note_list_grid).setVisibility(View.VISIBLE);
        TextView tvContent = helper.getView(R.id.tv_note_list_grid_content);
        parseTextContent(tvContent, item.getNoteContent());

        setNoteTime(helper, item.getModifiedTime());
        setCheckBox(helper);
    }

    /**
     * 解析文本中的图片
     */
    private String parseTextContent(TextView textView, String content) {

        textView.setText("");
        List<String> images = new ArrayList<>();

        Pattern p = Pattern.compile(EditNoteConstants.imageTabBefore + "([^<]*)" + EditNoteConstants.imageTabAfter);
        Matcher m = p.matcher(content);
        int tempIndex = 0;
        List<String> textList = new ArrayList<>();
        while (m.find()) {
            String temp = m.group(1);
            int index = content.indexOf(EditNoteConstants.imageTabBefore, tempIndex);
            String text = content.substring(tempIndex, index);
            textList.add(text);
            int flagLength = EditNoteConstants.imageTabBefore.length() + EditNoteConstants.imageTabAfter.length();
            tempIndex = index + flagLength + temp.length();
        }

        if (textList.size() != 0) {
            for (int i = 0; i < textList.size(); i++) {
                textView.append(textList.get(i));
                textView.append("[图片]");
            }
            textView.append(content.substring(tempIndex));
        } else {
            textView.setText(content);
        }

        if (images.size() > 0)
            return images.get(0);
        else return null;
    }

    private void setLinearLayout(BaseViewHolder helper, Note item) {

        helper.addOnClickListener(R.id.ll_note_list_line);
        helper.addOnLongClickListener(R.id.ll_note_list_line);

        helper.getView(R.id.ll_note_list_linear).setVisibility(View.VISIBLE);
        helper.getView(R.id.cv_note_list_grid).setVisibility(View.GONE);


        TextView tvContent = helper.getView(R.id.tv_note_list_linear_content);
//        parseTextContent(tvContent, item.getNoteContent());

        String imageName = parseTextContent(tvContent, item.getNoteContent());

        if (imageName != null) {

            ImageView imageView = helper.getView(R.id.img_note_list_img);

            File imageFile = new File(item.getPath() + "/" + imageName);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//只读取图片，不加载到内存中
            BitmapFactory.decodeFile(imageFile.getPath(), options);
            //图片宽高都为原来的四分之一，即图片为原来的1/16
            options.inSampleSize = 4;
            options.inJustDecodeBounds = false;//加载到内存中

            Bitmap bm = BitmapFactory.decodeFile(imageFile.getPath(), options);


            imageView.setImageBitmap(bm);

            View imageLay = helper.getView(R.id.ll_note_list_img);
            imageLay.setVisibility(View.VISIBLE);

        } else {
            View imageLay = helper.getView(R.id.ll_note_list_img);
            imageLay.setVisibility(View.GONE);
        }

        setNoteTime(helper, item.getModifiedTime());
        setLinearLayoutGroup(helper, item.getCreatedTime());
        setCheckBox(helper);
    }

    private void setNoteTime(BaseViewHolder helper, long time) {
        long now = TimeUtils.getNowMills();

        if (DateUtils.isInSameDay(now, time))
            setNoteTimeInfo(helper, time, new SimpleDateFormat("HH:mm", Locale.CHINA));
        else if (DateUtils.isInSameYear(now, time))
            setNoteTimeInfo(helper, time, new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA));
        else
            setNoteTimeInfo(helper, time, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA));

    }

    private void setNoteTimeInfo(BaseViewHolder helper, long time, SimpleDateFormat format) {
        if (isLinearLayoutManager()) {
            helper.setText(R.id.tv_note_list_linear_time, TimeUtils.millis2String(time, format));
        } else {
            helper.setText(R.id.tv_note_list_grid_time, TimeUtils.millis2String(time, format));
        }
    }

    private void setLinearLayoutGroup(BaseViewHolder helper, long time) {
        int position = helper.getLayoutPosition();
        if (position == 0 ||
                !DateUtils.isInSameMonth(time, getData().get(position - 1).getCreatedTime())) {
            showLinearLayoutGroup(true, helper, time);
            return;
        }
        showLinearLayoutGroup(false, helper, time);
    }

    private void showLinearLayoutGroup(boolean isShow, BaseViewHolder helper, long time) {
        LinearLayout ll = helper.getView(R.id.ll_note_list_linear);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();
        if (isShow) {
            helper.setVisible(R.id.tv_note_list_linear_month, true);
            setLinearGroupStyle(helper, time);

            params.setMargins(SizeUtils.dp2px(0), SizeUtils.dp2px(8), SizeUtils.dp2px(0), SizeUtils.dp2px(0));
            ll.setLayoutParams(params);

        } else {
            helper.setVisible(R.id.tv_note_list_linear_month, false);
            params.setMargins(SizeUtils.dp2px(0), SizeUtils.dp2px(0), SizeUtils.dp2px(0), SizeUtils.dp2px(0));
            ll.setLayoutParams(params);
        }
    }

    private void setLinearGroupStyle(BaseViewHolder helper, long time) {
        long nowTime = TimeUtils.getNowMills();

        if (DateUtils.isInSameYear(nowTime, time)) {
            helper.setText(R.id.tv_note_list_linear_month,
                    TimeUtils.millis2String(time, new SimpleDateFormat("MM月", Locale.CHINA)));
        } else {
            helper.setText(R.id.tv_note_list_linear_month,
                    TimeUtils.millis2String(time, new SimpleDateFormat("yyyy年MM月", Locale.CHINA)));
        }
    }

    private void setCheckBox(BaseViewHolder helper) {

        int position = helper.getLayoutPosition();
        CheckBox checkBox;
        if (isLinearLayoutManager())
            checkBox = helper.getView(R.id.cb_note_list_liear_check);
        else
            checkBox = helper.getView(R.id.cb_note_list_grid_check);
        showCheckBox(checkBox, position);
    }

    private void showCheckBox(CheckBox checkBox, int position) {
        if (NoteListConstants.isShowMultiSelectAction) {
            checkBox.setVisibility(View.VISIBLE);
            if (mCheckList.get(position))
                checkBox.setChecked(true);
            else
                checkBox.setChecked(false);
        } else {
            checkBox.setVisibility(View.INVISIBLE);
            checkBox.setChecked(false);
        }
    }


}
