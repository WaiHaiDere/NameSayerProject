package namesayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;


public class NameSelectionMenu implements Initializable {
	@FXML
	private Button mainMenuBtn;
	@FXML
	private ComboBox<String> inputMethodChoice;
	@FXML
	private TextField nameInputField;
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
	private static boolean hasNone = false;

	private String[] testData1 = new String[]{"this", "is", "a", "test"};
	private String[] testData2 = new String[]{"Mike", "-", "John", " ", "Lee"};
	private String[] testData3 = new String[]{"John", "-", "Catherine", " ", "Cena", "-", "Wison", " ", "Anderson"};
	private String[] testData4 = new String[]{"Antony"};
	private String[] testData5 = new String[]{"Ryan", " ", "Lim", " ", "Li", "-", "Bruce"};
	private static ObservableList<String[]> namesObsList = FXCollections.observableArrayList();

	private boolean selectedManual;
	private String[] selectedNameArray;
	private static boolean shuffleSelected;
	private static List<String> namesNotInDatabase = new ArrayList<>();



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> rateList = FXCollections.observableArrayList("Browse for text file", "Manual input");
		inputMethodChoice.setItems(rateList);
		inputMethodChoice.setValue("Browse for text file");
		nameInputField.setPromptText("Choose a text file");
		nameInputField.setDisable(true);

		namesObsList.addAll(testData2, testData1, testData3, testData4, testData5);
		namesSelectedListView.setItems(namesObsList);
		namesSelectedListView.setCellFactory(names -> new NameListCell());

		namesSelectedListView.setMouseTransparent(false);
		namesSelectedListView.setFocusTraversable(true);
	}


	public void mainMenuBtnClicked(ActionEvent actionEvent) {
		mainMenuBtn.getScene().setRoot(MainMenu.getMainMenuRoot());
	}


	public void addNameBtnClicked(ActionEvent actionEvent) {

		if (selectedManual) {
			if(((nameInputField.getText() == null) || nameInputField.getText().trim().equals(""))) {
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
					namesObsList.add(inputArray);
					nameInputField.setText(null);
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
							listOfNamesSelected.add(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				namesObsList.clear();
				for (String input : listOfNamesSelected) {
					input = input.trim().replaceAll(" +", " ").replaceAll("\\s*-\\s*", "-").replaceAll("-+", "-").replaceAll("^-", "").replaceAll("-$", "");
					String[] inputArray = NameChecker.nameAsArray(input);
					namesObsList.add(inputArray);
				}

			}

		}

	}


	public void enterPressed(ActionEvent actionEvent) {
		addNameBtnClicked(actionEvent);
	}


	public void practiceBtnClicked(ActionEvent actionEvent) {
		if (namesObsList.isEmpty()) {
			Alert nonSelectedAlert = new Alert(Alert.AlertType.INFORMATION);
			nonSelectedAlert.setTitle("ERROR - Please select some names");
			nonSelectedAlert.setHeaderText(null);
			nonSelectedAlert.setContentText("No name(s) have been entered. Please enter at least one name to practice");
			nonSelectedAlert.showAndWait();
		} else if(namesNotInDatabase.size() > 0){
			Alert nonSelectedAlert = new Alert(Alert.AlertType.INFORMATION);
			nonSelectedAlert.setTitle("ERROR - Name doesn't exist");
			nonSelectedAlert.setHeaderText(null);
			nonSelectedAlert.setContentText("One of the names entered is not in the database. Please delete it and enter another name.");
			nonSelectedAlert.showAndWait();
		} else {
		
			if(shuffleButton.isSelected()){
				shuffleSelected = true;
			} else{
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
		for(String part : selectedNameArray){
			if(namesNotInDatabase.contains(part)){
				namesNotInDatabase.remove(part);
			}
		}
		namesObsList.remove(selectedNameArray);

		System.out.println(namesNotInDatabase);

	}


	public Parent getControllerRoot() {
		return nameSelectionMenuRoot;
	}


	public static List<String> getAddedList() {
		return listOfNamesSelected;
	}


	public void onInputSelected(ActionEvent actionEvent) {
		if(inputMethodChoice.getValue().equals("Manual input")) {
			listOfNamesSelected.clear();
			selectedManual = true;
			nameInputField.setPromptText("Enter a name");
			nameInputField.setDisable(false);

		} else if (inputMethodChoice.getValue().equals("Browse for text file")) {
			selectedManual = false;
			nameInputField.setPromptText("Browse for a text file by clicking the button -->");
			nameInputField.setDisable(true);

		}
	}

	public static ObservableList<String[]> getNamesObList(){
		return namesObsList;
	}

	public void handleDeleteAll(ActionEvent actionEvent) {
		namesObsList.clear();
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

	public static List<String> getNoneList(){
		return namesNotInDatabase;
	}

}
