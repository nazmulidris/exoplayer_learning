/*
 * Copyright 2018 Nazmul Idris. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.naz.ep2

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat

open class MediaLibrary(
        val list: MutableList<MediaDescriptionCompat>) :
        List<MediaDescriptionCompat> by list {
    companion object : MediaLibrary(mutableListOf<MediaDescriptionCompat>())

    init {
        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("MP4 loaded from assets folder")
                    setMediaId("1")
                    setMediaUri(Uri.parse("asset:///video/stock_footage_video.mp4"))
                    setTitle("Stock footage")
                    setSubtitle("Local video")
                    build()
                })
        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("MP3 loaded from assets folder")
                    setMediaId("2")
                    setMediaUri(Uri.parse("asset:///audio/cielo.mp3"))
                    setTitle("Music")
                    setSubtitle("Local audio")
                    build()
                })
        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("MP3 loaded over HTTP")
                    setMediaId("3")
                    setMediaUri(Uri.parse("http://storage.googleapis.com/exoplayer-test-media-0/play.mp3"))
                    setTitle("Spoken track")
                    setSubtitle("Streaming audio")
                    build()
                })
        list.add(
                with(MediaDescriptionCompat.Builder()) {
                    setDescription("MP4 loaded over HTTP")
                    setMediaId("4")
                    setMediaUri(Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"))
                    setTitle("Short film")
                    setSubtitle("Streaming video")
                    build()
                })
    }
}