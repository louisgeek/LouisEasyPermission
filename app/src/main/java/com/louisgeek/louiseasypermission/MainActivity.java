package com.louisgeek.louiseasypermission;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.taobao.library.EasyPermission;
import com.taobao.library.PermissionResultCallback;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * 新的运行时权限检测机制仅作用于TargetSdkVersion大于等于23的情况
 * <p>
 * 新的权限机制将权限分为普通权限和危险权限，仅危险权限需要通过代码动态去申请。
 * <p>
 * 危险权限包括:
 * <p>
 * 身体传感器
 * 日历
 * 摄像头
 * 通讯录
 * 地理位置
 * 麦克风
 * 电话
 * 短信
 * 存储空间
 * <p>
 * CALENDAR : READ_CALENDAR WRITE_CALENDAR
 * <p>
 * CAMERA : CAMERA
 * <p>
 * CONTACTS : READ_CONTACTS WRITE_CONTACTS GET_ACCOUNTS
 * <p>
 * LOCATION : ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION
 * <p>
 * MICROPHONE : RECORD_AUDIO
 * <p>
 * PHONE : READ_PHONE_STATE CALL_PHONE READ_CALL_LOG WRITE_CALL_LOG ADD_VOICEMAIL USE_SIP PROCESS_OUTGOING_CALLS
 * <p>
 * SENSORS :  BODY_SENSORS
 * <p>
 * SMS :  SEND_SMS  RECEIVE_SMS READ_SMS RECEIVE_WAP_PUSH RECEIVE_MMS
 * <p>
 * STORAGE : READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.id_btn_old).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionOld();
            }
        });
        findViewById(R.id.id_btn_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionNew();
            }
        });
    }
        /**【新方案】*/
    private EasyPermission mEasyPermission;
    private void checkPermissionNew() {
        if(mEasyPermission == null){
            mEasyPermission = new EasyPermission.Builder(this, new PermissionResultCallback() {
                @Override
                public void onPermissionGranted(List<String> grantedPermissions) {
                    Toast.makeText(MainActivity.this, "一些已授予的权限:" + grantedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    writeFile();

                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    Toast.makeText(MainActivity.this, "一些被拒绝的权限:\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                }
            }).permission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE)
                    .rationalMessage("若想使用此功能，必须给我权限")
                    .deniedMessage("您没有授予我权限，功能将不能正常使用。你可以去设置页面重新授予权限")
                    .settingBtn(true)
                    .build();

        }

        mEasyPermission.check();
    }


    /**【新方案】调用EasyPermission#handleResult分发结果*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mEasyPermission != null){
            mEasyPermission.handleResult(requestCode,resultCode,data);
        }

    }

    /**【传统方案】*/
    private void checkPermissionOld(){
        //1. 检查我们是否有权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            //2. 是否应该向用户解释（用户之前拒绝过此权限）
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

//                Toast.makeText(this,"你需要此权限去写文件",Toast.LENGTH_SHORT).show();
                //解释完之后再去请求权限  弹dialog，如果dialog同意就重新请求权限

                //如果是fragment，请使用直接使用requestPermissions

                new AlertDialog.Builder(MainActivity.this).setMessage("OLD:你需要此权限去写文件")
                        .setPositiveButton("申请权限", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

            }else{
                //3. 请求权限
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            }
        }else{
            writeFile();
        }
    }
    /**【传统方案】*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100://request code 不超过255
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    writeFile();

                } else {
                    //弹dialog，让用户去设置页面打开权限
                  //  Toast.makeText(this,"OLD:写文件失败,没有权限",Toast.LENGTH_SHORT).show();
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setMessage("OLD:你需要此权限去写文件in onRequestPermissionsResult").setPositiveButton("申请权限", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();


                }
                return;
        }

    }
    private void writeFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            File file = new File(dir,"test.txt");
            FileWriter writer = new FileWriter(file);
            writer.write("文件内容...");
            writer.flush();
            writer.close();
            Toast.makeText(this, "写文件成功", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "写文件失败", Toast.LENGTH_SHORT).show();
        }
    }
}
