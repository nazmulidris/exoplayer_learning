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
is not allowed.

## Audio, Video Playback
- [ ] [IO17 ExoPlayer2 Session](https://www.youtube.com/watch?v=jAZn-J1I8Eg)
- [ ] [Caster.io course on ExoPlayer2](https://goo.gl/EeuZi1)
- [ ] [ExoPlayer2 for audio, video playback sample](https://goo.gl/1d4bkY)
- [ ] [ExoPlayer2 play audio + video](https://goo.gl/eVbEoD)
- [x] [ExoPlayer2 Overview](https://goo.gl/ZynVzk)
- [x] [Why ExoPlayer2? from Google](https://goo.gl/tny1Rz)

## MediaSession 
- [ ] [MediaSession extension for ExoPlayer2](https://medium.com/google-exoplayer/the-mediasession-extension-for-exoplayer-82b9619deb2d)

## Codelabs
- [ ] [IO17 ExoPlayer2 codelab](https://codelabs.developers.google.com/codelabs/exoplayer-intro/#0)

## Changelog for ExoPlayer2
- [ ] [Latest changes for 2.6.1](https://medium.com/google-exoplayer/exoplayer-2-6-1-whats-new-a9e54bffffc5)

## DASH, HLS
- [x] [Dash, HLS](https://goo.gl/r9fXXf)
- [x] [Dash benefits over HLS, etc](https://goo.gl/SNvMgQ)

## PIP, AR
- [x] [PIP, AR](https://goo.gl/1GoECE)
