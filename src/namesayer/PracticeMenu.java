package namesayer;


import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;


public class PracticeMenu implements Initializable {

    private String selectedName;

    private int selectedIndex = 0;

    private ObservableList<String> listToDisplay;

    private ObservableList<String> recordedList;

    private String selectedArchive;

    private boolean contains;

    private String toPlay;

    @FXML
    private Button returnButton;

    @FXML
    private Button prevButton;

    @FXML
    private Button playButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button playArcButton;

    @FXML
    private Button deleteArcButton;

    @FXML
    private Button recordButton;

    @FXML
    private ListView<String> availableListView;

    @FXML
    private ListView<String> displayListView;

    @FXML
    private ProgressBar recordingIndicator;



    @FXML
    private ProgressBar micBar = new ProgressBar();

    @FXML
    private Label playingLabel;

    private List<String[]> namesToPractice;
    private List<NameFile> namesDatabase;

    private File creations = new File("./Creations");

    private String currentName;

    private List<String> attemptDatabase;
    private List<String> listOfAttempts;

    private boolean closePractice = false;

    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-HHmmss");
    private Date date;
    private List<String> namesToPlay;
   


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        namesDatabase = MainMenu.getAddedNames();
        namesToPractice = NameSelectionMenu.getNamesObList();
        listToDisplay = FXCollections.observableArrayList();
        getlistToDisplay();
        displayListView.setItems(listToDisplay);
        displayListView.getSelectionModel().clearSelection();
        displayListView.getSelectionModel().selectFirst();
        selectedIndex = 0;
        selectedName = displayListView.getSelectionModel().getSelectedItem();
        playingLabel.setText(selectedName);
        makeNewAudio(selectedName);
//        newNameSelected();

