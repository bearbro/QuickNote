package cn.edu.zjut.quicknote.module;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.zjut.quicknote.R;
import cn.edu.zjut.quicknote.bean.ImageEntity;
import cn.edu.zjut.quicknote.constants.EditNoteConstants;
import cn.edu.zjut.quicknote.utils.PermissionUtils;
import cn.edu.zjut.quicknote.utils.ProgressDialogUtils;
import cn.edu.zjut.quicknote.utils.ThemeUtils;
import cn.edu.zjut.quicknote.widget.MyEditText;

public class NoteEditActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_TO_CAMERA = 1;  //前往相机
    public static final int REQUEST_CODE_TO_PHOTO = 2;   //前往图库
    public static final int REQUEST_CODE_TO_IMAGE_INFO = 3;

    private final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String PERMISSION_RECORD = Manifest.permission.RECORD_AUDIO;
    private final int REQUEST_PERMISSION_TO_CAMERA = 1;    // 请求权限前往相机
    private final int REQUEST_PERMISSION_TO_PHOTO = 2;     // 请求权限前往图库

    private boolean voiceChanged = false;//是否改变录音
    private boolean taking = false;
    private MediaPlayer mediaPlayer;
    private VoiceService voiceService;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.et_edit_note_content)
    MyEditText mEdContent;


    @BindView(R.id.ll_edit_note_to_microphone)
    LinearLayout mLlToMicrophone;

    @BindView(R.id.voice_talk)
    ImageView voiceTalk;

    @BindView(R.id.ll_edit_note_player)
    LinearLayout mLlPlayer;

    @BindView(R.id.voice_player)
    ImageView voicePlayer;

    @BindView(R.id.ll_edit_note_delete_voice)
    LinearLayout mLlDeleteVoice;

    @BindView(R.id.ll_edit_note_to_camera)
    LinearLayout mLlToCamera;

    @BindView(R.id.ll_edit_note_to_photo)
    LinearLayout mLlToPhoto;

    private boolean mIsNew; //　是否是新增便签
    private String mNoteId;
    private long mModifiedTime;
    private String mNoteContent;
    private int mPosition = 0;

    private String mImageName = "";
    private File mImageFolder;
    private File mImageFile;
    private String path;
    private ImageEntity mSelectedImageEntity;

    private ProgressDialogUtils mProgressDialogUtils = new ProgressDialogUtils(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        ButterKnife.bind(this);
        initViews();
        updateViews();
    }

    public void initData() {
        Intent intent = getIntent();
        mIsNew = intent.getBooleanExtra("is_add", false);
        if (mIsNew) {
            mNoteId = UUID.randomUUID().toString();
            mModifiedTime = TimeUtils.getNowMills();
            mNoteContent = "";
        } else {
            mPosition = intent.getIntExtra("position", 0);
            mNoteId = intent.getStringExtra("note_id");
            mModifiedTime = intent.getLongExtra("modified_time", 0);
            mNoteContent = intent.getStringExtra("note_content");
            path = intent.getStringExtra("path");
            String vpath = getExternalFilesDir(mNoteId).getPath() + "/" + mNoteId + ".mp4";
            File f = new File(vpath);
            if (f.exists()) {
                mLlToMicrophone.setVisibility(View.GONE);
                mLlDeleteVoice.setVisibility(View.VISIBLE);
                mLlPlayer.setVisibility(View.VISIBLE);
            }
        }
        setTitle(TimeUtils.millis2String(mModifiedTime));
    }

    public void setTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    protected void initViews() {
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mEdContent.setMinHeight(getNoteEditNeedHeight());
        initData();
    }

    protected void updateViews() {
        parseNoteContent();
        mEdContent.setSelection(mEdContent.getText().length());
        mEdContent.addTextChangedListener(mTextWatcher);
    }

    public void parseNoteContent() {
        if (mIsNew)
            return;
        showNoteContent(mNoteContent);
        String flag = EditNoteConstants.imageTabBefore + "([^<]*)" + EditNoteConstants.imageTabAfter;

        // 利用正则找出文档中的图片
        Pattern p = Pattern.compile(flag);
        Matcher m = p.matcher(mNoteContent);
        List<String> array = new ArrayList<>();
        while (m.find()) {
            String temp = m.group(1);
            array.add(temp);
        }
        for (int i = 0; i < array.size(); i++) {
            final String imageName = array.get(i);
            replaceImage(imageName);
        }
    }

    private void replaceImage(final String imageName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        File fileDir = getExternalFilesDir(mNoteId);
        assert fileDir != null;
        File imageFile = new File(fileDir.getPath() + "/" + imageName);

        BitmapFactory.decodeFile(imageFile.getPath(), options);

        int imageRequestWidth = getRequestImeWidth();
        int imageRequestHeight = setNeedHeight(options);

        System.out.println(imageFile.getPath());
        Glide.with(this)
                .load(imageFile)
                .asBitmap()
                .override(imageRequestWidth, imageRequestHeight)
                .fitCenter()
                .priority(Priority.HIGH)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        replaceImage(imageName, resource);
                    }
                });
    }

    private int getRequestImeWidth() {
        return (int) (ScreenUtils.getScreenWidth() - EditNoteConstants.imageMargin);
    }

    private int setNeedHeight(BitmapFactory.Options options) {
        int imageRequestHeight = getRequestImeHeight(options);
        if (imageRequestHeight <= 0) {
            return getNoteEditNeedHeight();
        } else
            return imageRequestHeight;
    }

    public int getRequestImeHeight(BitmapFactory.Options option) {
        float width = option.outWidth;
        float height = option.outHeight;
        //计算宽、高缩放率
        float scanleWidth = (getRequestImeWidth()) / width;
        return (int) (height * scanleWidth);
    }

    public int getNoteEditNeedHeight() {
        // 屏幕高度减去 状态栏高度、toolbar高度、底部工具栏高度
        float height = ScreenUtils.getScreenHeight() - ThemeUtils.getStatusBarHeight()
                - SizeUtils.dp2px(56) - SizeUtils.dp2px(48);
        return (int) height;
    }

    public void replaceImage(String imageName, Bitmap bitmap) {
        mEdContent.replaceDrawable(bitmap, imageName);
    }

    public void showNoteContent(String content) {
        mEdContent.setText(content);
    }

    @OnClick(R.id.ll_edit_note_to_microphone)
    public void checkToMicrophone() {
        PermissionUtils.checkPermission(this, PERMISSION_RECORD, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                starMicrophone();
                Log.d("starMicrophone","onHasPermission");
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {// 用户之前已拒绝过权限申请
                PermissionUtils.requestPermission(NoteEditActivity.this, permission[0], 4);
                starMicrophone();
                Log.d("starMicrophone","onUserHasAlreadyTurnedDown");
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {  // 用户之前已拒绝并勾选了不在询问、用户第一次申请权限。
                PermissionUtils.requestPermission(NoteEditActivity.this, permission[0], 4);
               // starMicrophone();
                Log.d("starMicrophone","onUserHasAlreadyTurnedDown");
            }
        });


    }

    private void starMicrophone(){
        voiceChanged = true;
        String vpath = getExternalFilesDir(mNoteId).getPath() + "/" + mNoteId + ".mp4";
        if (taking) {//结束
            voiceTalk.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_microphone_black));
            taking = !taking;
            voiceService.stopRecording();
            mLlToMicrophone.setVisibility(View.GONE);
            mLlDeleteVoice.setVisibility(View.VISIBLE);
            mLlPlayer.setVisibility(View.VISIBLE);

        } else {//开始
            voiceTalk.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_microphone_red));
            taking = !taking;
            voiceService = new VoiceService();
            voiceService.setmFilePath(vpath);
            voiceService.startRecording();
        }
    }
    @OnClick(R.id.ll_edit_note_delete_voice)
    public void checkDeleteVoice() {
        if (mIsNew) {
            voiceChanged = false;
        } else {
            voiceChanged = true;
        }
        String vpath = getExternalFilesDir(mNoteId).getPath() + "/" + mNoteId + ".mp4";
        File file = new File(vpath);
        if (file.exists()) {
            Log.d("delete", "startDelete");
            file.delete();
        } else {
            Log.d("delete", "文件不存在：" + vpath);
        }
        mLlDeleteVoice.setVisibility(View.GONE);
        mLlPlayer.setVisibility(View.GONE);
        mLlToMicrophone.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.ll_edit_note_player)
    public void checPlayVoice() {
        // path = "/storage/emulated/0/123.mp4";

        String vpath = getExternalFilesDir(mNoteId).getPath() + "/" + mNoteId + ".mp4";
        File file = new File(vpath);
        ///storage/emulated/0/123.mp4
        if (file.exists()) {
            Log.d("player", "start");
            voicePlayer.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_playing));
            startPlay(file);

        } else {
            Log.d("player", "文件不存在：" + vpath);
        }
    }


    private void startPlay(File mFile) {
        try {
            //初始化播放器
            mediaPlayer = new MediaPlayer();
            //设置播放音频数据文件
            mediaPlayer.setDataSource(mFile.getAbsolutePath());
            //设置播放监听事件
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //播放完成
                    // playEndOrFail(true);
                    Log.d("play", "end");
                    voicePlayer.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_player));

                }
            });
            //播放发生错误监听事件
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    //  playEndOrFail(false);
                    Log.d("error", "播放失败");
                    voicePlayer.setImageDrawable(getResources().getDrawable(R.drawable.ic_phone_player));
                    return true;
                }
            });
            //播放器音量配置
            mediaPlayer.setVolume(1, 1);
            //是否循环播放
            mediaPlayer.setLooping(false);
            //准备及播放
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            //播放失败正理
            // playEndOrFail(false);

        }

    }

    @OnClick(R.id.ll_edit_note_to_camera)
    public void checkPermissionAndToCamera() {
        PermissionUtils.checkPermission(this, PERMISSION_STORAGE, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                toCamera();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                PermissionUtils.requestPermission(NoteEditActivity.this, permission[0], REQUEST_PERMISSION_TO_CAMERA);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                PermissionUtils.requestPermission(NoteEditActivity.this, permission[0], REQUEST_PERMISSION_TO_CAMERA);
            }
        });
    }

    @OnClick(R.id.ll_edit_note_to_photo)
    public void checkPermissionAndToPhoto() {
        PermissionUtils.checkPermission(this, PERMISSION_STORAGE, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                toPhoto();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                PermissionUtils.requestPermission(NoteEditActivity.this, permission[0], REQUEST_PERMISSION_TO_PHOTO);
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                PermissionUtils.requestPermission(NoteEditActivity.this, permission[0], REQUEST_PERMISSION_TO_PHOTO);
            }
        });
    }

    @OnClick(R.id.et_edit_note_content)
    public void clickNoteEditText() {
        // 获取光标位置
        int selectionAfter = mEdContent.getSelectionStart();

        for (int i = 0; i < mEdContent.mImageList.size(); i++) {

            ImageEntity imageEntity = mEdContent.mImageList.get(i);

            if (selectionAfter >= imageEntity.getStart()
                    && selectionAfter <= imageEntity.getEnd()) { // 光标位置在照片的位置内
                // 隐藏键盘
                InputMethodManager imm = (InputMethodManager) Utils.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(mEdContent.getWindowToken(), 0);
                // 光标移到图片末尾的换行符后面
                mEdContent.setSelection(imageEntity.getEnd() + 1);
                mSelectedImageEntity = imageEntity;
//                toImageInfoActivity();
                break;
            }
        }
    }

    public void toCamera() {
        try {
            setFile();
            if (!mImageFolder.exists()) mImageFolder.createNewFile();
            mImageFile.createNewFile();
            Uri imageUri;
            if (Build.VERSION.SDK_INT >= 24) {
                imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", mImageFile);
            } else {
                imageUri = Uri.fromFile(mImageFile);
            }

            Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
            getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(getImageByCamera, REQUEST_CODE_TO_CAMERA);
        } catch (Exception e) {
            ToastUtils.showShort("打开相机失败");
        }
    }

    public void toPhoto() {
        setFile();
        Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
        getImage.addCategory(Intent.CATEGORY_OPENABLE);
        getImage.setType("image/*");
        startActivityForResult(getImage, REQUEST_CODE_TO_PHOTO);
    }

    private void setFile() {
        mImageName = TimeUtils.getNowString() + ".jpg";
        mImageFolder = getExternalFilesDir(mNoteId);
        mImageFile = new File(mImageFolder, mImageName);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_TO_CAMERA:
                onRequestCameraResult(grantResults);
                break;
            case REQUEST_PERMISSION_TO_PHOTO:
                onRequestPhotoResult(grantResults);
                break;
        }
    }

    private void onRequestCameraResult(int[] grantResults) {
        PermissionUtils.onRequestPermissionResult(this, PERMISSION_STORAGE, grantResults, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                toCamera();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                ToastUtils.showShort("请允许读取储存权限");
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                showToAppSettingDialog();
            }
        });
    }

    private void onRequestPhotoResult(int[] grantResults) {
        PermissionUtils.onRequestPermissionResult(this, PERMISSION_STORAGE, grantResults, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                toPhoto();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                ToastUtils.showShort("请允许读取储存权限");
            }

            @Override
            public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                showToAppSettingDialog();
            }
        });
    }

    public void showToAppSettingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("权限设置")
                .setMessage("您已禁止应用的储存权限，请前往应用设置中开启")
                .setPositiveButton("前往",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PermissionUtils.toAppSetting(NoteEditActivity.this);
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    public void calculateContentAndImageCount(MyEditText myEditText) {
        int count = myEditText.getText().length();
        int imageCount = myEditText.mImageList.size();
        for (int i = 0; i < myEditText.mImageList.size(); i++) {
            count = count - (myEditText.mImageList.get(i).getImageFlag().length());
            count = count - 1;
        }
        showStatisticsDialog(imageCount, count);
    }

    public void showStatisticsDialog(int imageCount, int textCount) {
        new AlertDialog.Builder(this)
                .setMessage("文字数量：" + textCount + "\n" + "图片数量：" + imageCount)
                .setPositiveButton("确定", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_TO_PHOTO:
                if (resultCode == RESULT_OK) {
                    toPhotoResult(data);
                }
                break;
            case REQUEST_CODE_TO_CAMERA:
                if (resultCode == RESULT_OK)
                    displayImage();
                break;
            case REQUEST_CODE_TO_IMAGE_INFO:
                if (resultCode == RESULT_OK) {
                    mEdContent.getEditableText()
                            .replace(mSelectedImageEntity.getStart(),
                                    mSelectedImageEntity.getEnd() + 1,
                                    "");
                }
                break;
        }
    }

    private void toPhotoResult(Intent intent) {
        if (Build.VERSION.SDK_INT >= 19) {
            handleImageOnKitKat(intent);
        } else {
            handleImageBeforeKitKat(intent);
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
//            如果是document类型的URI，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            assert uri != null;
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePatch(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePatch(contentUri, null);
            }
        } else {
            assert uri != null;
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                //            如果是content类型的uri的话，则使用普通方式处理
                imagePath = getImagePatch(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                //            若果是file类型的uri，则直接获取图片路径
                imagePath = uri.getPath();
            }
        }
        new CopyFileAsyncTask(imagePath).execute();
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePatch(uri, null);
        new CopyFileAsyncTask(imagePath).execute();
    }

    private String getImagePatch(Uri uri, String selection) {
        String path = null;
//        通过URi和selection 来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage() {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;  // 对图片进行设置 但不形成示例，不耗费内存

        BitmapFactory.decodeFile(mImageFile.getPath(), options);

        int imageRequestWidth = getRequestImeWidth();
        int imageRequestHeight = getRequestImeHeight(options);

        Glide.with(this)
                .load(mImageFile)
                .asBitmap()
                .override(imageRequestWidth, imageRequestHeight)  // 设置大小
                .fitCenter()                                     // 不按照比例
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        //根据Bitmap对象创建ImageSpan对象
                        mEdContent.insertDrawable(resource, mImageName);
                    }
                });
    }

