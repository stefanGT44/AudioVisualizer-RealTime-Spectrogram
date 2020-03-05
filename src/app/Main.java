package app;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application{
	
	public static String mode = "";
	
	public static void main(String[] args) {
		if (args.length != 1 || (!args[0].equals("mic") && !args[0].equals("player"))) {
			System.out.println("Error - invalid arguments. Please pass argument: mic or player");
			return;
		}
		mode = args[0];
		launch(args);
	}

	@Override
	public void start(Stage arg0) throws Exception {
		View.get();
	}

}
