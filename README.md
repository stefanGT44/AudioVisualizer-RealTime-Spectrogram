# AudioVisualizer-RealTime-Spectrogram
This is a small JavaFX desktop application for real-time audio visualization (spectrogram) built from scratch.

## Overview
The application supports visualization and playback of .wav files or direct microphone input.
<br>The audio is visually represented with a spectrogram - an image where the X coordinate of a pixel represents time, the Y coordinate represents a corresponding frequency and the pixel color is the amplitude of that frequency.

![Alt text](images/player.png?raw=true "Playing Vivaldi")
![Alt text](images/aphex.png?raw=true "Aphex Twin Equation")<br>
![Alt text](images/mic.png?raw=true "Speech")
![Alt text](images/whistle.png?raw=true "Whistling")

## Implementation details
### 1. Creating the color map
Before audio processing begins, a color map must be created. The map is created from an array of starting colors using linear interpolation, the result array (map) contains gradients between every adjecent starting color. <br>The map is used to choose the appropriate pixel color based on the amplitude of the frequency represented by that pixel. The higher the color on the map the higher the amplitude (the map is vertical).

### 2. Calculating the Mel filter bank
The visualizer mimics the human auditory system, which is logarithmic in its nature. This means that as the frequencies get higher we can detect fewer changes in sound. Because of this the Mel scale is used for selecting which frequencies are shown in the visualizer, and for calculating their amplitudes. The Mel filter bank is an array of logarithmically spaced frequencies, the filters are triangular and overlapping which means the first filter starts at index 0, has a center at index 1 and ends at index 2, the second filter starts at index 1, has a center at 2 and ends at 3 etc. Since the spectrogram image height is 128 pixels, 128 filters are needed -> there are 130 frequencies in the filter bank array.

![Alt text](images/melbank.png?raw=true "10 filter example")

### 3. Reading raw audio data slices (from .wav file or microphone) and (if .wav) writing to output (speakers) - playing audio
After creating the color map and Mel filter bank, audio processing can begin.

### 4. Unpacking raw data into samples and applying the Hamming window function
Because we are taking fixed slices of audio, a window function is used to smooth out the transition between two slices. This helps emphasize the key characteristics of each time slice.

### 5. Processing samples and drawing the Spectrogram
FFT (Fast Fourier transform) decomposes the sequence of samples (sound wave) into components of different frequencies (base harmonics - elementary sound waves). Every sample sequence (time slice) represents a new column of pixels in the picture. Using the Mel filter bank we know which components to use for computing the amplitude of each frequency band (each pixel - Y coordinate) in the current time slice (in the newest column - X). Based on the computed amplitudes, the corresponding color is assigned to each pixel using the color map. The final step is shifting all pixels of the image to the left by 1 pixel and inserting the new column.

## Sidenote
This was a small side project that I had done in my spare time at the start of the 5th semester, influenced by studying audio processing for the course - Speech recognition at the Faculty of Computer Science in Belgrade.

## Download
You can download the .jar files [here](downloads/AudioVisualizerSpectrogram.zip).<br>
To run the AudioVisualizerSpectrogramPlayer.jar it must be within the same folder as the audioFiles folder.

## Contributors
- Stefan Ginic - <stefangwars@gmail.com>
