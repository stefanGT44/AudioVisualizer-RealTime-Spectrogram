# RealTime-AudioVisualizer-Spectrogram
This is a small JavaFX desktop application for real-time audio visualization (spectrogram) built from scratch.

## Overview
The application supports visualization and playback of .wav files, or it can directly visualize microphone input.
<br>The audio is visually represented with a spectrogram - a picture where the X coordinate of a pixel represents time, the Y coordinate represents a corresponding frequency and the pixel color is the amplitude of that frequency.

![Alt text](images/player.png?raw=true "Playing Vivaldi")
![Alt text](images/aphex.png?raw=true "Aphex Twin Equation")
![Alt text](images/mic.png?raw=true "Random talk")
![Alt text](images/whistle.png?raw=true "Whistling")

## Implementation details
### 1. Creating the color map
Before audio processing begins, a color map must be created. The map is calculated from an array of starting colors using the linear interpolation function, the result array (map) has the starting colors and gradients between every two neighbouring colors. <br>The map is used to choose the appropriate pixel color based on the amplitude of the frequency represented by that pixel. The higher the color on the map the higher the amplitude (the map is vertical).

### 2. Calculating the Mel filter bank
The visualizer mimics the human auditory system which is logarithmic. This means that as the frequencies get higher we can detect fewer changes in sound. That is why the Mel scale is used to select which frequencies are shown in the visualizer and how their amplitudes are calculated. The Mel filter bank is an array of logarithmically spaced frequencies, the filters are triangular which means the first filter starts at index 0, has a center at index 1 and ends at index 2, the second filter starts at index 1 has a center at 2 and ends at 3 etc. Because the spectrogram image height is 128 pixels and we want to show 1 frequency per pixel, we need 128 filters -> the filter bank is 130 long (array of frequencies).

![Alt text](images/melbank.png?raw=true "")

### 3. Reading raw audio data slices (from .wav file or microphone) and (if .wav) writing to output (speakers) - playing audio
After creating the color map and Mel filter bank, audio processing can begin.

### 4. Unpacking raw data into samples and applying the Hamming window function
Because we are taking fixed slices of audio we use a window function to smooth out the transition between two slices. This helps emphasize the key characteristics of each time slice.

### 5. Processing samples and drawing the Spectrogram
FFT (Fast Fourier transform) decomposes the sequence of samples (sound wave) into components of different frequencies (base harmonics - elementary sound waves). Every sample sequence (time slice) represents a new column in the picture. Using the Mel filter bank we know which components to use for computing the amplitude of each frequency band (each pixel - Y coordinate) in the current time slice (in the newest column - X). After computing the amplitude of each frequency band, the corresponding color is assigned to each pixel using the color map. The final step is shifting all the pixels of the image to the left by 1 pixel and inserting the new column.

## Sidenote
This was a fun little project I had done in my spare time at the start of the 5th semester (start of the 3rd year of college), influenced by studying audio processing for the course - Speech recognition at the Faculty of Computer Science in Belgrade.

## Download
You can download the .jar files [here](downloads).<br>
To run the AudioVisualizerSpectrogramPlayer.jar it must be within the same folder as the audioFiles folder.

## Contributors
- Stefan Ginic - <stefangwars@gmail.com>
