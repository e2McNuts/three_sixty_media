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