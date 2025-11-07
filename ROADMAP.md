# ThreeSixtyMedia – Developer Roadmap
### Ziel
Ein Flutter-Plugin, das **360°-Bilder und -Videos** auf **allen Plattformen** anzeigen kann – zunächst Android, dann Video-Support, danach Web – mit einem einheitlichen Interface, Controller-API, UI-Overlay und optionalen Zusatzfunktionen.  
Der Fokus liegt auf **Stabilität, Performance und klarer Architektur**.

---

## 🤖 Version 0.1.0 – Android Core Foundation ✅
> Ziel: Minimal lauffähige 360°-Bildanzeige auf Android  
> Fokus: Native Rendering, Steuerung, Flutter-Anbindung

### Funktionen
- Anzeige von **360°-Bildern** (equirectangular)
- Touch-Steuerung (Drag → Kamera-Pan)
- Zoom (Pinch-to-Zoom)
- Reset-Ansicht
- Rendering mit **OpenGL ES** über **GLSurfaceView**
- Kommunikation über **Platform Channel**
- Sauberes API-Grundgerüst

### Technische Aufgaben
**1. Plugin-Struktur**
```
three_sixty_media/
 ├─ lib/
 │   ├─ three_sixty_media.dart
 │   ├─ src/
 │   │   ├─ controller.dart
 │   │   ├─ view.dart
 │   │   ├─ platform_interface.dart
 │   │   └─ method_channel_impl.dart
 ├─ android/
 │   ├─ src/main/java/com/threesixtymedia/
 │   │   ├─ ThreeSixtyMediaPlugin.kt
 │   │   ├─ ThreeSixtyMediaView.kt
 │   │   ├─ Renderer360.kt
 │   │   ├─ SphereMesh.kt
 │   │   └─ TouchController.kt
 ├─ example/
 │   └─ main.dart
```
**2. Native Rendering**
- Implementiere `Renderer360` mit:
    - Sphere Mesh (UV-mapped equirectangular texture)
    - Shader für Texturprojektion 
    - Kamera-Steuerung (Yaw, Pitch)
- OpenGL-Initialisierung mit `GLSurfaceView`
- Laden der Bitmap über `GlUtils.texImage2D()`

**3. Steuerung**
- Touch → Kamera Rotation (Drag)
- Pinch → Zoom (FOV verändern)
- Doppeltap → Reset auf Default View

**4. Flutter-Integration**
- MethodChannel für
    - `loadImage(String path)`
    - `setYawPitch(double yaw, double pitch)`
    - `resetView()`
- Public Widget:
```dart
ThreeSixtyView(imageUrl: 'assets/pano.jpg');
```

**5. Testing & Example**
- Beispiel-App mit einfachem Bild
- Dokumentation (README + API docs)  

**Deliverable:**  
Ein einfaches Flutter-Widget, das auf Android 360°-Bilder anzeigen und rotieren kann. ✅

---

## 🎬 Version 0.2.0 – Android Video Engine
> Ziel: 360°-Videos auf Android abspielbar  
> Fokus: Video-Texture-Streaming, Playback-Control

### Funktionen
- Video-Wiedergabe (MP4, WebM)
- Play / Pause / Seek
- Texture über **ExoPlayer** → OpenGL Sphere
- Basis-Controller-API für Videowiedergabe
- Buffering-Anzeige
- Video aus Assets, Dateien oder URLs laden

### Technische Aufgaben
**1. Video-Texture-Integration**
- `SurfaceTexture` für Video-Frame-Streaming
- Anbindung an **ExoPlayer** (oder MediaPlayer als Fallback)
- Synchronisation zwischen Video-Frames und GL-Rendering

**2. Renderer-Erweiterung**
```kotlin
class Renderer360 {
    private var videoTexture: SurfaceTexture? = null
    private var player: ExoPlayer? = null
    
    fun loadVideo(uri: Uri) {
        // ExoPlayer Setup
        // Surface für Texture bereitstellen
    }
    
    fun updateVideoFrame() {
        videoTexture?.updateTexImage()
        // GL-Texture aktualisieren
    }
}
```

**3. Playback-Controller**
- PlatformChannel-Erweiterungen:
    - `play()`
    - `pause()`
    - `seekTo(milliseconds)`
    - `getCurrentPosition()`
    - `getDuration()`
