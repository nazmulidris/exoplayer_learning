# ExoPlayer2 exploration

This project is an exploration of ExoPlayer2 for audio and video playback.

## Loading files locally from APK
[DefaultDataSource](https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/upstream/DefaultDataSource.html) 
allows local files to be loaded via the following URIs:
- `file:///`
- `asset:///`
- `content:///`
- `rtmp`
- `data`
- `http(s)`

Note that loading files from `Uri.parse("android.resource://${packageName}/${R.raw.id})"` 
is not allowed. Also note that you can't add folders in the `res` folder.

Note that you can load files from `assets` in the following ways (you can have nested 
under `assets` folders):
- `val uri = Uri.parse("file:///android_asset/video/stock_footage_video.mp4")`
- `val uri = Uri.parse("asset:///video/stock_footage_video.mp4")`

## Audio, Video Playback
- [ ] [IO14 ExoPlayer Introduction Video](https://www.youtube.com/watch?v=6VjF638VObA)
- [ ] [IO17 ExoPlayer2 Session Video](https://www.youtube.com/watch?v=jAZn-J1I8Eg)
- [ ] [Caster.io course on ExoPlayer2](https://goo.gl/EeuZi1)
- [ ] [ExoPlayer2 for audio, video playback sample](https://goo.gl/1d4bkY)
- [ ] [ExoPlayer2 play audio + video](https://goo.gl/eVbEoD)
- [x] [ExoPlayer2 Overview](https://goo.gl/ZynVzk)
- [x] [Why ExoPlayer2? from Google](https://goo.gl/tny1Rz)

## MediaSession 
- [ ] [MediaSession extension for ExoPlayer2](https://medium.com/google-exoplayer/the-mediasession-extension-for-exoplayer-82b9619deb2d)

## Codelabs
- [ ] [IO17 ExoPlayer2 codelab](https://codelabs.developers.google.com/codelabs/exoplayer-intro/#0)

## Tutorials
- [ ] [Using ExoPlayer and Kotlin to build simple audio app](https://medium.com/mindorks/implementing-exoplayer-for-beginners-in-kotlin-c534706bce4b)

## Changelog for ExoPlayer2
- [ ] [Latest changes for 2.6.1](https://medium.com/google-exoplayer/exoplayer-2-6-1-whats-new-a9e54bffffc5)

## DASH, HLS
- [x] [Dash, HLS](https://goo.gl/r9fXXf)
- [x] [Dash benefits over HLS, etc](https://goo.gl/SNvMgQ)

## PIP, AR
- [x] [PIP, AR](https://goo.gl/1GoECE)

# Notes on implementation

## Quick start for player creation

At a minimum, in order to create a `SimpleExoPlayer` you will need to provide a track selector 
(which chooses which track of audio, video, or text to load from your media source, based on 
bandwidth, devices, capabilities, language, etc). 

You will need to create a MediaSource, which tells player where to load media from. 
Sources of media can be the asset folder in your APK, or over HTTP, for regular media 
files (mp3, mp4, webm, mkv, etc). You cna use the `ExtractorMediaSource` to handle these 
sources and formats. For adaptive formats, you can use `DashMediaSource` (for DASH sources), 
`SsMediaSource` (for SmoothStreaming sources), and `HlsMediaSource` (for HLS sources).

You have to provide a `URI` that points to your media content, which is used by the 
`MediaSource` to actually load and prepare the content for playback.

You must also prepare the player, which tells it to start loading the data (and it might
have to buffer this data over the network). You also have to set a flag `playWhenReady`. 
true means play, and false means pause playback (after enough content has been buffered).

Finally, you have to attach the player to a `SimpleExoPlayerView` - displays audio / video 
playback and controls to the UI.

```kotlin
val player: SimpleExoPlayer =
            // Create the player
            ExoPlayerFactory.newSimpleInstance(ctx, DefaultTrackSelector())
                    .apply {
                        // Bind to the view
                        playerView.player = this
                        // Pick the media to play
                        val uri = selectMediaToPlay(state.source)
                        // Load media
                        prepare(buildMediaSource(uri))
                        // Start auto playback
                        playWhenReady = true
                        // Add logging (note, player hasn't been initialized yet, so passing this)
                        attachLogging(this)
                        // Restore state
                        with(state) {
                            playWhenReady = whenReady
                            seekTo(window, position)
                        }
                        warn { "SimpleExoPlayer created" }
                    }
                  
fun buildMediaSource(uri: Uri): ExtractorMediaSource {
        return ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(ctx, "exoplayer-learning")).createMediaSource(uri)
    }

fun selectMediaToPlay(source: Source): Uri {
        return when (source) {
            Source.local_audio -> Uri.parse("asset:///audio/cielo.mp3")
            Source.local_video -> Uri.parse("asset:///video/stock_footage_video.mp4")
            Source.http_audio -> Uri.parse("http://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
            Source.http_video -> Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4")
        }
    }
    
data class PlayerState(var window: Int = 0,
                       var position: Long = 0,
                       var whenReady: Boolean = true,
                       var source: Source = Source.local_audio)

enum class Source {
    local_audio, local_video, http_audio, http_video;
}
```

When you're done with playback, be sure to release the player, since it consumes a lot of resources
(memory and system codecs, which are a globally shared resource on your phone, and there might be
a limited number of them available on the phone).

```kotlin
fun release() {
        with(player) {
            // Save state
            with(state) {
                position = currentPosition
                window = currentWindowIndex
                whenReady = playWhenReady
            }
            // Release the player
            release()
        }
        warn { "SimpleExoPlayer is released" }
    }
```

## Slightly more control over player creation

You can also use a different signature of `ExoPlayerFactor.newSimpleInstance(...)` factory method 
to create your player, that accepts a few more parameters. You can also pass a 
`DefaultRenderersFactory()` and a `DefaultLoadControl()` as arguments. For example, by passing some 
arguments to the DefaultLoadControl class you can change the buffering policy of ExoPlayer2 to 
suit your needs. 

```kotlin
val player: SimpleExoPlayer =
            // Create player
            ExoPlayerFactory.newSimpleInstance(
                    // Renders audio, video, text (subtitles) content
                    DefaultRenderersFactory(ctx),
                    // Choose best audio, video, text track from available sources, based on bandwidth
                    // device capabilities, language, etc
                    DefaultTrackSelector(),
                    // Manage buffering and loading data over the network
                    DefaultLoadControl()
            ).apply {
                // Attach UI
                playerView.player = this

                // Init player state
                playWhenReady = state.playWhenReady
                seekTo(state.currentWindow, state.playbackPosition)

                // Load media
                val uri =
                        //Uri.parse(getString(R.string.media_url_mp3)) // audio
                        Uri.parse(ctx.getString(R.string.media_url_mp4)) // video
                prepare(buildMediaSource(uri))
            }
```

For more complex use cases, you can provide your own implementations of all the arguments that are
passed to the `ExoPlayerFactory.newSimpleInstance(...)` factory method, which gives you a great
deal of flexibility in what you can do with ExoPlayer2.