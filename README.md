# ExoPlayer2 exploration

This project is an exploration of ExoPlayer2 for audio and video playback.

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

Finally, you have to attach the player to a `SimpleExoPlayerView`, which renders the video 
to your UI, and also provides controls for audio / video playback.

```kotlin
enum class Source {
    local_audio, local_video, http_audio, http_video, playlist;
}

data class PlayerState(var window: Int = 0,
                       var position: Long = 0,
                       var whenReady: Boolean = true,
                       var source: Source = local_audio)
                       
class PlayerHolder : AnkoLogger {
    val ctx: Context
    val playerView: SimpleExoPlayerView
    val state: PlayerState
    val player: SimpleExoPlayer
                   
    constructor(c: Context, pv: PlayerView, s: PlayerState){
        ctx = c
        playerView = pv
        state = s

        // Create the player
        player = ExoPlayerFactory.newSimpleInstance(ctx, DefaultTrackSelector())
                .apply {
                    // Bind to the view
                    playerView.player = this
                    // Load media
                    prepare(buildMediaSource(state.source))
                    // Restore state
                    with(state) {
                        // Start playback when media has buffered enough (whenReady is true by default)
                        playWhenReady = whenReady
                        seekTo(window, position)
                    }
                    // Add logging (note, player hasn't been initialized yet, so passing this)
                    attachLogging(this)
                    info { "SimpleExoPlayer created" }
                }    
    }

    fun selectMediaToPlay(source: Source): Uri {
        return when (source) {
            Source.local_audio -> Uri.parse("asset:///audio/cielo.mp3")
            Source.local_video -> Uri.parse("asset:///video/stock_footage_video.mp4")
            Source.http_audio -> Uri.parse("http://storage.../play.mp3")
            Source.http_video -> Uri.parse("http://downloa.../BigBuckBunny_320x180.mp4")
        }
    }

    fun buildMediaSource(uri: Uri): ExtractorMediaSource {
        return ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(ctx, "exoplayer-learning")).createMediaSource(uri)
    }

    fun release() { ... }

}
```

When you're done with playback, be sure to release the player, since it consumes resources 
like network, memory and system codecs. Codecs are a globally shared resource on the 
phone, and there might be a limited number of them available depending on the specific 
phone and OS version, so it's important to release them when not using them.

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
        info { "SimpleExoPlayer is released" }
    }
```

You have to integrate with the Android Activity lifecycle in order to release and create the 
player. Here's a simple example of what this can look like.

```kotlin
class VideoActivity : AppCompatActivity(), AnkoLogger {

    lateinit var playerHolder: PlayerHolder
    val state = PlayerState()

    override fun onCreate(savedInstanceState: Bundle?) { ... }

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

}
```

## Slightly more control over player creation

You can also use a different signature of `ExoPlayerFactor.newSimpleInstance(...)` factory method 
to create your player, that accepts a few more parameters. You can also pass a 
`DefaultRenderersFactory()` and a `DefaultLoadControl()` as arguments. For example, by passing some 
arguments to the DefaultLoadControl class you can change the buffering policy of ExoPlayer2 to 
suit your needs. 

```kotlin
// Create player
player = ExoPlayerFactory.newSimpleInstance(
    // Renders audio, video, text (subtitles) content
    DefaultRenderersFactory(ctx),
    // Choose best audio, video, text track from available sources, 
    // based on bandwidth, device capabilities, language, etc
    DefaultTrackSelector(),
    // Manage buffering and loading data over the network
    DefaultLoadControl()
).apply { ... }
```

For more complex use cases, you can provide your own implementations of all the arguments that are
passed to the `ExoPlayerFactory.newSimpleInstance(...)` factory method, which gives you a great
deal of flexibility in what you can do with ExoPlayer2.

## Creating playlists

Instead of providing the player with a single `ExtractorMediaSource` (as shown in the 
`buildMediaSource(Uri)` method above), you can simply create a `ConcatenatingMediaSource` 
which can have any number of `MediaSource` objects contained in it. Here's an example.

```kotlin
val mMediaMap = mapOf<Source, Uri>(
        local_audio to Uri.parse("asset:///audio/cielo.mp3"),
        local_video to Uri.parse("asset:///video/stock_footage_video.mp4"),
        http_audio to Uri.parse("http://storag...play.mp3"),
        http_video to Uri.parse("http://downlo...BigBuckBunny_320x180.mp4")
    )

fun buildMediaSource(source: Source): MediaSource {
        return when (source) {
            playlist -> {
                return ConcatenatingMediaSource(
                        createExtractorMediaSource(local_audio),
                        createExtractorMediaSource(local_video),
                        createExtractorMediaSource(http_audio),
                        createExtractorMediaSource(http_video)
                )
            }
            else -> { return createExtractorMediaSource(source) }
        }
    }

    private fun createExtractorMediaSource(source: Source): MediaSource {
        return ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(mContext, "exoplayer-learning"))
                .createMediaSource(mMediaMap.get(source))
    }
```

`ConcatenatingMediaSource` creates a static playlist. If you want a dynamic playlist then you
can use `DynamicContactenatingMediaSource`. Both of them will combine media sources seamlessly
and handle buffering for the entire playlist. Here is a [medium article](https://medium.com/google-exoplayer/exoplayer-2-x-mediasource-composition-6c285fcbca1f) 
on the details of `MediaSource` composition.

## Saving player state between onPause() and onResume()

The `PlayerState` data class is used to load the player's state information before it's been run 
the first time. When the player is released, some of the player's state is saved to an object of 
this class. When a new player is created, this simple state object is used to restore a 
`boolean`, `Int`, `Long`, and an enum value (`Source`) which are used by the player to set itself 
back up (where the previous player instance had left off). 

From a UX standpoint, this means that when you run the app, play some media, and hit 
the home button, the player is actually released (destroyed). When you switch back 
to that app, the player is initialized again, and the previous state information should
be restored, so that the user can resume playback where they left off (position of previous
playback if any, and the item in the playlist that they were consuming, which is a window
index).

Before the player is released, the player's `currentWindowIndex`, `currentPosition`, `playWhenReady`, 
and playlist or media item information is saved to the `PlayerState` object, and it's restored once 
the player is created again.

## Loading files locally from APK
[`DefaultDataSource`](https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/upstream/DefaultDataSource.html)
allows local files to be loaded via the following URIs:
- `file:///`
- `asset:///`
- `content:///`
- `rtmp`
- `data`
- `http(s)`

You can load files from `assets` in the following ways (you can have nested 
under `assets` folders):
- `val uri = Uri.parse("file:///android_asset/video/stock_footage_video.mp4")`
- `val uri = Uri.parse("asset:///video/stock_footage_video.mp4")`

Note that ExoPlayer doesn't allow loading files from 
`Uri.parse("android.resource://${packageName}/${R.raw.id})"`. 
Also, Android doesn't allow you to add folders in the `res` folder, which is allowed in 
the `assets` folder.

# Resources

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