- Event-Streams für:
    - `onPlaybackStateChanged`
    - `onPositionChanged`
    - `onBuffering`

**4. Flutter-API**
```dart
class ThreeSixtyController {
  Future<void> loadVideo(String url);
  Future<void> play();
  Future<void> pause();
  Future<void> seekTo(Duration position);
  
  Stream<PlaybackState> get playbackState;
  Stream<Duration> get position;
}
```

**5. Beispiel-App**
- Video-Demo mit lokalem MP4
- Playback-Controls (Play/Pause/Seekbar)

**Deliverable:**  
360°-Videos können auf Android abgespielt, pausiert und durchsucht werden.

---

## 🎥 Version 0.3.0 – Unified Controller & Video UI
> Ziel: Vollständige Video-Integration mit UI-Overlay  
> Fokus: Benutzerfreundliche Steuerung, einheitliche API

### Funktionen
- Vollständiges Video-UI-Overlay:
    - Play/Pause-Button
    - Seekbar mit Vorschau
    - Zeitanzeige (Current / Total)
    - Vollbild-Toggle
    - Lautstärke-Steuerung
- `ThreeSixtyController` für Bilder **und** Videos
- `ThreeSixtyMediaPlayer`-Widget (mit eingebautem UI)
- Anpassbares Theme-System

### Technische Aufgaben
**1. UI-Komponenten**
```dart
class ThreeSixtyMediaPlayer extends StatefulWidget {
  final MediaSource source; // Image oder Video
  final ThreeSixtyUiTheme? theme;
  
  @override
  Widget build(BuildContext context) {
    return Stack([
      ThreeSixtyView(...),
      if (source.isVideo) VideoControlsOverlay(...),
    ]);
  }
}
```

**2. Theme-System**
```dart
class ThreeSixtyUiTheme {
  final Color primaryColor;
  final Color accentColor;
  final bool showTimestamp;
  final bool autoHideControls;
  final Duration autoHideDuration;
}
```

**3. Controller-Vereinheitlichung**
- Gemeinsame Basis für Bild- und Video-Modi
- State-Management für UI-Synchronisation
- Event-Streams für alle Zustandsänderungen

**Deliverable:**  
Ein vollständiges, benutzerfreundliches Video-Player-Widget mit anpassbarem UI.

---

## 🌐 Version 0.4.0 – Web Core Foundation
> Ziel: Funktionierende Web-Implementierung mit gleichem API wie Android  
> Fokus: WebGL via Three.js, Plattform-Abstraktion

### Funktionen
- 360°-Bilderanzeige (WebGL via Three.js)
- Maussteuerung (Drag)
- Zoom per Scroll
- Reset-Button 
- Gleiche API wie Android (`ThreeSixtyView`, `ThreeSixtyController`)

### Technische Aufgaben
**1. Web Plugin Struktur**
```
web/
 ├─ three_sixty_media_web.dart
 ├─ js/
 │   ├─ threesixty_web.js
 │   └─ threesixty_web.d.ts (optional)
```

**2. Web Rendering**
- Einbindung von **Three.js** (als externe Dependency)
- Erstelle `SphereGeometry` + `MeshBasicMaterial`
- Lade Textur (Image URL)  
- Kamera: `PerspectiveCamera`, Steuerung via `OrbitControls` 
- WebGL Renderer in `canvas`-Element einbetten

**3. Platform Interface**
```dart
// lib/src/platform_interface.dart
abstract class ThreeSixtyMediaPlatform {
  Future<void> loadImage(String path);
  Future<void> loadImageBytes(Uint8List bytes);
  Future<void> setYawPitch(double yaw, double pitch);
  Future<void> setFov(double fov);
  
  static ThreeSixtyMediaPlatform get instance {
    if (kIsWeb) return ThreeSixtyMediaWeb();
    return ThreeSixtyMediaMethodChannel();
  }
}
```

**4. Flutter → JS Communication**
- Implementiere `ThreeSixtyMediaWeb` über `PlatformInterface`
- Nutzung von `@JS()`-Interop, um Flutter API an JS weiterzugeben 
- Gleiche Dart-Signaturen wie Android

**Deliverable:**  
Das gleiche Beispielprojekt läuft auch im Browser und zeigt ein interaktives 360°-Bild an.

---

## 🎬 Version 0.5.0 – Web Video Engine
> Ziel: 360°-Videos auch im Browser abspielbar

