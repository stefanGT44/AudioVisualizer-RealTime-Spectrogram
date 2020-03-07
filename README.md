# RealTime-AudioVisualizer-Spectrogram
This is a small JavaFX desktop application for real-time audio visualization (spectrogram) built from scratch.

## Overview
The application supports visualization and playback of .wav files, or it can directly visualize microphone input.
<br>The audio is visually represented with a spectrogram - a picture where the X coordinate of a pixel represents time, the Y coordinate represents a corresponding frequency and the pixel color is the amplitude of that frequency.

## Implementation details
### 1. Creating the color map
Before audio processing begins, a color map must be created. The map is calculated from an array of starting colors using the linear interpolation function, the result array (map) has the starting colors and gradients between every two neighbouring colors. <br>The map is used to choose the appropriate pixel color based on the amplitude of the frequency represented by that pixel. The lower the color on the map the higher the amplitude (the map is vertical).

### 2. Calculating the Mel filter bank
The visualizer mimics the human auditory system which is logarithmic. This means that as the frequencies get higher we can detect fewer changes in sound (We can detect far more differences at lower frequencies). It is carefully calculated which frequencies are shown in the visualizer so that the sound is represented in the same way we hear it. After the frequencies are calculated a triangulation filter is applied to help capture the energy at each critical frequency band and give a rough approximation of the spectrum shape.

### 3. Reading raw audio data slices (from .wav file or microphone) and (if .wav) writing to output (speakers) - playing audio
After creating the color map and Mel filter bank, audio processing can begin.

## Sidenote
This was a fun little project I had done in my spare time at the start of the 5th semester (start of the 3rd year of collage), influenced by studying audio processing for the course - Speech recognition at the Faculty of Computer Science in Belgrade.

## Download
You can download the .jar files [here](downloads).<br>
To run the AudioVisualizerSpectrogramPlayer.jar it must be within the same folder as the audioFiles folder.

## Contributors
- Stefan Ginic - <stefangwars@gmail.com>
