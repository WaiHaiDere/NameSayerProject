package namesayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;


public class NameSelectionMenu implements Initializable {
	@FXML
	private Button mainMenuBtn;
	@FXML
	private ComboBox<String> inputMethodChoice;

	@FXML
	private AnchorPane inputPane;
	private AutoCompleteTextField nameInputField;

	@FXML
	private Button addNameBtn;
	@FXML
	private ListView<String[]> namesSelectedListView;
	@FXML
	private Button practiceButton;
	@FXML
	private Button deleteBtn;
	@FXML
	private Button deleteAllButton;
	@FXML
	private RadioButton shuffleButton;

	private static List<String> listOfNamesSelected = new ArrayList<String>();
	private static Parent nameSelectionMenuRoot;

	private List<String> listOfUserInput = new ArrayList<>();

	private static ObservableList<String[]> namesObsListManual = FXCollections.observableArrayList();
	private static ObservableList<String[]> namesObsListFile = FXCollections.observableArrayList();

	private static boolean selectedManual;
	private String[] selectedNameArray;
	private static boolean shuffleSelected;
	private static List<String> namesNotInDatabase = new ArrayList<>();


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> rateList = FXCollections.observableArrayList("Browse for text file", "Manual input");
		inputMethodChoice.setItems(rateList);
		inputMethodChoice.setValue("Browse for text file");

