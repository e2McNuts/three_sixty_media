import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'image_source.dart';

MethodChannel? _channelForView(int viewId) =>
    MethodChannel('com.threesixtymedia/core/$viewId');

class ThreeSixtyController {
  ThreeSixtyController() {
    // Initialisiere den Event-Handler für die native Seite
    _channel?.setMethodCallHandler(_handleNativeCalls);
  }

  MethodChannel? _channel;

  void Function(double fov)? _onFovChanged;
  void Function(String message)? _onError;

  void attachToView(int viewId) {
    _channel = _channelForView(viewId);
    _channel?.setMethodCallHandler(_handleNativeCalls);
  }

  Future<void> setImage(ImageSource source) async {
    if (source is AssetSource) {
      await setImageFromAsset(source.path);
    } else if (source is UrlSource) {
      await setImageFromUrl(source.url);
    } else if (source is FileSource) {
      await setImageFromFile(File(source.path));
    } else if (source is MemorySource) {
      await _loadImageBytes(source.bytes);
    }
  }

  Future<void> setImageFromAsset(String assetPath) async {
    final data = await rootBundle.load(assetPath);
    await _loadImageBytes(data.buffer.asUint8List());
  }

  Future<void> setImageFromUrl(String url) async {
    final response = await http.get(Uri.parse(url));
    if (response.statusCode == 200) {
      await _loadImageBytes(response.bodyBytes);
    } else {
      _onError?.call('Failed to load image from $url');
    }
  }

  Future<void> setImageFromFile(File file) async {
    final bytes = await file.readAsBytes();
    await _loadImageBytes(bytes);
  }

  Future<void> _loadImageBytes(Uint8List bytes) async {
    await _channel?.invokeMethod('loadImageBytes', {'bytes': bytes});
  }

  Future<void> setYawPitch(double yaw, double pitch) async {
    await _channel?.invokeMethod('setYawPitch', {'yaw': yaw, 'pitch': pitch});
  }

  Future<void> setFov(double fov) async {
    await _channel?.invokeMethod('setFov', {'fov': fov});
  }

  Future<void> setFovLimits({required double min, required double max}) async {
    await _channel?.invokeMethod('setFovLimits', {'min': min, 'max': max});
  }

  Future<void> resetView() async {
    await _channel?.invokeMethod('resetView');
  }

  Future<ViewState> getViewState() async {
    final map =
        await _channel?.invokeMapMethod<String, dynamic>('getViewState');
    final yaw = (map?['yaw'] as num?)?.toDouble() ?? 0.0;
    final pitch = (map?['pitch'] as num?)?.toDouble() ?? 0.0;
    final fov = (map?['fov'] as num?)?.toDouble() ?? 75.0;
    return ViewState(yaw, pitch, fov);
  }

  set onFovChanged(void Function(double fov)? callback) {
    _onFovChanged = callback;
  }

  set onError(void Function(String message)? callback) {
    _onError = callback;
  }

  Future<void> _handleNativeCalls(MethodCall call) async {
    switch (call.method) {
      case 'onFovChanged':
        final args = call.arguments as Map?;
        if (args != null && args.containsKey('fov')) {
          final fov = (args['fov'] as num).toDouble();
          _onFovChanged?.call(fov);
        }
        break;

      case 'onError':
        final args = call.arguments as Map?;
        if (args != null && args.containsKey('message')) {
          final msg = args['message'] as String;
          _onError?.call(msg);
        }
        break;

      default:
        debugPrint('Unhandled native method: ${call.method}');
    }
  }
}

class ViewState {
  final double yaw, pitch, fov;
  const ViewState(this.yaw, this.pitch, this.fov);
}
