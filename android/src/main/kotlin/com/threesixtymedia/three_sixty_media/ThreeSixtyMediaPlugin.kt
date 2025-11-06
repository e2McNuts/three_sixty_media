package com.threesixtymedia.three_sixty_media

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin

/** Registriert die PlatformView-Factory für 'ThreeSixtyMediaView'. */
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
    // Nichts weiter nötig; Factory wird automatisch deregistriert.
  }
}
