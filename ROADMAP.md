# ThreeSixtyMedia – Developer Roadmap
### Ziel
Ein Flutter-Plugin, das **360°-Bilder und -Videos** auf **allen Plattformen** anzeigen kann – zunächst Android & Web – mit einem einheitlichen Interface, Controller-API, UI-Overlay und optionalen Zusatzfunktionen.  
Der Fokus liegt auf **Stabilität, Performance und klarer Architektur**.

---

## 🤖 Version 0.1.0 – Android Core Foundation
> Ziel: Minimal lauffähige 360°-Bildanzeige auf Android  
> Fokus: Native Rendering, Steuerung, Flutter-Anbindung

### Funktionen
- Anzeige von **360°-Bildern** (equirectangular)
- Touch-Steuerung (Drag → Kamera-Pan)
- Zoom (Pinch-to-Zoom)
- Reset-Ansicht
- Rendering mit **OpenGL ES** über **TextureView**
- Kommunikation über **Platform Channel**
- Sauberes API-Grundgerüst

### Technische Aufgaben
**1. Plugin-Struktur**
```css
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
 │   │   └─ TouchHandler.kt
 ├─ example/
 │   └─ main.dart
```
**2. Native Rendering**
- Implementiere `Renderer360` mit:
    - Sphere Mesh (UV-mapped equirectangular texture)
    - Shader für Texturprojektion 
    - Kamera-Steuerung (Yaw, Pitch)
- OpenGL-Initialisierung mit `GLSurfaceView` oder `TextureView`
- Laden der Bitmap über `GlUtils.texImage2D()`

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
Ein einfaches Flutter-Widget, das auf Android 360°-Bilder anzeigen und rotieren kann.

---

## Version 0.2.0 – Web Core Foundation
> Ziel: Funktionierende Web-Implementierung mit gleichem API wie Android

### Funktionen
- 360°-Bilderanzeige (WebGL via Three.js)
- Maussteuerung (Drag)
- Zoom per Scroll
- Reset-Butto 
- Gleiche API wie Android (`ThreeSixtyView`, `ThreeSixtyController`)

### Technische Aufgaben
**1. Web Plugin Struktur**
```css
web/
 ├─ three_sixty_media_web.dart
 ├─ js/
 │   ├─ threesixty_web.js
 │   └─ threesixty_web.d.ts (optional)
```

**2. Web Rendering**
- Einbindung von **Three.js** (als externe Dependency)
- Erstelle `SphereGeometry` + `MeshBasicMaterial`
- Lade Textur (Image URL)  
- Kamera: `PerspectiveCamera`, Steuerung via `OrbitControls` 
- WebGL Renderer in `canvas`-Element einbetten

**3. Flutter → JS Communication**
- Implementiere `ThreeSixtyMediaWeb` über `PlatformInterface`
- Nutzung von `@JS()`-Interop, um Flutter API an JS weiterzugeben 
- Gleiche Dart-Signaturen wie Android

**4. Synchronisation API**
- Gemeinsame Dart-API für beide Plattformen (`controller.setYawPitch`)
- Web-seitig: State-Sync zwischen Dart und JS

**Deliverable:**  
Das gleiche Beispielprojekt läuft auch im Browser und zeigt ein interaktives 360°-Bild an.

---

## Version 0.3.0 – Android Video Engine
> Ziel: 360°-Videos auf Android abspielbar

### Funktionen
- Video-Wiedergabe (MP4)
- Play/Pause    
- Seek 
- Texture über ExoPlayer → OpenGL Sphere
- Basis-Controller-API

### Technische Aufgaben
- `Renderer360` erweitert um VideoTexture (SurfaceTexture)
- Anbindung an ExoPlayer
- PlatformChannel:
    - `play()`, `pause()`, `seekTo(ms)`    
- Update der `ThreeSixtyController` in Dart

**Deliverable:**  
Videos können abgespielt, pausiert und per Code gesteuert werden.

---

## Version 0.4.0 – Web Video Engine
> Ziel: 360°-Videos auch im Browser abspielbar

### Funktionen
- Video via `<video>`-Tag als Textu
- Play/Pause/Seek über Dart 
- Synchronisierte Controller-API

### Technische Aufgaben
- Ersetze ImageTexture durch HTML5 VideoTexture in Three.js
- API-Kompatibilität mit Android
- Performanceoptimierung (GPU decode falls möglich)

---

## Version 0.5.0 – Unified Controller & Base API
> Ziel: Einheitliche Steuerung und API über alle Plattformen

### Funktionen
- `ThreeSixtyController` für alle Plattformen
- `getViewState()` (Yaw, Pitch, Zoom) 
- EventStreams (`onViewChanged`, `onReady`)
- Plugin Lifecycle (load → ready → dispose)
- Architektur-Dokumentation

---

## Version 0.6.0
> Ziel: Einheitliches UI mit Buttons, Seekbar und Settings

### Funktionen
- UI Overlay mit:
    - Play/Pause  
    - Seekbar
    - Fullscreen    
    - Settings (Qualität, Speed) 
- Anpassbar via `ThreeSixtyUiTheme`
- Touch Hide/Show Toggle
- Separate `ThreeSixtyMediaPlayer`-Widget (mit eingebautem UI)

---

## Version 0.7.0 – Marker System & Timeline
> Ziel: Interaktive Marker auf Zeitachse oder im Raum

### Funktionen 
- Zeitbasierte Marker mit Labels und Icons
- Klick-Callback (`onMarkerTap`)
- JSON-Import/Export
- Integration in Seekbar

---

## Version 0.8.0 – Qualität & Playback Options
> Ziel: Verbesserte Wiedergabe mit Einstellmöglichkeiten

### Funktionen
- Qualitätsauswahl (manuell)
- Playback Speed
- Loop, Autoplay 
- Buffering-Anzeige
- Volume Control

---

## Version 0.9.0 – Stabilisierung & Cross-Platform
> Ziel: iOS Support + Tests + Dokumentation

### Funktionen
- iOS Rendering mit Metal
- Gyroskop-Unterstützung
- CI/CD Setup
- Performanceprofiling

---

## Version 1.0.0 – MVP-Release
> Ziel: Stabiles, getestetes Plugin mit 360° Bild- & Videoanzeige, UI & Timeline

### Features im MVP
- 360° Bilder & Videos auf Android + Web + iOS
- Einheitliche API
- Playbar, Seekbar, Quality Menu, Marker Timeline
- Flutter Controller-API
- Themes + Custom UI
- Beispiel-App mit Demos

### Deliverables
- Vollständige Dokumentation
- Beispiel-App (`example/`)
- Tests (Unit + Integration)
- CI/CD Pipeline (GitHub Actions)
- Versionierung & Veröffentlichung auf pub.dev

---

## Gesamtarchitektur
```css
ThreeSixtyMedia
├── Core (Dart)
│   ├── View, Controller, PlatformInterface
│   ├── Event Streams
│   └── UI Overlay Widgets
├── Platform Implementations
│   ├── Android → OpenGL + ExoPlayer
│   ├── Web → Three.js + HTML5 Video
│   └── (später) iOS → Metal
└── Example App (Showcase)
```