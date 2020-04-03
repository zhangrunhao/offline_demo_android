package com.example.offlinedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.expamle.myfirestapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FileDownloader.setup(this);
        FileDownloader
                .getImpl()
                .create("https://zhangrunhao.oss-cn-beijing.aliyuncs.com/offline_zip/test.zip")
                .setPath(FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "test_save" + File.separator + "123.zip")
                .setForceReDownload(true)
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.i("tag", "pending: ");
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.i("TAG", "progress: ");
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.i("TAG", "paused: ");
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.i("TAG", "completed: " + task.getPath());
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.i("TAG", "error: ");
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.i("TAG", "warn: ");
                    }
                }).start();
    }

    public void  sendMessage(View view) {
        // 首先要使用Context参数, 因为Activity类是Context类的子类.
        // Class参数是app组件, 也就是传入intent的地方, 换句话, 这是这个activity开始的地方
        Intent intent = new Intent(this, DisplayMessageActivity.class);

        EditText editText = findViewById(R.id.editText);
        String message = editText.getText().toString();
        // intent把值添加进去
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void openMyWebView(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }

    private static void unZip(String zipFileString, String outPathString) throws Exception {

    }


}
