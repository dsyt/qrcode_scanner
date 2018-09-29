package io.xiaoyan.qrcodescanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Map;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** QrcodeScannerPlugin */
public class QrcodeScannerPlugin implements MethodCallHandler, ActivityResultListener,  PluginRegistry.RequestPermissionsResultListener {
  private static final int REQUEST_CODE_SCAN_ACTIVITY = 2777;
  private static final int REQUEST_CODE_CAMERA_PERMISSION = 3777;

  private FlutterActivity activity;
  private Result pendingResult;
  private Map<String, Object> arguments;
  private boolean executeAfterPermissionGranted;

  public QrcodeScannerPlugin(FlutterActivity activity) {
    this.activity = activity;
  }

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "qrcode_scanner");
    QrcodeScannerPlugin instance = new QrcodeScannerPlugin((FlutterActivity) registrar.activity());
    registrar.addActivityResultListener(instance);
    registrar.addRequestPermissionsResultListener(instance);
    channel.setMethodCallHandler(instance);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("scanQRCode")) {
      if (!(call.arguments instanceof Map)) {
        throw new IllegalArgumentException("Plugin not passing a map as parameter: " + call.arguments);
      }
      arguments = (Map<String, Object>) call.arguments;
      boolean handlePermission = (boolean) arguments.get("handlePermissions");
      this.executeAfterPermissionGranted = (boolean) arguments.get("executeAfterPermissionGranted");

      if (checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        if (shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
          if (handlePermission) {
            requestPermissions();
          } else {
            setNoPermissionsError();
          }
        } else {
          if (handlePermission) {
            requestPermissions();
          } else {
            setNoPermissionsError();
          }
        }
      } else {
        startView();
      }
    } else {
      throw new IllegalArgumentException("Unknown method " + call.method);
    }
  }

  @TargetApi(Build.VERSION_CODES.BASE_1_1)
  private int checkSelfPermission(Context context, String permission) {
    if (permission == null) {
      throw new IllegalArgumentException("permission is null");
    }
    return context.checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid());
  }

  private void startView() {
    Intent intent = new Intent(activity, QRCodeScannerActivity.class);
    intent.putExtra(QRCodeScannerActivity.SCAN_TYPE, (int) arguments.get("scanType"));
    activity.startActivityForResult(intent, REQUEST_CODE_SCAN_ACTIVITY);
  }

  private boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
    if (Build.VERSION.SDK_INT >= 23) {
      return activity.shouldShowRequestPermissionRationale(permission);
    }
    return false;
  }

  @TargetApi(Build.VERSION_CODES.M)
  private void requestPermissions() {
    activity.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
  }

  private void setNoPermissionsError() {
    pendingResult.error("permission", "you don't have the user permission to access the cameras", null);
    pendingResult = null;
    arguments = null;
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
      for (int i = 0; i < permissions.length; i++) {
        String permission = permissions[i];
        int grantResult = grantResults[i];
        if (permission.equals(Manifest.permission.CAMERA)) {
          if (grantResult == PackageManager.PERMISSION_GRANTED) {
            if (executeAfterPermissionGranted) {
              startView();
            }
          } else {
            setNoPermissionsError();
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_SCAN_ACTIVITY) {
      if (resultCode == Activity.RESULT_OK) {
        String string = data.getStringExtra(QRCodeScannerActivity.EXTRA_RESULT);
        pendingResult.success(string);
      } else {
        pendingResult.success(null);
      }
      pendingResult = null;
      arguments = null;
      return true;
    }
    return false;
  }
}
