package com.example.process;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.process.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_WRITE_STORAGE = 0x111;

    private Button bundle;
    private Button file;
    private Button messenger;
    private Button contentProvider;
    private Button aidl;
    private Button socket;
    private Button guard;
    private Button pixel;

    private int count = 1000;
    private Messenger sender;
    private ICommunicate communicate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bundle = findViewById(R.id.bundle);
        bundle.setOnClickListener(this);
        file = findViewById(R.id.file);
        file.setOnClickListener(this);
        messenger = findViewById(R.id.messenger);
        messenger.setOnClickListener(this);
        aidl = findViewById(R.id.aidl);
        aidl.setOnClickListener(this);
        contentProvider = findViewById(R.id.contentProvider);
        contentProvider.setOnClickListener(this);
        socket = findViewById(R.id.socket);
        socket.setOnClickListener(this);
        guard = findViewById(R.id.guard);
        guard.setOnClickListener(this);
        pixel = findViewById(R.id.pixel);
        pixel.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "权限申请成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == bundle) {
            Intent intent = new Intent("com.example.bundle.receiver");
            intent.putExtra("message", "Bundle Hello world");
            startActivity(intent);
        } else if (v == file) {
            File file = new File(Environment.getExternalStorageDirectory(), "readMe.txt");
            FileChannel fileChannel = null;
            FileLock fileLock = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                RandomAccessFile randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "rw");
                fileChannel = randomAccessFile.getChannel();
                fileLock = fileChannel.lock();
                ByteBuffer byteBuffer = ByteBuffer.wrap(("File Hello World" + (count += 111)).getBytes());
                fileChannel.write(byteBuffer);
                Toast.makeText(this, "写入" + new String(byteBuffer.array()), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(fileChannel);
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (v == messenger) {
            if (sender == null || sender.getBinder() == null) {
                Intent intent = new Intent();
                intent.setAction("com.example.messenger.receiver");
                intent.setPackage("com.example.receiver");
                bindService(intent, new ServiceCallback(), BIND_AUTO_CREATE);
            } else {
                sendMessage();
            }
        } else if (v == aidl) {
            if (communicate == null) {
                Intent intent = new Intent(this, AIDLService.class);
//                intent.setAction("com.example.aidl.receiver");
//                intent.setPackage("com.example.receiver");
                bindService(intent, new AIDLCallback(), BIND_AUTO_CREATE);
            } else {
                sendAIDLMessage();
            }
//            Intent intent = new Intent(this, AIDLService.class);
//            startService(intent);

        } else if (v == socket) {
            socket.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket sock = null;
                    try {
                        sock = new Socket("127.0.0.1", 8888);
                        PrintWriter printWriter = new PrintWriter(sock.getOutputStream());
                        printWriter.write("Socket Hello World" + (count += 111));
                        printWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (sock != null) {
                            try {
                                sock.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    socket.post(new Runnable() {
                        @Override
                        public void run() {
                            socket.setEnabled(true);
                        }
                    });
                }
            }).start();
        } else if (v == guard) {
            Intent intent = new Intent(this, ServiceActivity.class);
            startActivity(intent);
        } else if (v == pixel) {

        }
    }

    private void sendAIDLMessage() {
        try {
            communicate.send( "Hello world from AIDL" + (count += 111));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        Message message = Message.obtain();
        message.what = 1;
        String str = "Hello world from Messenger" + (count += 111);
        Bundle bundle = new Bundle();
        bundle.putString("message", str);
        message.setData(bundle);
        try {
            sender.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class ServiceCallback implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sender = new Messenger(service);
            sendMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sender = null;
        }
    }

    private class AIDLCallback implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            communicate = ICommunicate.Stub.asInterface(service);
            sendAIDLMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
