import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Erzeugt einen eigenen MethodChannel pro View.
/// Dadurch muss kein viewId in jedem Aufruf mitgegeben werden.
MethodChannel _channelForView(int viewId) =>
    MethodChannel('com.threesixtymedia/core/$viewId');

/// Steuert eine native ThreeSixtyMedia-Instanz.
/// Version 0.1.0: Nur Android, nur Bilder.
class ThreeSixtyController {
  /// Private interne Factory – kann nur innerhalb dieser Datei aufgerufen werden.
  ThreeSixtyController._(this._viewId)
      : _channel = _channelForView(_viewId) {
    // 🆕 Event-Handler für Rückmeldungen vom nativen Code (z. B. FOV-Änderung)
    _channel.setMethodCallHandler(_handleNativeCalls);
  }

  // ignore: unused_field
  final int _viewId;
  final MethodChannel _channel;

  // 🆕 Callback für FOV-Änderungen (z. B. bei Pinch)
  void Function(double fov)? _onFovChanged;

  /// Öffentliche Factory, um den Controller an eine View zu binden.
  /// Beispiel: `ThreeSixtyController.attachToView(viewId)`
  static ThreeSixtyController attachToView(int viewId) {
    return ThreeSixtyController._(viewId);
  }

  // --------------------------------------------------------------------------
  // 🧩 Öffentliche API-Methoden
  // --------------------------------------------------------------------------

  /// Setzt Blickrichtung (Radiant). Pitch wird intern geclamped.
  Future<void> setYawPitch(double yaw, double pitch) async {
    await _channel.invokeMethod('setYawPitch', {'yaw': yaw, 'pitch': pitch});
  }

  /// Setzt Field-of-View (Grad).
  Future<void> setFov(double fov) async {
    await _channel.invokeMethod('setFov', {'fov': fov});
  }

  /// Legt minimale und maximale FOV-Werte fest.
  ///
  /// Damit kann man den Zoom-Bereich einschränken oder erweitern (z. B. Fisheye).
  Future<void> setFovLimits({required double min, required double max}) async {
    await _channel.invokeMethod('setFovLimits', {'min': min, 'max': max});
  }

  /// Setzt die Ansicht auf die Standardwerte zurück.
  Future<void> resetView() async {
    await _channel.invokeMethod('resetView');
  }

  /// Lädt ein neues Bild (Asset/Datei/URI).
  Future<void> loadImage(String source) async {
    await _channel.invokeMethod('loadImage', {'source': source});
  }

  /// Lädt ein Bild direkt aus Bytes (vom rootBundle).
  Future<void> loadImageBytes(Uint8List bytes) async {
    await _channel.invokeMethod('loadImageBytes', {'bytes': bytes});
  }

  /// Liest aktuellen Zustand (kann auf Android leicht verzögert sein).
  Future<ViewState> getViewState() async {
    final map = await _channel.invokeMapMethod<String, dynamic>('getViewState');
    final yaw = (map?['yaw'] as num?)?.toDouble() ?? 0.0;
    final pitch = (map?['pitch'] as num?)?.toDouble() ?? 0.0;
    final fov = (map?['fov'] as num?)?.toDouble() ?? 75.0;
    return ViewState(yaw, pitch, fov);
  }

  // --------------------------------------------------------------------------
  // 🧭 Event Listener
  // --------------------------------------------------------------------------

  /// Wird aufgerufen, wenn sich der Zoom (FOV) ändert (z. B. durch Pinch oder Reset).
  set onFovChanged(void Function(double fov)? callback) {
    _onFovChanged = callback;
  }

  // --------------------------------------------------------------------------
  // 🧩 Interner Channel-Handler (Android -> Flutter)
  // --------------------------------------------------------------------------

  // 🆕 Empfängt Events vom nativen Renderer (z. B. onFovChanged)
  Future<void> _handleNativeCalls(MethodCall call) async {
    switch (call.method) {
      case 'onFovChanged':
        final args = call.arguments as Map?;
        if (args != null && args.containsKey('fov')) {
          final fov = (args['fov'] as num).toDouble();
          _onFovChanged?.call(fov);
        }
        break;

      // Optional: später weitere Events hinzufügen
      default:
        debugPrint('Unhandled native method: ${call.method}');
    }
  }
}

/// Einfacher Datenträger für die aktuelle Kameraansicht.
class ViewState {
  final double yaw, pitch, fov;
  const ViewState(this.yaw, this.pitch, this.fov);
}
