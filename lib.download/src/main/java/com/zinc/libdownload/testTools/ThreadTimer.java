package com.zinc.libdownload.testTools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ThreadTimer implements Runnable {

    private Handler handler;

    public static int progess = 0;

    public ThreadTimer(Handler handler) {

        this.handler = handler;

    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                Message msg = new Message();
                msg.what = 0;
                Bundle bundle = new Bundle();


                if(progess<100) {
                    progess += 10;
                    bundle.putInt("downloaded", progess);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }else {
                    break;
                }

//                progess = 100;
//                bundle.putInt("downloaded", progess);
//                msg.setData(bundle);
//                handler.sendMessage(msg);
//                break;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
