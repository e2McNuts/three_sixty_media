import 'package:flutter/material.dart';
import 'package:three_sixty_media/three_sixty_media.dart';
import 'package:file_picker/file_picker.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const DemoApp());
}

class DemoApp extends StatelessWidget {
  const DemoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(home: TestApp());
  }
}

class TestApp extends StatefulWidget {
  const TestApp({super.key});

  @override
  State<TestApp> createState() => _TestAppState();
}

class _TestAppState extends State<TestApp> {
  late final ThreeSixtyController controller;

  @override
  void initState() {
    super.initState();
    controller = ThreeSixtyController();
  }

  Future<void> _pickAndLoadImage() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles(
      type: FileType.image,
      allowMultiple: false,
    );

    if (result != null && result.files.single.path != null) {
      String filePath = result.files.single.path!;
      await controller.setImage(FileSource(filePath));
    } else {
      // User canceled the picker or no file was selected
      debugPrint("File picking cancelled or no file selected.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('ThreeSixtyMedia'),
        actions: [
          IconButton(
            icon: const Icon(Icons.folder_open),
            onPressed: _pickAndLoadImage,
            tooltip: 'Load local image',
          ),
        ],
      ),
      body: Stack(
        children: [
          ThreeSixtyView(
            controller: controller,
            onReady: () async {
              // Load a default asset image when the view is ready
              await controller.setImage(AssetSource('assets/test.jpg'));
              await controller.setFovLimits(min: 30, max: 160);
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
