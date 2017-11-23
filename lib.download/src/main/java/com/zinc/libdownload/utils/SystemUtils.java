package com.zinc.libdownload.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * @author Jiang zinc
 * @date 创建时间：2017/11/22
 * @description
 */

public class SystemUtils {

    /**
     * @date 创建时间 2017/11/15
     * @author Jiang zinc
     * @Description 自安装
     * @version
     */
    public static void installAuto(Context context, File file) {
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
