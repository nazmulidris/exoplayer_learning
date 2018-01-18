# ExoPlayer2 exploration

This project is an exploration of ExoPlayer2 for audio and video playback.

## Loading files locally from APK
[DefaultDataSource](https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/upstream/DefaultDataSource.html) 
allows local files to be loaded via the following URIs:
- file:///
- asset:///
- content:///
- rtmp
- data
- http(s)

Note that loading files from `Uri.parse("android.resource://${packageName}/${R.raw.id})"` 
is not allowed.

## Audio, Video Playback
- [ ] [Caster.io course on EP](https://goo.gl/EeuZi1)
- [ ] [EP2 for audio, video playback sample](https://goo.gl/1d4bkY)
- [ ] [EP2 play audio + video](https://goo.gl/eVbEoD)
- [x] [EP2 Overview](https://goo.gl/ZynVzk)
- [x] [Why EP2? from Google](https://goo.gl/tny1Rz)

## DASH, HLS
- [x] [Dash, HLS](https://goo.gl/r9fXXf)
- [x] [Dash benefits over HLS, etc](https://goo.gl/SNvMgQ)

## PIP, AR
- [x] [PIP, AR](https://goo.gl/1GoECE)
