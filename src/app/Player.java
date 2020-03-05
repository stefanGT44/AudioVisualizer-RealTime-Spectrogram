package app;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class Player {
	
	public static final int DEF_BUFFER_SAMPLE_SZ = 1024;
	
	public static volatile boolean running = false;

	public void start(){
		running = true;
		AudioInputStream in = null;
		SourceDataLine out = null;
		try {
			
			File audioFile = new File(View.path + View.song);
			final AudioFormat audioFormat = (AudioSystem.getAudioFileFormat(audioFile).getFormat());
			
			in = AudioSystem.getAudioInputStream(audioFile);
			out = AudioSystem.getSourceDataLine(audioFormat);
			
			final int normalBytes = normalBytesFromBits(audioFormat.getSampleSizeInBits());
			
			float[] samples = new float[DEF_BUFFER_SAMPLE_SZ * audioFormat.getChannels()];
			long[] transfer = new long[samples.length];
			byte[] bytes = new byte[samples.length * normalBytes];
			
			out.open(audioFormat, bytes.length);
			out.start();
			
			for (int feed = 0; feed < 6; feed++) {
				out.write(bytes, 0, bytes.length);
			}

			int bread;
			
			while ((bread = in.read(bytes)) != -1 && running){

			samples = unpack(bytes, transfer, samples, bread, audioFormat);
			//samples = window(samples, bread / normalBytes, audioFormat);
			//samples = hanning(samples, bread / normalBytes, audioFormat);
			samples = hamming(samples, bread / normalBytes, audioFormat);
			
			View.drawSpectrum2(samples);

			out.write(bytes, 0, bread);
			}
			running = false;
			
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			running = false;
			if (in != null){
				try {
				in.close();
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			if (out != null){
				out.flush();
				out.close();
			}
		}
		return;
	}
	
	public static int normalBytesFromBits(int bitsPerSample) {
		return bitsPerSample + 7 >> 3;
	}
	
	public static float[] unpack(byte[] bytes, long[] transfer, float[] samples, int bvalid, AudioFormat fmt) {
		if (fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
				&& fmt.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {

			return samples;
		}

		final int bitsPerSample = fmt.getSampleSizeInBits();
		final int bytesPerSample = bitsPerSample / 8;
		final int normalBytes = normalBytesFromBits(bitsPerSample);

		/*
		 * not the most DRY way to do this but it's a bit more efficient.
		 * otherwise there would either have to be 4 separate methods for each
		 * combination of endianness/signedness or do it all in one loop and
		 * check the format for each sample.
		 * 
		 * a helper array (transfer) allows the logic to be split up but without
		 * being too repetetive.
		 * 
		 * here there are two loops converting bytes to raw long samples.
		 * integral primitives in Java get sign extended when they are promoted
		 * to a larger type so the & 0xffL mask keeps them intact.
		 * 
		 */

		if (fmt.isBigEndian()) {
			for (int i = 0, k = 0, b; i < bvalid; i += normalBytes, k++) {
				transfer[k] = 0L;

				int least = i + normalBytes - 1;
				for (b = 0; b < normalBytes; b++) {
					transfer[k] |= (bytes[least - b] & 0xffL) << (8 * b);
				}
			}
		} else {
			for (int i = 0, k = 0, b; i < bvalid; i += normalBytes, k++) {
				transfer[k] = 0L;

				for (b = 0; b < normalBytes; b++) {
					transfer[k] |= (bytes[i + b] & 0xffL) << (8 * b);
				}
			}
		}

		final long fullScale = (long) Math.pow(2.0, bitsPerSample - 1);

		/*
		 * the OR is not quite enough to convert, the signage needs to be
		 * corrected.
		 * 
		 */

		if (fmt.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {

			/*
			 * if the samples were signed, they must be extended to the 64-bit
			 * long.
			 * 
			 * the arithmetic right shift in Java will fill the left bits with
			 * 1's if the MSB is set.
			 * 
			 * so sign extend by first shifting left so that if the sample is
			 * supposed to be negative, it will shift the sign bit in to the
			 * 64-bit MSB then shift back and fill with 1's.
			 * 
			 * as an example, imagining these were 4-bit samples originally and
			 * the destination is 8-bit, if we have a hypothetical sample -5
			 * that ought to be negative, the left shift looks like this:
			 * 
			 * 00001011 << (8 - 4) =========== 10110000
			 * 
			 * (except the destination is 64-bit and the original bit depth from
			 * the file could be anything.)
			 * 
			 * and the right shift now fills with 1's:
			 * 
			 * 10110000 >> (8 - 4) =========== 11111011
			 * 
			 */

			final long signShift = 64L - bitsPerSample;

			for (int i = 0; i < transfer.length; i++) {
				transfer[i] = ((transfer[i] << signShift) >> signShift);
			}
		} else {

			/*
			 * unsigned samples are easier since they will be read correctly in
			 * to the long.
			 * 
			 * so just sign them: subtract 2^(bits - 1) so the center is 0.
			 * 
			 */

			for (int i = 0; i < transfer.length; i++) {
				transfer[i] -= fullScale;
			}
		}

		/* finally normalize to range of -1.0f to 1.0f */

		for (int i = 0; i < transfer.length; i++) {
			samples[i] = (float) transfer[i] / (float) fullScale;
		}

		return samples;
	}
	
	public static float[] window(float[] samples, int svalid, AudioFormat fmt) {
		/*
		 * most basic window function multiply the window against a sine curve,
		 * tapers ends
		 * 
		 * nested loops here show a paradigm for processing multi-channel
		 * formats the interleaved samples can be processed "in place" inner
		 * loop processes individual channels using an offset
		 * 
		 */

		int channels = fmt.getChannels();
		int slen = svalid / channels;

		for (int ch = 0, k, i; ch < channels; ch++) {
			for (i = ch, k = 0; i < svalid; i += channels) {
				samples[i] *= Math.sin(Math.PI * k++ / (slen - 1));
			}
		}

		return samples;
	}
	
	public static float[] hanning(float[] samples, int svalid, AudioFormat fmt) {

		int channels = fmt.getChannels();
		int slen = svalid / channels;

		for (int ch = 0, k, i; ch < channels; ch++) {
			for (i = ch, k = 0; i < svalid; i += channels) {
				samples[i] = (float)(samples[i] * 0.5 * (1 - Math.cos(2.0*Math.PI * k++ / samples.length)));
			}
		}

		return samples;
	}
	
	public static float[] hamming(float[] samples, int svalid, AudioFormat fmt) {

		int channels = fmt.getChannels();
		int slen = svalid / channels;

		for (int ch = 0, k, i; ch < channels; ch++) {
			for (i = ch, k = 0; i < svalid; i += channels) {
				samples[i] = (float)(samples[i] * (0.54 - 0.46 * Math.cos(2.0*Math.PI * k++ / samples.length)));
			}
		}

		return samples;
	}
	
	
}
