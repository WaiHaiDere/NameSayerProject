package namesayer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;


public class HelpMenu {
	@FXML
	private WebView instructionsWebView;
	@FXML
	private Button mainMenuBtn;


	// Loads the webview with html file
	@FXML
	private void initialize() {

		WebEngine engine = instructionsWebView.getEngine();
		try {
            String s = getClass().getResource("/namesayer/instructions.html").toURI().toString();
            engine.load(s);
        } catch (URISyntaxException e){
        }
	}

	
	// Goes back to main menu
	public void mainMenuBtnClicked(ActionEvent actionEvent) {
		mainMenuBtn.getScene().setRoot(MainMenu.getMainMenuRoot());
	}

}
