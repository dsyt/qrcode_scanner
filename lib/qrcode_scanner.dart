import 'dart:async';

import 'package:flutter/services.dart';

class QrcodeScanner {
  static const MethodChannel _channel =
      const MethodChannel('qrcode_scanner');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> scanQRCode(int type) async {
    final Map<String, Object> argument = Map();
    argument['scanType'] = type;
    argument['handlePermissions'] = true;
    argument['executeAfterPermissionGranted'] = true;
    return await _channel.invokeMethod("scanQRCode", argument);
  }

}
