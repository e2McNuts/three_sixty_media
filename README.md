# Three Sixty Media `v0.1.0‑alpha`

`three_sixty_media` is a Flutter plugin for displaying 360° media inside Flutter apps.
The current `v0.1.0‑alpha` release focuses on Android and provides a minimal, yet functional 360° **image** viewer.
Future releases will expand support to videos and other platforms (web, iOS, desktop) as outlined in the roadmap.

## Overview
The goal of Three Sixty Media is to offer a unified API and a set of widgets for viewing 360° photos (and later videos) across different platforms.
The Android implementation uses a native `GLSurfaceView` and OpenGL ES to render an inward‑facing sphere mesh to which an equirectangular image is mapped. Camera orientation (yaw, pitch) and field of view (FOV) can be manipulated via gestures or programmatically.
The Flutter side exposes a `ThreeSixtyView` widget and a `ThreeSixtyController` for interacting with the native renderer. A small overlay widget (`ThreeSixtyControls`) provides zoom in/out buttons, a reset button and displays the current FOV.

## Platform support
Platform	Status	Planned version
Android	✅ Implemented – core 360° image rendering with interactive controls
github.com
	Video support in v0.2.0; video UI in v0.3.0
Web	🔴 Not yet implemented	360° images planned for v0.4.0; videos for v0.5.0
github.com

iOS	🔴 Not yet implemented	Targeted for v0.8.0 with a Metal/SceneKit renderer
github.com

Windows & Linux	🔴 Not yet implemented	Planned post‑1.0.0

Refer to the [Developer Roadmap](ROADMAP.md)
 for details on upcoming milestones.

## Features (v0.1.0)

This alpha release is intentionally simple but provides a solid base for future development.
Currently supported features include:
- **Equirectangular image rendering** – images are rendered onto an inward‑facing sphere using OpenGL ES.
- **Gesture‑based navigation** – dragging changes yaw & pitch, pinch‑to‑zoom adjusts the field of view and double‑tap resets the view.
These gestures are handled by a native `TouchController` which translates touch events to camera rotation and zoom. 
- **Programmatic control** – via `ThreeSixtyController` you can set yaw/pitch, FOV, FOV limits, reset the view and load new images. 
- **Image loading from assets, files or memory** – call `loadImage()` with an asset or file path or `loadImageBytes()` to supply raw bytes. 
- **View state retrieval** – query the current yaw, pitch and FOV with `getViewState()`.
- **Event callbacks** – register `onFovChanged` and `onError` listeners to respond to native changes or errors. 

On top of the core widget, the **ThreeSixtyControls** overlay adds simple UI controls for zooming in/out, resetting the view and displaying the current FOV. 

## Usage
Add `three_sixty_media` as a dependency in your `pubspec.yaml` (see the included `pubspec.yaml` for minimal SDK constraints), then import and use the provided widgets and controller:
``` dart
import 'package:flutter/services.dart' show rootBundle;
import 'package:three_sixty_media/three_sixty_media.dart';

class My360Viewer extends StatefulWidget {
  @override
  State<My360Viewer> createState() => _My360ViewerState();
}

class _My360ViewerState extends State<My360Viewer> {
  late final ThreeSixtyController controller;

  @override
  void initState() {
    super.initState();
    controller = ThreeSixtyController.attachToView(0);
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Uint8List>(
      future: rootBundle.load('assets/panorama.jpg').then((data) => data.buffer.asUint8List()),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return const CircularProgressIndicator();
        final imageBytes = snapshot.data!;
        return Scaffold(
          body: Stack(
            children: [
              ThreeSixtyView(
                source: 'memory',
                controller: controller,
                onReady: () async {
                  // customise FOV limits (30° – 160°) and load image
                  await controller.setFovLimits(min: 30, max: 160);
                  await controller.loadImageBytes(imageBytes);
                },
              ),
              ThreeSixtyControls(
                controller: controller,
                initialFov: 75,
                minFov: 30,
                maxFov: 160,
              ),
            ],
          ),
        );
      },
    );
  }
}
```
The example above mirrors the demo in `example/lib/main.dart`.
Use `source: 'memory'` when loading images via `loadImageBytes()`; otherwise set `source` to an asset or file path and omit the controller’s load call.

## Development Roadmap
The roadmap in `ROADMAP.md` outlines the planned evolution of this plugin. Key milestones include:
- **v0.2.0 – Android video engine**: add 360° video playback using ExoPlayer, with play/pause/seek controls and event streams. 
- **v0.3.0 – Unified controller & video UI**: build a `ThreeSixtyMediaPlayer` widget with play bar, seek bar, fullscreen toggle and a unified controller for images and videos. 
- **v0.4.0 / v0.5.0 – Web support**: implement 360° images and videos in the browser via Three.js and HTML5 video. 
- **v0.6.0 – Marker system & timeline**: allow time‑based and spatial markers with callbacks and integration into the seek bar. 
- **v0.7.0 – Quality & playback options**: quality selection, playback speed, looping, buffering indicators and subtitle support. 
- **v0.8.0 – iOS support**: native renderer using Metal/SceneKit and optional ARKit integration. 
- **v0.9.0 – Stabilisation & cross‑platform**: gyroscope support across platforms, CI/CD, performance profiling and accessibility. 
- **v1.0.0 – MVP release**: cross‑platform 360° image & video player with unified API, customizable UI, marker timeline, gyroscope support and example app. 

Check [ROADMAP.md](ROADMAP.md) for the full breakdown of tasks and progress.

## Contributing
Contributions are welcome! The project is in its early stages – feedback, issue reports and pull requests (especially for platform ports or improved documentation) are highly appreciated. Please open an issue to discuss larger changes before submitting a PR.

## License
This project is licensed under the **Three Sixty Media License (Non‑Commercial)**.
Commercial use requires prior written permission from Magnus Ormos.