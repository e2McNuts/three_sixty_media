package com.threesixtymedia.three_sixty_media

import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.plugin.common.StandardMessageCodec

/** Erzeugt pro Flutter-Instanz eine native View. */
class ThreeSixtyMediaFactory(
  private val messenger: BinaryMessenger,
  private val appContext: Context
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

  override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
    @Suppress("UNCHECKED_CAST")
    val creationParams = args as? Map<String, Any?>
    return ThreeSixtyMediaView(
      context = context,
      messenger = messenger,
      viewId = viewId,
      creationParams = creationParams
    )
  }
}
