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

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v7.app.AppCompatActivity
import android.util.Rational
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kotlinx.android.synthetic.main.activity_video.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast


class VideoActivity : AppCompatActivity(), AnkoLogger {

    lateinit var mMediaSession: MediaSessionCompat
    lateinit var mMediaSessionConnector: MediaSessionConnector
    lateinit var mPlayerHolder: PlayerHolder
    val mPlayerState = PlayerState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        mMediaSession = MediaSessionCompat(this, packageName)
        mMediaSessionConnector = MediaSessionConnector(mMediaSession)
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
        // Note: do not pass a null to the 3rd param below, it will cause a NPE
        mMediaSessionConnector.setPlayer(mPlayerHolder.mPlayer, null)
        mMediaSession.isActive = true
    }

    override fun onStop() {
        super.onStop()
        mMediaSessionConnector.setPlayer(null, null)
        mMediaSession.isActive = false
        releasePlayer()
    }

    fun initPlayer() {
        mPlayerHolder = PlayerHolder(this, exoplayerview_activity_video, mPlayerState)

        mPlayerHolder.mPlayer.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        toast("playback ended")
                    }
                    Player.STATE_READY -> when (playWhenReady) {
                        true -> {
                            toast("playback started")
                        }
                        false -> {
                            toast("playback paused")
                        }
                    }
                }
            }
        })
    }

    fun releasePlayer() {
        mPlayerHolder.release()
    }

    // Picture in Picture
    override fun onUserLeaveHint() {

        enterPictureInPictureMode(
                with(PictureInPictureParams.Builder()) {
                    val width = 16
                    val height = 9
                    setAspectRatio(Rational(width, height))
                    build()
                })
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        exoplayerview_activity_video.useController = !isInPictureInPictureMode
    }

}
