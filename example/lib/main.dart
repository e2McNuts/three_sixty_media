import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle, Uint8List;
import 'package:three_sixty_media/three_sixty_media.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final bytes = await rootBundle.load('assets/pano.png');
  runApp(DemoApp(imageBytes: bytes.buffer.asUint8List()));
}

class DemoApp extends StatelessWidget {
  final Uint8List imageBytes;
  const DemoApp({super.key, required this.imageBytes});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: TestApp(imageBytes: imageBytes));
  }
}

class TestApp extends StatefulWidget {
  final Uint8List imageBytes;
  const TestApp({super.key, required this.imageBytes});

  @override
  State<TestApp> createState() => _TestAppState();
}

class _TestAppState extends State<TestApp> {
  late final ThreeSixtyController controller;

  @override
  void initState() {
    super.initState();
    controller = ThreeSixtyController.attachToView(0);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('ThreeSixtyMedia')),
      body: Stack(
        children: [
          ThreeSixtyView(
            source: 'memory',
            controller: controller,
            onReady: () async {
              await controller.setFovLimits(min: 30, max: 160); // Fisheye möglich
              await controller.loadImageBytes(widget.imageBytes);
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
  }
}
