package com.threesixtymedia.three_sixty_media

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin

/** Registers the PlatformView factory for 'ThreeSixtyMediaView'. */
class ThreeSixtyMediaPlugin: FlutterPlugin {

  private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding

  override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    flutterPluginBinding = binding
    binding.platformViewRegistry.registerViewFactory(
      "ThreeSixtyMediaView",
      ThreeSixtyMediaFactory(binding.binaryMessenger, binding.applicationContext)
    )
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    // No further action needed; the factory is automatically deregistered.
  }
}
