package cn.edu.zjut.quicknote.constants;

import android.graphics.Bitmap;

import com.blankj.utilcode.util.SizeUtils;



public class EditNoteConstants {

    // 图片标记的前便签和后标签
    public static String imageTabBefore="<image>";
    public static String imageTabAfter="</image>";

    public static String voiceTabBefore="<voice>";
    public static String voiceTabAfter="</voice>";

    // 图片距离左右的总距离
    public static float imageMargin= SizeUtils.dp2px(32);

    // 分享时的水印文字
    public static String watermarkText="来自：发呆便签";

    public static Bitmap shareBitmap;
}