		nameInputField = new AutoCompleteTextField();
		// Handle keyboard buttons being pressed
		nameInputField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent k) {
				if (k.getCode().equals(KeyCode.ENTER)) {
					addNameBtnClicked();
				}
				if (k.getCode().equals(KeyCode.BACK_SPACE) && !nameInputField.getText().isEmpty()) {
					nameInputField.setText(nameInputField.getText().substring(0, nameInputField.getText().length()-1));
					nameInputField.positionCaret(nameInputField.getText().length());
				}
			}
			
		});
		nameInputField.getEntries().addAll(MainMenu.getListOfJustNames());
		nameInputField.setPromptText("Choose a text file");
		nameInputField.setDisable(true);
		nameInputField.setPrefWidth(292);
		inputPane.getChildren().add(nameInputField);

		namesSelectedListView.setItems(namesObsListFile);
		namesSelectedListView.setCellFactory(names -> new NameListCell());

		namesSelectedListView.setMouseTransparent(false);
		namesSelectedListView.setFocusTraversable(true);
	}


	public void mainMenuBtnClicked(ActionEvent actionEvent) {
		mainMenuBtn.getScene().setRoot(MainMenu.getMainMenuRoot());
	}


	public void addNameBtnClicked() {

		if (selectedManual) {
			if (((nameInputField.getText() == null) || nameInputField.getText().trim().equals(""))) {
				Alert noInputAlert = new Alert(Alert.AlertType.INFORMATION);
				noInputAlert.setTitle("ERROR - Please enter a name");
				noInputAlert.setHeaderText(null);
				noInputAlert.setContentText("No name entered. Please enter a name to practice");
				noInputAlert.showAndWait();
			} else {
				// Trim leading and trailing white space and hyphens, and replace multiple spaces/hyphens with single ones
				String userInput = nameInputField.getText().trim().replaceAll(" +", " ").replaceAll("\\s*-\\s*", "-").replaceAll("-+", "-").replaceAll("^-", "").replaceAll("-$", "");
				if (!userInput.isEmpty()) {
					String[] inputArray = NameChecker.nameAsArray(userInput);

					boolean isInList = false;
					for (String[] s : namesObsListManual) {
						if (Arrays.equals(s, inputArray)) {
							Alert alreadyExistsAlert = new Alert(Alert.AlertType.INFORMATION);
							alreadyExistsAlert.setTitle("ERROR - Name Already in List");
							alreadyExistsAlert.setHeaderText(null);
							alreadyExistsAlert.setContentText("The name has already been selected. Please enter another name to practice");
							alreadyExistsAlert.showAndWait();
							nameInputField.setText(null);
							isInList = true;
							break;
						}

					}
					if(!isInList) {
						namesObsListManual.add(inputArray);
						nameInputField.setText(null);
					}
				}
			}
		} else {

			listOfNamesSelected.clear();
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open txt file");
			File selectedFile = fileChooser.showOpenDialog(addNameBtn.getScene().getWindow());

			if ((selectedFile != null) && (selectedFile.getPath().substring(selectedFile.getAbsolutePath().lastIndexOf('.')).equals(".txt"))) {
				System.out.println(selectedFile.getPath().substring(selectedFile.getAbsolutePath().lastIndexOf('.')));
				nameInputField.setText(selectedFile.getAbsolutePath());
				try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
					String line;
					while ((line = br.readLine()) != null) {
						if (!line.trim().equals("")) {
							if (!listOfNamesSelected.contains(line)) {
								listOfNamesSelected.add(line);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				namesObsListFile.clear();
				for (String input : listOfNamesSelected) {
					input = input.trim().replaceAll(" +", " ").replaceAll("\\s*-\\s*", "-").replaceAll("-+", "-").replaceAll("^-", "").replaceAll("-$", "");
					String[] inputArray = NameChecker.nameAsArray(input);
					namesObsListFile.add(inputArray);
				}

			}

		}

	}


	public void practiceBtnClicked(ActionEvent actionEvent) {
		if (namesSelectedListView.getItems().isEmpty()) {
			Alert nonSelectedAlert = new Alert(Alert.AlertType.INFORMATION);
			nonSelectedAlert.setTitle("ERROR - Please select some names");
			nonSelectedAlert.setHeaderText(null);
			nonSelectedAlert.setContentText("No name(s) have been entered. Please enter at least one name to practice");
			nonSelectedAlert.showAndWait();
		} else if (namesNotInDatabase.size() > 0) {
			Alert nonSelectedAlert = new Alert(Alert.AlertType.INFORMATION);
			nonSelectedAlert.setTitle("ERROR - Name doesn't exist");
			nonSelectedAlert.setHeaderText(null);
			nonSelectedAlert.setContentText("One of the names entered is not in the database. Please delete it and enter another name.");
			nonSelectedAlert.showAndWait();
		} else {

			if (shuffleButton.isSelected()) {
				shuffleSelected = true;
			} else {
				shuffleSelected = false;
			}
			try {
				nameSelectionMenuRoot = practiceButton.getScene().getRoot();
				FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PracticeMenu.fxml"));
				Parent root = fxmlLoader.load();
				namesSelectedListView.getScene().setRoot(root);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to open practice menu");
			}
		}
	}


	public void deleteBtnClicked(ActionEvent actionEvent) {
		if (selectedNameArray != null) {
			for(String part : selectedNameArray) {
				if(namesNotInDatabase.contains(part)) {
					namesNotInDatabase.remove(part);
				}
			}
			namesSelectedListView.getItems().remove(selectedNameArray);
		}
	}


	public Parent getControllerRoot() {
		return nameSelectionMenuRoot;
	}


	public static List<String> getAddedList() {
		return listOfNamesSelected;
	}


	public void onInputSelected(ActionEvent actionEvent) {
		if (inputMethodChoice.getValue().equals("Manual input")) {
			listOfNamesSelected.clear();
			selectedManual = true;
			nameInputField.clear();
			nameInputField.setPromptText("Enter a name");
			nameInputField.setDisable(false);
			namesSelectedListView.setItems(namesObsListManual);

		} else if (inputMethodChoice.getValue().equals("Browse for text file")) {
			selectedManual = false;
			nameInputField.setPromptText("Browse for a text file by clicking the button -->");
			nameInputField.setDisable(true);
			namesSelectedListView.setItems(namesObsListFile);
		}
	}



	public static ObservableList<String[]> getNamesObList() {
		if (selectedManual) {
			return namesObsListManual;
		}
		return namesObsListFile;
	}


	public void handleDeleteAll(ActionEvent actionEvent) {
		namesSelectedListView.getItems().clear();
		clearHasNone();
	}


	public void handleListSelected(MouseEvent mouseEvent) {
		selectedNameArray = namesSelectedListView.getSelectionModel().getSelectedItem();
	}


	public static boolean isShuffleSelected() {
		return shuffleSelected;
	}


	public static void addToNoneList(String name){
		namesNotInDatabase.add(name);
	}


	public static void removeFromNoneList(String name){
		namesNotInDatabase.remove(name);
	}


	public static void clearHasNone(){
		namesNotInDatabase.clear();
	}

	
	public static List<String> getNoneList() {
		return namesNotInDatabase;
	}

}
