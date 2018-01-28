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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import kotlinx.android.synthetic.main.activity_video.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn


class VideoActivity : AppCompatActivity(), AnkoLogger {

    lateinit var playerHolder: PlayerHolder
    val state = PlayerState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        SpinnerHandler(this, exoplayerview_activity_spinner, state)
    }

    override fun onResume() {
        super.onResume()
        initPlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    fun initPlayer() {
        playerHolder = PlayerHolder(this, exoplayerview_activity_video, state)
    }

    fun releasePlayer() {
        playerHolder.release()
    }

    fun applyNewState(newSource: Source, newPosition: Long, newWindow: Int) {
        releasePlayer()
        with(state) {
            source = newSource // tell the player to play new media
            position = newPosition // reset the playback position
            window = newWindow // reset the playback position
        }
        initPlayer()
    }
}

class SpinnerHandler(ctx: VideoActivity,
                     spinner: Spinner,
                     state: PlayerState) : AnkoLogger {
    init {
        // Setup the adapter
        with(ArrayAdapter<CharSequence>(ctx, android.R.layout.simple_spinner_item)) {
            Source.values().forEach { add(it.toString()) }
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = this
        }

        // Attach the selection listener.
        // Note: When the spinner launches it will fire a selection event immediately,
        // and this method will be called right away (which is why there's a check to see if
        // the state.source has actually changed or not, before releasing and initializing
        // a new player.
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val itemAsString = parent?.getItemAtPosition(pos).toString()
                val itemAsSource = Source.valueOf(itemAsString)
                if (itemAsSource != state.source) {
                    ctx.applyNewState(itemAsSource, 0, 0)
                    warn {
                        "onItemSelected: state.source changed to ${state.source}. Reloading player"
                    }
                } else {
                    warn {
                        "onItemSelected: state.source(${state.source}) hasn't changed. Ignoring selection"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }
}

