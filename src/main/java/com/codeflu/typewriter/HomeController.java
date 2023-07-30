package com.codeflu.typewriter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class HomeController implements Initializable {

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab inputTab;
    @FXML
    private Tab resetTab;
    @FXML
    public Button pasteButton;

    // input tab fields
    @FXML
    private TableView<InputContent> inputTable;
    @FXML
    private TableColumn<InputContent, Integer> lineNumber;
    @FXML
    public TableColumn<InputContent, String> preSpeech;
    @FXML
    private TableColumn<InputContent, String> line;
    @FXML
    public TableColumn<InputContent, String> postSpeech;
    @FXML
    public TableColumn<InputContent, Boolean> hasPause;

    ObservableList<InputContent> list = FXCollections.observableArrayList();

    // output tab fields
    @FXML
    public Button browseButton;
    @FXML
    public TextField outputFileField;
    @FXML
    public Hyperlink openFileTestLink;
    @FXML
    public CheckBox openFileChecked;
    @FXML
    public Spinner<Integer> typingSpeed;
    @FXML
    public Button startTypingButton;
    @FXML
    public Button resumeTypingButton;

    // Create the pauseSemaphore to pause typing
    CountDownLatch pauseLatch = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTableView();
        customColumnCells();
        enableDragAndDropReordering();
    }

    private void initializeTableView() {
        // Set up cell value factories for the columns
        lineNumber.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        preSpeech.setCellValueFactory(new PropertyValueFactory<>("preSpeech"));
        preSpeech.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/pre_speech.png")))));
        preSpeech.setStyle("-fx-text-fill: green;");
        line.setCellValueFactory(new PropertyValueFactory<>("line"));
        postSpeech.setCellValueFactory(new PropertyValueFactory<>("postSpeech"));
        postSpeech.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/post_speech.png")))));
        postSpeech.setStyle("-fx-text-fill: green;");
        hasPause.setCellValueFactory(new PropertyValueFactory<>("hasPause"));
        hasPause.setCellValueFactory(cellData -> cellData.getValue().getHasPause());
        hasPause.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/stop.png")))));
        hasPause.setCellFactory(CheckBoxTableCell.forTableColumn(hasPause));
        // Make editable cell
        line.setCellFactory(TextFieldTableCell.forTableColumn());
        line.setOnEditCommit(evt -> evt.getRowValue().setLine(evt.getNewValue()));
        // Initialize table with your data list
        inputTable.setItems(list);
        // Set Typing Speed Spinner properties for numeric values only
        typingSpeed.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2000, 50, 50));
        openFileChecked.setSelected(true);
        // Listener to update resizable column width when table width changes
        inputTable.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue();
            double newResizableColumnWidth = newWidth - (32 * 4);
            line.setPrefWidth(newResizableColumnWidth);
        });
    }

    private void customColumnCells() {
        preSpeech.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean b) {
                setOnMouseClicked(onClick -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Speech Test");
                    dialog.setHeaderText("Text to speech before writing:");
                    dialog.setContentText(getText());
                    dialog.showAndWait().ifPresent(this::setText);
                    inputTable.getItems().get(getIndex()).setPreSpeech(getText());
                });
            }
        });
        postSpeech.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean b) {
                setOnMouseClicked(onClick -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Output Text");
                    dialog.setHeaderText("Text to speech after writing:");
                    dialog.setContentText(getText());
                    dialog.showAndWait().ifPresent(this::setText);
                    postSpeech.setText(getText());
                    inputTable.getItems().get(getIndex()).setPostSpeech(getText());
                });
            }
        });

        postSpeech.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean b) {
                setOnMouseClicked(onClick -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Output Text");
                    dialog.setHeaderText("Text to speech after writing:");
                    dialog.setContentText(getText());
                    dialog.showAndWait().ifPresent(this::setText);
                    postSpeech.setText(getText());
                    inputTable.getItems().get(getIndex()).setPostSpeech(getText());
                });
            }
        });
    }

    private void enableDragAndDropReordering() {
        // Enable row reordering line using drag and drop for lineNumber column
        lineNumber.setCellFactory(column -> {
            TableCell<InputContent, Integer> cell = new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("⋮⋮ " + item);
                    }
                }

                {
                    setOnMouseEntered(event -> {
                        if (!isEmpty()) {
                            // Change the cursor to the open hand when entering the cell
                            getTableView().getScene().setCursor(Cursor.OPEN_HAND);
                        }
                    });
                    setOnMousePressed(event -> {
                        if (!isEmpty()) {
                            // Change the cursor to the drag icon when pressed the cell
                            getTableView().getScene().setCursor(Cursor.CLOSED_HAND);
                        }
                    });
                    setOnMouseExited(event -> {
                        // Change the cursor back to the default cursor when leaving the cell
                        getTableView().getScene().setCursor(Cursor.DEFAULT);
                    });
                }
            };
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(String.valueOf(cell.getIndex()));
                    db.setDragView(cell.snapshot(null, null));
                    db.setContent(cc);
                }
                event.consume();
            });

            cell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    if (draggedIndex != cell.getIndex()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    int dropIndex = cell.getIndex();

                    // Get the dragged item and update its lineNumber
                    InputContent draggedItem = inputTable.getItems().get(draggedIndex);
                    InputContent droppedItem = inputTable.getItems().get(dropIndex);

                    // Swap the line numbers
                    int tempLineNumber = draggedItem.getLineNumber();
                    draggedItem.setLineNumber(droppedItem.getLineNumber());
                    droppedItem.setLineNumber(tempLineNumber);

                    // Update the TableView
                    inputTable.refresh();
                }
                event.consume();
            });

            return cell;
        });
    }

    @FXML
    private void onPasteButtonClick() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String clipboardContent = clipboard.getString();
            String[] lines = clipboardContent.split("\n");

            int lineNumber = 1;
            list.clear();
            for (String line : lines) {
                list.add(new InputContent(lineNumber, null, line, null, 0,false));
                lineNumber++;
            }
        }
    }

    @FXML
    private void onBrowseButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Output File");
        // Show the file chooser dialog and get the selected file
        File selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedFile != null) {
            // Set the selected file path in the TextField
            outputFileField.setText(selectedFile.getAbsolutePath());
            outputFileField.positionCaret(outputFileField.getText().length());
            // Enable buttons
            openFileTestLink.setDisable(false);
            startTypingButton.setDisable(false);
        }
    }

    @FXML
    private void onOpenFileTestLinkClick() {
        // test weather file is opening or not
        String selectedFilePath = outputFileField.getText();
        File file = new File(selectedFilePath);
        openFile(file);
    }

    @FXML
    private void OnStartTypingButtonClick() {
        // disable buttons
        openFileTestLink.setDisable(true);
        startTypingButton.setDisable(true);
        Map<Integer, InputContent> linesMap = new HashMap<>();
        String selectedFilePath = outputFileField.getText();
        File file = new File(selectedFilePath);
        if(openFileChecked.isSelected()) openFile(file);

        //Start typing into selected file
        try {
            RandomAccessFile writer = new RandomAccessFile(file, "rw");
            for (InputContent content : list) {
                String line = content.getLine();
                String lineWithSpaces = line.replaceAll(".", " ");
                writer.writeBytes(lineWithSpaces + System.lineSeparator());
                linesMap.put(content.getLineNumber(), content);
            }
            // Create the process builder
            ProcessBuilder pb = new ProcessBuilder();
            // Create a ScheduledExecutorService with a single thread to schedule the character writing tasks.
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            // execute type line function
            typeLine(writer, pb, executor,  linesMap, 1);
            // Enable buttons
            openFileTestLink.setDisable(false);
            startTypingButton.setDisable(false);
        } catch (IOException e) {
            Alert.error("Typing Error: " + e.getMessage());
        }
    }

    @FXML
    private void OnResumeTypingButtonClick() {
        resumeTypingButton.setDisable(true);
        pauseLatch.countDown();
    }

    @FXML
    void onReset() {
        if (resetTab.isSelected()) {
            ButtonType buttonType = Alert.confirm("Are you sure you want to reset the table?");
            if (buttonType == ButtonType.YES) {
                // Clear input table and
                list.clear();
            }
            tabPane.getSelectionModel().select(inputTab);
        }
    }

    private void openFile(File file) {
        try {
            if (file.exists() && file.isFile()) {
                Desktop.getDesktop().open(file);
            } else {
                Alert.error("File does not exist or is not a regular file.");
            }
        } catch (IOException e) {
            Alert.error(e.getMessage());
        }
    }

    // Helper method to get the position of a specific line in the output
    private static long getPositionOfLine(RandomAccessFile raf, int lineNumber) throws IOException {
        raf.seek(0); // Start from the beginning of the file
        int currentLineNumber = 0;
        long position = 0;
        String line;
        while ((line = raf.readLine()) != null && currentLineNumber < lineNumber) {
            currentLineNumber++;
            position = raf.getFilePointer();
        }
        return position;
    }

    // Typing service to type using executor service
    public void typeLine(RandomAccessFile writer, ProcessBuilder pb, ScheduledExecutorService executor, Map<Integer, InputContent> linesMap, int i) throws IOException {
        if(linesMap.size()<i) {
            writer.close();
            executor.shutdown();
            return;
        }
        writer.seek(0);
        InputContent inputContent = linesMap.get(i);
        long pointerPosition = getPositionOfLine(writer, list.indexOf(inputContent));
        writer.seek(pointerPosition);
        // Perform the pre-speech if available
        speech(pb, inputContent.getPreSpeech());
        // Retrieve the line of text to be typed
        char[] line = inputContent.getLine().toCharArray();
        // Set the typing speed (delay between each character) in milliseconds.
        int typingSpeedMillis = typingSpeed.getValue();
        // Variable to keep track of the current character index.
        int[] currentIndex = {0};
        // Create a new CountDownLatch for each pause
        pauseLatch = new CountDownLatch(1);
        // Schedule the character writing task with a fixed delay.
        Runnable characterWriter = () -> {
            if (currentIndex[0] < line.length) {
                char c = line[currentIndex[0]];
                try {
                    writer.write(c);
                    writer.getChannel().force(true);
                } catch (IOException e) {
                    //Alert.error("Typing Error: " + e.getMessage());
                    e.printStackTrace();
                }
                currentIndex[0]++;
            } else {
                // shutdown the executor
                executor.shutdown();
                // If all characters are written, perform the post-speech if available.
                speech(pb, inputContent.getPostSpeech());
                // Recursive call to type next line
                try {
                    // Check if there's a pause and wait for the semaphore if necessary
                    if (inputContent.getHasPause().getValue()) {
                        try {
                            resumeTypingButton.setDisable(false);
                            pauseLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    typeLine(writer, pb, Executors.newSingleThreadScheduledExecutor(), linesMap, i+1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        // Schedule the characterWriter task to run with a fixed delay between each character.
        executor.scheduleAtFixedRate(characterWriter, 0, typingSpeedMillis, TimeUnit.MILLISECONDS);
    }

    // Running text to speech command on terminal using process builder.
    public void speech(ProcessBuilder pb, String text) {
        if (text == null || text.isBlank()) return;
        pb.command("say", text);
        try {
            // execute command
            Process process = pb.start();
            // Wait for the speech to finish
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            Alert.error(e.getMessage());
        }
    }

}