package com.ghost.thunder.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.ghost.thunder.app.MainActivity;
import com.ghost.thunder.demo.R;
import com.ghost.thunder.download.DownLoadProgressListener;
import com.ghost.thunder.download.DownLoadUtil;
import com.ghost.thunder.utils.LogPrinter;
import com.ghost.thunder.utils.StorageUtils;
import com.ghost.thunder.utils.UrlType;
import com.ghost.thunder.utils.UrlUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by wyt on 2018/8/9.
 */

public class HomeFragment extends Fragment implements DownLoadProgressListener {

    private static final String TAG = "HomeFragment";

    private Unbinder unbinder;

    @BindView(R.id.et_url)
    EditText etUrl;

    @BindView(R.id.btn_download)
    Button btnDownload;

    @BindView(R.id.btn_select_file)
    Button btnSelectFile;

    DownLoadUtil downLoadUtil;

    MainActivity mainActivity;

    @BindView(R.id.video_view)
    VideoView mVideoView;

    boolean canPlay = false;

    boolean isTorrent = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_layout, null);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick(R.id.btn_download)
    public void onDonwloadButtonClicked() {
        String url = etUrl.getText().toString();
        LogPrinter.i(TAG, "onDonwloadButtonClicked : " + url);
        if(TextUtils.isEmpty(url)) {
            Toast.makeText(getContext(), R.string.url_is_null, Toast.LENGTH_SHORT).show();
            return;
        }

        if(!UrlUtils.isValidUrl(url)) {
            Toast.makeText(getContext(), R.string.url_format_error, Toast.LENGTH_SHORT).show();
            return;
        }

        startDownloadTask(url);
    }

    @OnClick(R.id.btn_select_file)
    public void startSelectTorrentFile() {
        if(canPlay) {
            LogPrinter.i(TAG, "can play!");
        } else {
            LogPrinter.i(TAG, "can not play!");
        }
    }

    private void checkDonwloadUtil() {
        if(downLoadUtil == null)
            downLoadUtil = DownLoadUtil.getInstance(getContext().getApplicationContext());
    }

    private void startDownloadTask(String url) {
        checkDonwloadUtil();
        try {
            downLoadUtil.getTaskInfo(url);

            downLoadUtil.startDownLoad(url, this);
            LogPrinter.i(TAG, "url is valid");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.task_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(downLoadUtil != null) {
            downLoadUtil.stopTask();
        }
    }

    @Override
    public void onProgressChange(String totalSize, String downloadedSize, String downSpeed) {
        LogPrinter.i(TAG, "onProgressChange : " + totalSize + "  " +
                downloadedSize + "  " + downSpeed + "  " + Thread.currentThread().getName());
    }

    @Override
    public void onProgressChangeRealSize(long totalSize, long downloadedSize, long downSpeed) {
        if(StorageUtils.isCanPlay(totalSize, downloadedSize))
            canPlay = true;
        else
            canPlay = false;
    }

    @Override
    public void onDonwloadEnd(String filePath) {
        LogPrinter.i(TAG, "onDonwloadEnd -- > end file : " +
                filePath + "   currentThread : " + Thread.currentThread().getName());
        if(StorageUtils.isTorrentFile(filePath)) {
            LogPrinter.i(TAG,"onDonwloadEnd is a torrent file, start new torrent task!");
            startDownloadTask(filePath);
        } else {
            LogPrinter.i(TAG,"onDonwloadEnd success");
        }
    }

    @Override
    public void onTaskStart(String fileName) {
        LogPrinter.i(TAG, "OnTaskStart : " + fileName);
        if(UrlType.isTorrentUrl(fileName)) {
            isTorrent = true;
        } else {
            isTorrent = false;
        }

    }

}
