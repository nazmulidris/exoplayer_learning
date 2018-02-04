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
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v7.app.AppCompatActivity
import android.util.Rational
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import kotlinx.android.synthetic.main.activity_video.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast


class VideoActivity : AppCompatActivity(), AnkoLogger {

    lateinit var mMediaSession: MediaSessionCompat
    lateinit var mMediaSessionConnector: MediaSessionConnector
    lateinit var mPlayerHolder: PlayerHolder
    val mPlayerState = PlayerState()

    // Android lifecycle hooks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        createMediaSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaSession()
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
        activateMediaSession()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
        deactivateMediaSession()
    }

    // MediaSession related functions
    fun createMediaSession() {
        mMediaSession = MediaSessionCompat(this, packageName)
        mMediaSessionConnector = MediaSessionConnector(mMediaSession)
        // If QueueNavigator isn't set, then mMediaSessionConnector will not handle following
        // MediaSession actions (and they won't show up in the minimized PIP activity):
        // [ACTION_SKIP_PREVIOUS], [ACTION_SKIP_NEXT], [ACTION_SKIP_TO_QUEUE_ITEM]
        mMediaSessionConnector.setQueueNavigator(object : TimelineQueueNavigator(mMediaSession) {
            override fun getMediaDescription(windowIndex: Int): MediaDescriptionCompat {
                return with(MediaDescriptionCompat.Builder()) {
                    setDescription("Description $windowIndex")
                    setMediaId("id $windowIndex")
                    setMediaUri(Uri.parse("http://uri/$windowIndex"))
                    setTitle("title: $windowIndex")
                    setSubtitle("subTitle: $windowIndex")
                    build()
                }
            }
        })
    }

    fun activateMediaSession() {
        // Note: do not pass a null to the 3rd param below, it will cause a NPE
        mMediaSessionConnector.setPlayer(mPlayerHolder.mPlayer, null)
        mMediaSession.isActive = true
    }

    fun deactivateMediaSession() {
        mMediaSessionConnector.setPlayer(null, null)
        mMediaSession.isActive = false
    }

    fun releaseMediaSession() {
        mMediaSession.release()
    }

    // ExoPlayer related functions
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

    // Picture in Picture related functions
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
