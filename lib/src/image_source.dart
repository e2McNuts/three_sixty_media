import 'dart:typed_data';

/// Base class for different image sources.
abstract class ImageSource {}

/// Loads an image from the app's asset bundle.
class AssetSource extends ImageSource {
  final String path;
  AssetSource(this.path);
}

/// Loads an image from a URL.
class UrlSource extends ImageSource {
  final String url;
  UrlSource(this.url);
}

/// Loads an image from a local file.
class FileSource extends ImageSource {
  final String path;
  FileSource(this.path);
}

/// Loads an image from a byte array in memory.
class MemorySource extends ImageSource {
  final Uint8List bytes;
  MemorySource(this.bytes);
}
