package app;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class View extends Stage{
	
	private static View instance;
	
	public static final int height = 128, width = 300;
	
	public static BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
	public static int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	
	public static BufferedImage sideStrip = new BufferedImage(10, height,BufferedImage.TYPE_INT_RGB);
	public static int[] sidePixels = ((DataBufferInt)sideStrip.getRaster().getDataBuffer()).getData();
	
	public static int colorMap[] = new int[height];
	
	//int colors[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xf442dc, 0xFF0000, 0xffe900, 0x00e1ff, 0x2600ff, 0};
	int colorsMic[] = {0xFFFFFF, 0xf442dc, 0xFF0000, 0xffe900, 0xffe900, 0x00e1ff, 0x2600ff, 0};
	//int colors[] = {0xFFFFFF, 0xFFFFFF, 0};
	//int colors[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,  0x008ae6, 0x001f33, 0x001f33, 0x001f33};
	int colorsPlayer[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF,  0x1aa3ff, 0x001f33, 0x001f33, 0x001f33};
	int colors[];
	//int colors[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFF0000, 0xffe900, 0x00e1ff, 0x2600ff, 0};
	
	//int colors[] = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xffe900, 0xffe900, 0x00e1ff, 0x2600ff, 0};
	
	public static double frequencyBins[];
	
	public static double magnitudes[] = new double[height];
	
	public static Recorder recorder;
	
	public static ImageView iv;
	
	public static String song = "Vivaldi.wav";
	public static String path = "audioFiles\\";
	
	public static View get(){
		if (instance == null)
			new View();
		return instance;
	}
	
	private View(){
		instance = this;
		initWindow();
		calculateBins();
		if (Main.mode.equals("player")) 
			startPlaying();
		else
			startRecording();
	}
	
	private void startRecording(){
		recorder = new Recorder();
		
		Thread recordThread = new Thread(new Runnable() {
			
			public void run() {
				System.out.println("started recording...");
				recorder.start();
			}
			
		});
		recordThread.start();
	}
	
	private void startPlaying(){
		Player player = new Player();
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				player.start();
			}
			
		});
		
		thread.start();
	}
	
	public static void drawSpectrum2(float samples[]){
		Complex data[] = new Complex[samples.length];
		for (int i = 0; i < samples.length; i++){
			data[i] = new Complex(samples[i], 0);
		}
		Complex niz[] = FFT.fft(data);
		
		int k = 0;
		
		double max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
		for (int i = 0; i < magnitudes.length; i++){
			
			int startIndex = (int)frequencyBins[k];
			int endIndex = (int)frequencyBins[k+1];
			
			double maxAmp = Integer.MIN_VALUE;
			
			for (int j = startIndex; j < endIndex; j++){
				double amp = Math.sqrt(niz[j].re() * niz[j].re() + niz[j].im() * niz[j].im());
				if (amp > maxAmp) maxAmp = amp;
			}
			if (maxAmp == 0) magnitudes[i] = 0;
			else magnitudes[i] = 20 * Math.log10(maxAmp);
			
			if (magnitudes[i] > max) max = magnitudes[i];
			if (magnitudes[i] < min) min = magnitudes[i]; 
			
			k++;
			
		}
		
		if (min < 0){
			min = Math.abs(min);
			for (int i = 0; i < magnitudes.length; i++){
				magnitudes[i] += min;
			}
		}
		
		double scale = 127/100;
		shiftImageLeft(1);
		for (int i = magnitudes.length - 1; i >= 0; i--){
			int x = 127 - Math.abs((int)(magnitudes[127-i] * scale));
			pixels[width * i + width - 1] = colorMap[x];
		}
		iv.setImage(SwingFXUtils.toFXImage(image, null));
	}
	
	private static void shiftImageLeft(int pixelAmount){
		for (int i = 0; i < height; i++){
			for (int j = pixelAmount; j < width; j++){
				pixels[i * width + j - pixelAmount] = pixels[i * width + j];
			}
		}
	}
	
	private void calculateBins(){
		double maxFreq = 22050;
		double time = (Player.DEF_BUFFER_SAMPLE_SZ/2)/maxFreq;
		double minFreq = 1/time;
		
		frequencyBins = new double[height + 1];
		frequencyBins[0] = minFreq;
		frequencyBins[frequencyBins.length-1] = maxFreq;
		
		minFreq = melTransform(minFreq);
		maxFreq = melTransform(maxFreq);
		
		double amount = (maxFreq - minFreq)/height;
		
		for (int i = 1; i < frequencyBins.length-1; i++){
			frequencyBins[i] = iMelTransform(minFreq + i * amount);
		}
		
		frequencyBins[0] = 0;
		int index = 1;
		for (int i = 2; i < Player.DEF_BUFFER_SAMPLE_SZ/2; i++){
			double freq = i / time;
			if (freq >= frequencyBins[index]){
				frequencyBins[index++] = i-1;
			}
			if (index==(height+1)) break;
		}
		frequencyBins[frequencyBins.length-1] = Player.DEF_BUFFER_SAMPLE_SZ/2-1;
	}
	
	private void initWindow(){
		BorderPane bp = new BorderPane();
		iv = new ImageView(SwingFXUtils.toFXImage(image, null));
		//iv.setPreserveRatio(true);
		//iv.setFitWidth(width * 2);
		//iv.setFitHeight(height * 2);
		bp.setCenter(iv);
		
		if (Main.mode.equals("player"))
			colors = colorsPlayer;
		else
			colors = colorsMic;
		
		makeColorMap();
		
		ImageView pm = new ImageView(SwingFXUtils.toFXImage(sideStrip, null));
		//pm.setPreserveRatio(true);
		//pm.setFitWidth(10 * 2);
		//pm.setFitHeight(height * 2);
		bp.setRight(pm);
		
		this.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				Player.running = false;
				Recorder.running = false;
			}
		});
		
		Scene scene = new Scene(bp);
		this.setScene(scene);
		if (Main.mode.equals("player"))
			this.setTitle("Now playing: " + song);
		else
			this.setTitle("Recording...");
		this.show();
	}
	
	void makeColorMap(){
		int amount = height / (colors.length-1) + 1;
		int counter = 0;
		
		int color1 = 0, color2 = 0;
		for (int i = 0; i < height; i++){
			if (i % amount == 0 && counter < colors.length-1){
				color1 = colors[counter];
				color2 = colors[counter + 1];
				counter++;
			}

			double x = (i % amount)/(double)amount;
			int color = preLerp(color1, color2, x);
			
			for (int j = 0; j < sideStrip.getWidth(); j++){
				sidePixels[i*sideStrip.getWidth() + j] = color;
			}
			colorMap[i] = color;
		}
	}
	
	public static int preLerp(int color1, int color2, double x){
		int r = lerp(((color1 & 0xFF0000) >> 16), ((color2 & 0xFF0000) >> 16), x);
		int g = lerp(((color1 & 0x00FF00) >> 8), ((color2 & 0x00FF00) >> 8), x);
		int b = lerp((color1 & 0x0000FF), (color2 & 0x0000FF), x);
		return (r << 16) | (g << 8) | b;
	}
	
	public static int lerp(int a, int b, double x){
		return (int)(a + (b - a) * x);
	}
	
	private double melTransform(double freq){
		return 1125 * Math.log(1 + freq/(float)700);
	}
	
	private double iMelTransform(double freq){
		return 700 * (Math.pow(Math.E, freq/(float)1125) - 1);
	}

}
