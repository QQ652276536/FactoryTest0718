package com.zistone.factorytest0718;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.factorytest0718.util.MyConvertUtil;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;

import java.util.Arrays;

public class NfcActivity extends BaseActivity {

    private static final String TAG = "NfcActivity";

    private NfcAdapter _nfcAdapter;
    private PendingIntent _pendingIntent;
    private TextView _txt1;

    private void GetIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        if (ndef != null)
            Log.i(TAG, "Ndef的类型：" + ndef.getType());
        String id = MyConvertUtil.ByteArrayToHexStr(tag.getId());
        _txt1.setText("序列号\r\n" + id);
        String[] strArray = tag.getTechList();
        Log.e(TAG, Arrays.toString(strArray));
        String result = ReadTagClassic(tag);
        _txt1.append("\r\n" + result);
    }

    private String ReadTagClassic(Tag tag) {
        MifareClassic mifareClassic = MifareClassic.get(tag);
        //读取TAG
        try {
            //            mifareClassic.connect();
            String metaInfo = "";
            //获取TAG的类型
            int type = mifareClassic.getType();
            //获取TAG中包含的扇区数
            int sectorCount = mifareClassic.getSectorCount();
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mifareClassic.getBlockCount() + "个块\n存储空间: " + mifareClassic.getSize() + "B\n";
            //            for (int j = 0; j < sectorCount; j++) {
            //                // Authenticate a sector with key A.
            //
            //                auth = mfc.authenticateSectorWithKeyA(j,
            //                        MifareClassic.KEY_DEFAULT);
            //                int bCount;
            //                int bIndex;
            //                if (auth) {
            //                    metaInfo += "Sector " + j + ":验证成功\n";
            //                    // 读取扇区中的块
            //                    bCount = mfc.getBlockCountInSector(j);
            //                    bIndex = mfc.sectorToBlock(j);
            //                    for (int i = 0; i < bCount; i++) {
            //                        byte[] data = mfc.readBlock(bIndex);
            //                        metaInfo += "Block " + bIndex + " : "
            //                                + ByteArrayToHexString(data) + "\n";
            //                        bIndex++;
            //                    }
            //                } else {
            //                    metaInfo += "Sector " + j + ":验证失败\n";
            //                }
            //            }
            return metaInfo;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mifareClassic.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //当前app正在前端界面运行，这个时候有intent发送过来，那么系统就会调用onNewIntent将intent传过来，我们只需要在这里检验这
        //个intent是否是NFC相关的intent，如果是就调用处理方法
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
        }
        GetIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != _nfcAdapter) {
            _nfcAdapter.enableForegroundDispatch(this, _pendingIntent, null, null);
        } else {
            MyProgressDialogUtil.ShowWarning(this, "警告", "该设备不支持NFC，无法使用此功能！",false, () -> {
                Fail();
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_nfc);
        SetBaseContentView(R.layout.activity_nfc);
        //初始化NfcAdapter
        _nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //初始化PendingIntent，当有NFC设备连接上的时候，就交给当前Activity处理
        _pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
        _txt1 = findViewById(R.id.txt1_nfc);
        _txt1.setGravity(Gravity.CENTER);
    }

}