//    private void toImageInfoActivity() {
//        Intent intent = new Intent(this, ImageInfoActivity.class);
//        intent.putExtra("image_name", mSelectedImageEntity.getImageName());
//        intent.putExtra("note_id", mNoteId);
//        startActivityForResult(intent, REQUEST_CODE_TO_IMAGE_INFO);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_note_statistics:
                calculateContentAndImageCount(mEdContent);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        saveNote(mEdContent.getText().toString());
        super.onBackPressed();
    }

    public void saveNote(String content) {
        Intent intent;
        String npath = getExternalFilesDir(mNoteId).getPath();

        if (voiceService != null) {
            voiceService = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            voicePlayer = null;
        }
        // 内容改变时才保存
        if (!mNoteContent.equals(content)
                || !"".equals(content) && !npath.equals(path) || voiceChanged) {
            intent = getIntent();
            intent.putExtra("note_id", mNoteId);
            intent.putExtra("note_content", content);
            intent.putExtra("modified_time", mModifiedTime);
            intent.putExtra("position", mPosition);
            intent.putExtra("path", npath);
            setResult(RESULT_OK, intent);
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            for (int i = 0; i < mEdContent.mImageList.size(); i++) {
                ImageEntity imageEntity = mEdContent.mImageList.get(i);
                if (start == imageEntity.getEnd()) {
                    mEdContent.getEditableText().replace(imageEntity.getStart(), imageEntity.getEnd(), "");
                    mEdContent.mImageList.remove(i);
                    mEdContent.mDeleteImageList.add(imageEntity);
                    break;
                }
            }
            mEdContent.setTextCountChange(start, before, count);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @SuppressLint("StaticFieldLeak")
    class CopyFileAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private String mImagePaths;

        CopyFileAsyncTask(String imagePaths) {
            mImagePaths = imagePaths;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialogUtils.show("加载中...");
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return FileUtils.copyFile(new File(mImagePaths), mImageFile);
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            mProgressDialogUtils.hide();
            if (isSuccessful) {
                displayImage();
            } else {
                ToastUtils.showShort("图片读取失败");
            }
        }
    }
}
