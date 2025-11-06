import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'controller.dart';

/// Flutter-Widget, das den nativen Android-GL-View einbettet.
/// Web/iOS werden in späteren Versionen ergänzt.
class ThreeSixtyView extends StatefulWidget {
  const ThreeSixtyView({
    super.key,
    required this.source,
    this.controller,
    this.initialYaw = 0.0,
    this.initialPitch = 0.0,
    this.initialFov = 75.0,
    this.gestureEnabled = true,
    this.onReady,
    this.onViewChanged,
  });

  final String source;
  final ThreeSixtyController? controller;
  final double initialYaw;
  final double initialPitch;
  final double initialFov;
  final bool gestureEnabled;
  final VoidCallback? onReady;
  final void Function(double yaw, double pitch, double fov)? onViewChanged;

  @override
  State<ThreeSixtyView> createState() => _ThreeSixtyViewState();
}

class _ThreeSixtyViewState extends State<ThreeSixtyView> {
  // ignore: unused_field
  ThreeSixtyController? _controller;

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform != TargetPlatform.android) {
      return const SizedBox.shrink();
    }

    return AndroidView(
      viewType: 'ThreeSixtyMediaView',
      onPlatformViewCreated: _onPlatformViewCreated,
      creationParams: {
        'source': widget.source,
        'initialYaw': widget.initialYaw,
        'initialPitch': widget.initialPitch,
        'initialFov': widget.initialFov,
        'gestureEnabled': widget.gestureEnabled,
      },
      creationParamsCodec: const StandardMessageCodec(),
    );
  }

  void _onPlatformViewCreated(int id) {
    // Public factory statt privatem Konstruktor:
    _controller = widget.controller ?? ThreeSixtyController.attachToView(id);

    // Hinweis: Warnung "field not used" ist kein Fehler, wir behalten die Referenz
    // um später Methoden aufzurufen oder onDispose sauber freizugeben.
    widget.onReady?.call();
  }
}
