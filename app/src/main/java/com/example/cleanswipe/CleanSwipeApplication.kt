package com.example.cleanswipe

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache

import com.example.cleanswipe.data.preferences.SettingsManager
import com.example.cleanswipe.util.ThemeHelper

class CleanSwipeApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        val settings = SettingsManager.getInstance(this).settings.value
        ThemeHelper.applySystemNightMode(this, settings.themeMode)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
