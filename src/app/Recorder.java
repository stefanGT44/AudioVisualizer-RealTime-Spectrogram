package app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import javafx.application.Platform;

public class Recorder {

	public static final int BUFFER_SIZE = 1024;
	private TargetDataLine audioLine;
	private AudioFormat format;

	public static volatile boolean running;

	AudioFormat getAudioFormat() {
		float sampleRate = 44100;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	public void start(){
		try {
		format = getAudioFormat();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		if (!AudioSystem.isLineSupported(info)) {
			throw new LineUnavailableException("The system does not support the specified format.");
		}

		audioLine = AudioSystem.getTargetDataLine(format);
		audioLine.open(format);
		audioLine.start();

		running = true;
			
		final int normalBytes = Player.normalBytesFromBits(format.getSampleSizeInBits());
		
		float[] samples = new float[BUFFER_SIZE * format.getChannels()];
		long[] transfer = new long[samples.length];
		byte[] bytes = new byte[samples.length * normalBytes];
		
		int bread = BUFFER_SIZE * format.getChannels();
		
		while (running) {
			audioLine.read(bytes, 0, bytes.length);
			samples = Player.unpack(bytes, transfer, samples, bread, format);
			samples = Player.hamming(samples, bread, format);
			View.drawSpectrum2(samples);
		}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			audioLine.flush();
			audioLine.drain();
			audioLine.close();
			System.out.println("STOPPED");
		}
	}

	public void stop() throws IOException {
		running = false;
	}

}
