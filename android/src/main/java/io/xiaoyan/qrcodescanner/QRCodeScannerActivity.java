package io.xiaoyan.qrcodescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class QRCodeScannerActivity extends Activity implements QRCodeView.Delegate {
    private static final String HEADER = "X-HM://";

    public static String EXTRA_RESULT = "extra_result";

    public static String SCAN_TYPE = "scan_type";

    public static int SCAN_TO_SHARE = 1;
    public static int SCAN_TO_REQUEST = 2;

    private ZXingView requestView;
    private ZXingView shareView;

    private ImageView backButton;

    private int scanType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);
        backButton = findViewById(R.id.back_button);
        requestView = findViewById(R.id.qrcode_scan_request);
        shareView = findViewById(R.id.qrcode_scan_share);
        final Intent intent = getIntent();
        scanType = intent.getIntExtra(SCAN_TYPE, 1);

        if (scanType == SCAN_TO_REQUEST) {
            requestView.setDelegate(this);
            requestView.setVisibility(View.VISIBLE);
            shareView.setVisibility(View.GONE);
        } else if (scanType == SCAN_TO_SHARE) {
            shareView.setDelegate(this);
            shareView.setVisibility(View.VISIBLE);
            requestView.setVisibility(View.GONE);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanType == SCAN_TO_REQUEST) {
            requestView.startCamera();
            requestView.showScanRect();
            requestView.startSpot();
        } else  if (scanType == SCAN_TO_SHARE) {
            shareView.startCamera();
            shareView.showScanRect();
            shareView.startSpot();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanType == SCAN_TO_REQUEST) {
            requestView.stopSpot();
            requestView.stopCamera();
        } else if (scanType == SCAN_TO_SHARE) {
            requestView.stopSpot();
            requestView.stopCamera();
        }
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        if (scanType == SCAN_TO_SHARE) {
            shareView.stopSpot();
            shareView.startSpot();
        } else {
            requestView.stopSpot();
            requestView.startSpot();
        }
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT, result);
        setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e("ERROR", "open camera error");
    }
}
