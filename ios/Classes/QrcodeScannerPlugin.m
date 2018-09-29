#import "QrcodeScannerPlugin.h"
#import <qrcode_scanner/qrcode_scanner-Swift.h>

@implementation QrcodeScannerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftQrcodeScannerPlugin registerWithRegistrar:registrar];
}
@end
