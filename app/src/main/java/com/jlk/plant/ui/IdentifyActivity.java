package com.jlk.plant.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jlk.plant.R;
import com.jlk.plant.app.AppSetting;
import com.jlk.plant.base.BaseFragmentActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class IdentifyActivity extends BaseFragmentActivity {

    private final String tag = "IdentifyActivity";
    private TextView text_result;
    private ImageView image;
    private Button btn_upload;
    private File file;
    public static final int NONE = 0;
    public static final int PHOTOZOOM = 2; // 缩放
    public static final int PHOTORESOULT = 3;// 结果
    public static final String IMAGE_UNSPECIFIED = "image/*";

    @Override
    public void setActivityContext() {
        mContext = this;
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_identity);
    }

    @Override
    public void initViews() {
        findViewById(R.id.back).setVisibility(View.VISIBLE);

        text_result = (TextView) findViewById(R.id.text_result);
        image = (ImageView) findViewById(R.id.image);
        btn_upload = (Button) findViewById(R.id.btn_upload);


    }

    @Override
    public void initListeners() {
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PHOTOZOOM);
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file == null) {
                    return;
                }


                OkHttpClient client = new OkHttpClient();

                RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);

                RequestBody requestBody = new MultipartBody.Builder()
                        .addFormDataPart("image", "img_identified.png", fileBody)
                        .build();

//                Request request = new Request.Builder()
//                        .url("http://image.baidu.com/pictureup/uploadshitu?objurl=http://map1.zw3e.com:8080/zw_news_map/150/2014073/1405906118712531720.jpg")
//                        .get()
//                        .build();
                Request request = new Request.Builder()
                        .url("http://image.baidu.com/pictureup/uploadshitu")
                        .post(requestBody)
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                text_result.setText(Html.fromHtml(result));
                                SharedPreferences sp = getSharedPreferences(AppSetting.spfile, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("html", result);
                                editor.commit();

                            }
                        });
                    }
                    //...
                });
            }
        });
    }


    @Override
    public void initData() {

    }

    @Override
    public String getTitleName() {
        return "以图识花";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // ContentResolver resolver = getContentResolver();

        if (resultCode == NONE || data == null)
            return;

        // 读取相册缩放图片
        if (requestCode == PHOTOZOOM) {
            try {
                // 获得图片的uri
                Uri originalUri = data.getData();

                startPhotoZoom(originalUri);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 处理结果
        if (requestCode == PHOTORESOULT) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");

                File dir = new File(AppSetting.sdCard_dir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file = new File(dir, "img_identified.png");
                try {
                    // 保存截图
                    FileOutputStream output = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, output);// (0--100)压缩文件
                    image.setImageBitmap(photo);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 开启一个系统页面来裁剪传进来的照片
     *
     * @param uri 需要裁剪的照片的URI值
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTORESOULT);
    }
}