        // Show microphone level on a ProgressBar
        new Thread() {
            @Override
            public void run() {
                micBar.setStyle("-fx-accent: red;");
                // Taken from https://stackoverflow.com/questions/15870666/calculating-microphone-volume-trying-to-find-max
                // Open a TargetDataLine for getting microphone input & sound level
                TargetDataLine line = null;
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); 	// format is an AudioFormat object
                if (!AudioSystem.isLineSupported(info)) {
                    System.out.println("The line is not supported.");
                }
                // Obtain and open the line.
                try {
                    line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();
                } catch (LineUnavailableException ex) {
                    System.out.println("The TargetDataLine is Unavailable.");
                }

                while (true) {
                    byte[] bytes = new byte[line.getBufferSize() / 5];
                    line.read(bytes, 0, bytes.length);
                    double prog = (double) calculateRMSLevel(bytes) / 65;
                    micBar.setProgress(prog);

                    if (closePractice || !micBar.getScene().getWindow().isShowing()) {
                        line.close();
                        return;
                    }
                }
            }
        }.start();

    }


    public void handlePrevButton(ActionEvent actionEvent) {
        if (selectedIndex == 0) {
//            displayListView.scrollTo(selectedIndex);
//            displayListView.getSelectionModel().selectFirst();
        } else {
            selectedIndex--;
            displayListView.scrollTo(selectedIndex);
            displayListView.getSelectionModel().select(selectedIndex);
//        }
            selectedName = displayListView.getSelectionModel().getSelectedItem();
            playingLabel.setText(selectedName);
//        newNameSelected();
        }
    }


    public void handleNextButton(ActionEvent actionEvent) {
        if (selectedIndex == listToDisplay.size() - 1) {
//            displayListView.scrollTo(selectedIndex);
//            displayListView.getSelectionModel().selectLast();
        } else {
            selectedIndex++;
            displayListView.scrollTo(selectedIndex);
            displayListView.getSelectionModel().select(selectedIndex);

            selectedName = displayListView.getSelectionModel().getSelectedItem();
            playingLabel.setText(selectedName);
//            newNameSelected();
        }
    }


    public void handlePlayButton(ActionEvent actionEvent) {
    	for(String s : namesToPlay){
    		playAudio("names /"+s);
    	}
    }


    public void handleArcListClicked(MouseEvent mouseEvent) {
        selectedArchive = availableListView.getSelectionModel().getSelectedItem();
    }


    public void handlePlayArc(ActionEvent actionEvent) {
        if (selectedArchive == null) {
            noFileAlert();
        } else {
            toPlay = currentName;
            String fileToPlay = toPlay.substring(0, toPlay.lastIndexOf("_")+1) + selectedArchive;
            playAudio("Creations/" + fileToPlay + ".wav");
        }
    }


    private void playAudio(String fileToPlay) {
        new Thread() {
            @Override
            public void run() {
                try {
                    AudioInputStream stream = AudioSystem.getAudioInputStream(new File(fileToPlay));
                    AudioFormat format = stream.getFormat();
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
                    sourceLine.open(format);
                    sourceLine.start();

                    // Disable buttons while audio file plays
                    long frames = stream.getFrameLength();
                    long durationInSeconds = (frames / (long)format.getFrameRate());
                    setAllButtonsDisabled(true);
                    PauseTransition pause = new PauseTransition(Duration.seconds(durationInSeconds));
                    pause.setOnFinished(event -> {
                        setAllButtonsDisabled(false);
                        Thread.currentThread().interrupt();
                    });
                    pause.play();

                    int nBytesRead = 0;
                    int BUFFER_SIZE = 128000;
                    byte[] abData = new byte[BUFFER_SIZE];
                    while (nBytesRead != -1) {
                        try {
                            nBytesRead = stream.read(abData, 0, abData.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (nBytesRead >= 0) {
                            @SuppressWarnings("unused")
                            int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
                        }
                    }
                    sourceLine.drain();
                    sourceLine.close();
                } catch (Exception e) {
                    System.out.println("FAILED TO PLAY FILE");
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public void handleDeleteArc(ActionEvent actionEvent) {
        if (selectedArchive == null) {
            noFileAlert();
        } else {
            toPlay = currentName;
            String fileToDelete = toPlay.substring(0, toPlay.lastIndexOf("_")+1) + selectedArchive;
            String fileString = "Creations/" + fileToDelete + ".wav";
            File toDelete = new File(fileString);
            if (toDelete.exists()) {
                Alert deleteConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete:" + selectedArchive + "?", ButtonType.YES, ButtonType.NO);
                deleteConfirm.showAndWait();
                if (deleteConfirm.getResult() == ButtonType.YES) {

                    try {
                        Files.deleteIfExists(toDelete.toPath());
                    } catch (IOException e) {
                        System.out.println("FAILED TO DELETE");
                        e.printStackTrace();
                    }

                    listOfAttempts.remove(fileToDelete);
                    updateArchive();
                    availableListView.getSelectionModel().clearSelection();
                    selectedArchive = null;
                }
            } else {
                if (!contains) {
                    availableListView.setMouseTransparent(true);
                    availableListView.setFocusTraversable(false);
                }
            }
        }
        //		updateArchive();
    }


    public void handleRecordAction(ActionEvent actionEvent) {
        date = new Date();
        String currentTime = formatter.format(date);
        String recordingName = currentName + " " + currentTime;
        String recordCommand = "ffmpeg -f alsa -ac 1 -ar 44100 -i default -t 5 \"" + recordingName + "\".wav";
        ProcessBuilder recordAudio = new ProcessBuilder("/bin/bash", "-c", recordCommand);
        recordAudio.directory(creations);

        try{
            recordAudio.start();
        } catch (IOException e){
            e.printStackTrace();
        }

        setAllButtonsDisabled(true);
        // Time 5 seconds and set progress bar accordingly
        new Thread() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                recordButton.setDisable(true);
                while (System.currentTimeMillis() < (startTime + 5000)) {
                    double recordingProgress = (System.currentTimeMillis() - startTime) / 5000.0;
                    recordingIndicator.setProgress(recordingProgress);
                }
                recordButton.setDisable(false);
                setAllButtonsDisabled(false);
                return;
            }
        }.start();


        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        recordingIndicator.setProgress(0);
                    }
                },
                5000
        );

        listOfAttempts.add(recordingName);
        updateArchive();
    }


    public void noFileAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ERROR");
        alert.setHeaderText(null);
        alert.setContentText("No file selected");
        alert.showAndWait();
    }


    // Taken from https://stackoverflow.com/questions/15870666/calculating-microphone-volume-trying-to-find-max
    protected static int calculateRMSLevel(byte[] audioData) {
        // audioData might be buffered data read from a data line
        long lSum = 0;
        for (int i = 0; i < audioData.length; i++)
            lSum = lSum + audioData[i];

        double dAvg = lSum / audioData.length;

        double sumMeanSquare = 0d;
        for (int j = 0; j < audioData.length; j++)
            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);

        double averageMeanSquare = sumMeanSquare / audioData.length;
        return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
    }





    public void newNameSelected() {
        getCurrentName();
        initialiseAttemptDatabase();
        fillAttemptList();
        updateArchive();
        selectedArchive = null;
    }


    public void getCurrentName() {
        currentName = selectedName;
    }


    public void initialiseAttemptDatabase() {
        attemptDatabase = new ArrayList<String>(Arrays.asList(creations.list()));
    }


    public void fillAttemptList() {
        for (String s : attemptDatabase) {
            if (s.lastIndexOf(" ") != -1) {
                String nameMatch = s.substring(0, s.lastIndexOf(" ")-1);
                if (currentName.equals(nameMatch)) {
                    String toAddToList = s.substring(0, s.lastIndexOf("."));
                    if (!listOfAttempts.contains(toAddToList)) {
                        listOfAttempts.add(toAddToList);
                    }
                }
            }
        }
    }


    // Update attempts list
    public void updateArchive() {
        recordedList = FXCollections.observableArrayList(listOfAttempts);
        if (recordedList.size() == 0) {
            contains = false;
            availableListView.setMouseTransparent(true);
            availableListView.setFocusTraversable(false);
        } else {
            contains = true;
            availableListView.setMouseTransparent(false);
            availableListView.setFocusTraversable(true);
        }
        availableListView.setItems(recordedList);
        availableListView.getSelectionModel().clearSelection();
    }


    private void setAllButtonsDisabled(boolean b) {
        playButton.setDisable(b);
        prevButton.setDisable(b);
        nextButton.setDisable(b);
        recordButton.setDisable(b);
        playArcButton.setDisable(b);
        deleteArcButton.setDisable(b);
    }


    public void returnToNameSelection() {
    	NameSelectionMenu.clearHasNone();
        closePractice = true;
        NameSelectionMenu ctrl = new NameSelectionMenu();
        returnButton.getScene().setRoot(ctrl.getControllerRoot());
    }

    public void getlistToDisplay(){
        for( String[] s : namesToPractice){
            String displayName = String.join("", s);
            listToDisplay.add(displayName);
        }
    }
    public void makeNewAudio(String name){
    	
    	String[] nameArray = namesToPractice.get(selectedIndex);
    	namesToPlay = new ArrayList<>();
    	for(String s : nameArray){
    		for(NameFile namefile : namesDatabase){
    			if(namefile.toString().equals(s)){
    				namesToPlay.add(namefile.getFileName());
    			}
    		}
    	}
    }
    


    public void handleDisplayListClicked(MouseEvent mouseEvent) {
        selectedName = displayListView.getSelectionModel().getSelectedItem();
        selectedIndex = listToDisplay.indexOf(selectedName);
        playingLabel.setText(selectedName);
//        newNameSelected();

    }
}