### Funktionen
- Video via `<video>`-Tag als Texture
- Play/Pause/Seek über Dart 
- Synchronisierte Controller-API mit Android
- HLS/DASH-Streaming-Support (optional)

### Technische Aufgaben
- Ersetze ImageTexture durch HTML5 VideoTexture in Three.js
- API-Kompatibilität mit Android
- Performanceoptimierung (GPU decode falls möglich)
- Cross-Browser-Testing (Chrome, Firefox, Safari)

---

## 🎯 Version 0.6.0 – Marker System & Timeline
> Ziel: Interaktive Marker auf Zeitachse oder im Raum

### Funktionen 
- Zeitbasierte Marker mit Labels und Icons
- Räumliche Marker (Hotspots im 360°-Raum)
- Klick-Callback (`onMarkerTap`)
- JSON-Import/Export
- Integration in Seekbar

### Technische Aufgaben
```dart
class Marker {
  final Duration? timestamp; // für Videos
  final Vector3? position;    // für räumliche Marker
  final String label;
  final IconData icon;
  final VoidCallback? onTap;
}

controller.addMarker(Marker(
  timestamp: Duration(seconds: 30),
  label: "Wichtiger Moment",
  icon: Icons.star,
  onTap: () => print("Marker clicked"),
));
```

---

## 🎨 Version 0.7.0 – Quality & Playback Options
> Ziel: Verbesserte Wiedergabe mit Einstellmöglichkeiten

### Funktionen
- Qualitätsauswahl (Auto/SD/HD/4K)
- Playback Speed (0.5x - 2.0x)
- Loop, Autoplay 
- Buffering-Anzeige
- Volume Control
- Subtitle-Support (WebVTT)

---

## 📱 Version 0.8.0 – iOS Support
> Ziel: Native iOS-Implementierung

### Funktionen
- iOS Rendering mit **Metal**
- SceneKit als Alternative
- Gyroskop-Unterstützung
- ARKit-Integration (optional)

---

## 🚀 Version 0.9.0 – Stabilisierung & Cross-Platform
> Ziel: Produktionsreife auf allen Plattformen

### Funktionen
- Gyroskop-Unterstützung (Android/iOS/Web)
- CI/CD Setup (GitHub Actions)
- Performanceprofiling
- Memory-Leak-Tests
- Accessibility-Features

---

## 🎉 Version 1.0.0 – MVP-Release
> Ziel: Stabiles, getestetes Plugin mit 360° Bild- & Videoanzeige, UI & Timeline

### Features im MVP
- 360° Bilder & Videos auf Android + Web + iOS
- Einheitliche API
- Playbar, Seekbar, Quality Menu, Marker Timeline
- Flutter Controller-API
- Themes + Custom UI
- Beispiel-App mit Demos
- Gyroskop-Support

### Deliverables
- Vollständige Dokumentation
- Beispiel-App (`example/`)
- Tests (Unit + Integration)
- CI/CD Pipeline (GitHub Actions)
- Versionierung & Veröffentlichung auf pub.dev

---

## Gesamtarchitektur
```
ThreeSixtyMedia
├── Core (Dart)
│   ├── View, Controller, PlatformInterface
│   ├── Event Streams
│   └── UI Overlay Widgets
├── Platform Implementations
│   ├── Android → OpenGL ES + ExoPlayer
│   ├── Web → Three.js + HTML5 Video
│   └── iOS → Metal + AVPlayer
└── Example App (Showcase)
```

---

## Prioritäten-Übersicht

| Version | Fokus | Plattform | Status |
|---------|-------|-----------|--------|
| 0.1.0 | Bild-Rendering | Android | ✅ Abgeschlossen |
| 0.2.0 | Video-Engine | Android | 🔄 In Arbeit |
| 0.3.0 | Video-UI | Android | ⏳ Geplant |
| 0.4.0 | Web-Bilder | Web | ⏳ Geplant |
| 0.5.0 | Web-Videos | Web | ⏳ Geplant |
| 0.6.0 | Marker-System | Cross-Platform | ⏳ Geplant |
| 0.7.0 | Qualität & Optionen | Cross-Platform | ⏳ Geplant |
| 0.8.0 | iOS-Support | iOS | ⏳ Geplant |
| 0.9.0 | Stabilisierung | Cross-Platform | ⏳ Geplant |
| 1.0.0 | MVP-Release | All Platforms | ⏳ Geplant |