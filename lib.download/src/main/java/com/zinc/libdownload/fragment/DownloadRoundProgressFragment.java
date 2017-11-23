package com.zinc.libdownload.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.zinc.jdownload.R;
import com.zinc.libdownload.widget.RoundChart;
import com.zinc.libdownload.widget.RunningTextview;

/**
 * @author Jiang zinc
 * @date 创建时间：2017/11/15
 * @description 下载环形进度
 */

public class DownloadRoundProgressFragment extends AppCompatDialogFragment {

    private final String TAG = Integer.toHexString(System.identityHashCode(this));

    private RunningTextview running_textview;
    private RoundChart round_chart;

    public static DownloadRoundProgressFragment newInstance() {
        DownloadRoundProgressFragment fragment = new DownloadRoundProgressFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TranslucentNoTitle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download_round_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        running_textview = view.findViewById(R.id.running_textview);
        round_chart = view.findViewById(R.id.round_chart);

    }

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }

    public void setProgress(int downloaded, int total) {
        round_chart.setTotal(total).display(downloaded);
        running_textview.display(downloaded * 100 / total);
    }

    public void setProgress(long downloaded, long total) {

        float percent = downloaded / total;

        int perInt = (int) (percent * 100);

        this.setProgress(perInt, 100);
    }
}
