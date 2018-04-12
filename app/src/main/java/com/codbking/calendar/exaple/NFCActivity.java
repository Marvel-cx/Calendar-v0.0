package com.codbking.calendar.exaple;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.jyd.patrolsys.utils.DesUtil;
import com.jyd.patrolsys.utils.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NFCActivity extends AppCompatActivity {
    private TextView ifo_NFC;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter ndef;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private boolean isFirst = true;
    private String readResult = "";
    private String MPTDP_ID;
    private String point_name;
    private Handler handler;
    private String reqString;
    private List<Map<String,Object>> dataList =new ArrayList<Map<String,Object>>();
    private String result;
    private String rspData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        init();
        handler=new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String data = (String)msg.obj+"";
                        ifo_NFC.setText("打卡时间"+rspData);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void init() {
        ifo_NFC = (TextView) findViewById(R.id.ifo_NFC);
        Bundle bundle=this.getIntent().getExtras();
        MPTDP_ID=bundle.getString("MPTDP_ID");
        point_name=bundle.getString("point_name");
        try {
            reqString= DesUtil.encryptData(MPTDP_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //NFC适配器，所有的关于NFC的操作从该适配器进行
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(!ifNFCUse()){
            return;
        }
        //将被调用的Intent，用于重复被Intent触发后将要执行的跳转
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        //设定要过滤的标签动作，这里只接收ACTION_NDEF_DISCOVERED类型
        ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addCategory("*/*");
        mFilters = new IntentFilter[] { ndef };// 过滤器
        mTechLists = new String[][] { new String[] { NfcA.class.getName() },
                new String[] { NfcF.class.getName() },
                new String[] { NfcB.class.getName() },
                new String[] { NfcV.class.getName() } };// 允许扫描的标签类型

        if (isFirst) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent()
                    .getAction())) {
                System.out.println(getIntent().getAction());
                if (readFromTag(getIntent())) {
                    ifo_NFC.setText(readResult);
                    System.out.println("1.5...");
                } else {
                    ifo_NFC.setText("标签数据为空");
                }
            }
            isFirst = false;
        }
    }

    /**
     * 检测工作,判断设备的NFC支持情况
     * @return
     */
    private Boolean ifNFCUse() {
        // TODO Auto-generated method stub
        if (nfcAdapter == null) {
            ifo_NFC.setText("设备不支持NFC！");
            finish();
            return false;
        }
        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            ifo_NFC.setText("请在系统设置中先启用NFC功能！");
            finish();
            return false;
        }
        return true;
    }

    /**
     * 读取NFC标签数据的操作
     * @param intent
     * @return
     */
    private boolean readFromTag(Intent intent) {
        Parcelable[] rawArray = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawArray != null) {
            NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
            NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
            try {
                if (mNdefRecord != null) {
                    readResult = new String(mNdefRecord.getPayload(), "UTF-8");
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
        System.out.println("onPause...");
    }

    /*
     * 重写onResume回调函数的意义在于处理多次读取NFC标签时的情况
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters,
                mTechLists);


        System.out.println("onResume...");
    }

    /*
     * 有必要要了解onNewIntent回调函数的调用时机,请自行上网查询
     *  (non-Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        System.out.println("onNewIntent1...");
        System.out.println(intent.getAction());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            if (readFromTag(intent)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL postUrl = new URL("http://192.168.1.122:8080/WService/Task/clockPoint.do?"+"key="+reqString);

                            HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();

                            connection.setDoOutput(true);

                            connection.setDoInput(true);

                            connection.setRequestMethod("GET");

                            connection.setUseCaches(false);

                            connection.setInstanceFollowRedirects(true);

                            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                            connection.connect();

                            if(connection.getResponseCode() == 200){
                                InputStream is = connection.getInputStream();
                                result = HttpUtils.readMyInputStream(is);
                                result= DesUtil.decrypt(result);
                                JSONObject object = JSONObject.parseObject(result);
                                rspData =object.getString("resobj");
                                connection.disconnect();
                            }
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ProtocolException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(0);
                        Message msg =new Message();
                        msg.obj =result;
                        handler.sendMessage(msg);
                    }
                }).start();
            } else {
                ifo_NFC.setText("标签数据为空");
            }
        }

    }
}
