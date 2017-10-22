/*
    メインスレッドで接続を行っている間はユーザーの操作を受け付けない
    なので、接続処理は別スレッドで行うのが一般的らしい
    Androidでネットワーク通信を行う場合は、UIスレッドから通信してはいけない
    UIスレッドから通信してしまうとNetworkOnMainThreadExceptinが発生する(Android 3.0 以降)
    バインド＝接続
 */
/*
    グローバルIPアドレスでインターネットに接続されているルータを特定する
    そこからポート番号でそのルータのローカルに接続している端末を特定する
*/
/*
    うちのマンションでは、グローバルIPアドレスがマンション管理側に１つまたは複数個あり
    そのグローバルIPアドレスを小分けにしてプライベートIPアドレスとして各部屋に配置しているっぽい
    その状況でもポート開放できるのか？
    そもそもポート開放していないと外部（グローバルIPアドレス）からの接続要求に答えることができないのか？
 */
package com.example.nakatsukakentaro.socketex;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private final static String BR = System.getProperty("line.separator");

    /*
        家でローカルIPアドレスを指定したらできた
        グローバルIPアドレスだとできない
     */
    private String ip;
    private int port;

    private Socket socket;      //ソケット
    private InputStream in;     //入力ストリーム
    private OutputStream out;   //出力ストリーム
    private boolean error;      //エラー

    private TextView textview;
    private EditText chatEditText;

    /*
        ハンドラとは
        AndroidはUIの操作がメインスレッドでしかできないようになっているため、
        メインスレッド以外のスレッド内でUIを操作するには、ハンドラ経由でユーザーインターフェースを操作する
        ハンドラ経由なら操作が可能？
        ハンドラが生成されたスレッドにRunnnableを投げる？
    */
    private final Handler handler = new Handler();  //ハンドラ

    //アクティビティ起動時に呼ばれる
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        ip = intent.getStringExtra("ipaddress");
        port = Integer.parseInt(intent.getStringExtra("portnumber"));

        textview = (TextView) findViewById(R.id.textview);
        chatEditText = (EditText) findViewById(R.id.chatedittext);

    }

    //アクティビティ開始時に呼ばれる
    @Override
    protected void onStart() {
        super.onStart();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    connect(ip, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    //アクティビティの停止時に呼ばれる
    @Override
    protected void onStop() {
        super.onStop();
        disconnect();
    }

    private void addText(final String text) {
        //ハンドラの生成
        handler.post(new Runnable() {
            @Override
            public void run() {
                textview.setText(text + BR + textview.getText());
            }
        });
    }

    private void connect(String ip, int port) {
        int size;
        String str;
        /*
            注：要検証
            Streamについて先生に聞く
         */

        //バッファを一時的に溜めておくためのバイト配列
        byte[] w = new byte[1024];

        try {
            addText("接続中");
            //ソケット接続  (IPアドレス, ポート番号)
            socket = new Socket(ip, port);
            //データ取得の準備
            in = socket.getInputStream();
            //データ送信の準備
            out = socket.getOutputStream();
            addText("接続完了");

            //受信ループ
            //ソケット接続している限りループし続ける
            while(socket != null && socket.isConnected()) {
                //データの受信
                size = in.read(w);

                if(size <= 0) {
                    continue;
                }
                str = new String(w, 0, size, "UTF-8");

                addText(str);
                System.out.println(str);
            }
        } catch (Exception e) {
            addText("通信失敗しました");
        }
    }

    private void disconnect() {
        try {
            socket.close();
            socket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickButton(View view) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                error = false;

                try {
                    //データの送信
                    if(socket != null && socket.isConnected()) {
                        byte[] w = chatEditText.getText().toString().getBytes("UTF-8");
                        //送信準備
                        out.write(w);
                        //送信開始
                        out.flush();
                    }
                } catch (Exception e) {
                    error = true;
                }

                //ハンドラの生成
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!error) {
                            chatEditText.setText("");
                        } else {
                            addText("通信失敗しました");
                        }
                    }
                });
            }
        });
        thread.start();
    }
}
