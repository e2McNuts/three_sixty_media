import 'package:flutter/material.dart';
import 'package:three_sixty_media/three_sixty_media.dart';

// Reset Button is working fine.

/// Overlay-UI für den 360°-Viewer.
class ThreeSixtyControls extends StatefulWidget {
  final ThreeSixtyController controller;
  final double initialFov;
  final double minFov;
  final double maxFov;

  const ThreeSixtyControls({
    super.key,
    required this.controller,
    this.initialFov = 75,
    this.minFov = 30,
    this.maxFov = 150,
  });

  @override
  State<ThreeSixtyControls> createState() => _ThreeSixtyControlsState();
}

class _ThreeSixtyControlsState extends State<ThreeSixtyControls> {
  double _fov = 75;

  @override
void initState() {
  super.initState();
  widget.controller.onFovChanged = (newFov) {
    setState(() => _fov = newFov);
  };
}

  Future<void> _zoomIn() async {
    _fov = (_fov - 5).clamp(widget.minFov, widget.maxFov);
    await widget.controller.setFov(_fov);
    setState(() {});
  }

  Future<void> _zoomOut() async {
    _fov = (_fov + 5).clamp(widget.minFov, widget.maxFov);
    await widget.controller.setFov(_fov);
    setState(() {});
  }

  Future<void> _reset() async {
    await widget.controller.resetView();
    setState(() => _fov = widget.initialFov);
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      right: 12,
      bottom: 12,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          FloatingActionButton(
            heroTag: 'zoomIn',
            mini: true,
            onPressed: _zoomIn,
            tooltip: 'Zoom In',
            child: const Icon(Icons.add),
          ),
          const SizedBox(height: 8),
          FloatingActionButton(
            heroTag: 'zoomOut',
            mini: true,
            onPressed: _zoomOut,
            tooltip: 'Zoom Out',
            child: const Icon(Icons.remove),
          ),
          const SizedBox(height: 8),
          FloatingActionButton(
            heroTag: 'reset',
            mini: true,
            onPressed: _reset,
            tooltip: 'Reset View',
            child: const Icon(Icons.refresh),
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: Colors.black54,
              borderRadius: BorderRadius.circular(4),
            ),
            child: Text(
              'FOV: ${_fov.toStringAsFixed(0)}°',
              style: const TextStyle(color: Colors.white, fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }
}
