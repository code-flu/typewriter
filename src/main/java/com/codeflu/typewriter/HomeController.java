package com.codeflu.typewriter;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

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
    private TableColumn<InputContent, String> line;

    ObservableList<InputContent> list = FXCollections.observableArrayList();

    // output tab fields
    @FXML
    public Button browseButton;
    @FXML
    public TextField outputFileField;
    @FXML
    public Hyperlink openFileTestLink;
    @FXML
    public Spinner<Integer> typingSpeed;
    @FXML
    public Spinner<Integer> delayBeforeStart;
    @FXML
    public Button startTypingButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTableView();
        enableDragAndDropReordering();
    }

    private void initializeTableView() {
        // Set up cell value factories for the columns
        lineNumber.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        line.setCellValueFactory(new PropertyValueFactory<>("line"));
        // Make editable cell
        line.setCellFactory(TextFieldTableCell.forTableColumn());
        line.setOnEditCommit(evt -> evt.getRowValue().setLine(evt.getNewValue()));
        // Make column resizable
        line.prefWidthProperty().bind(inputTable.widthProperty().multiply(1.0));
        // Initialize table with your data list
        inputTable.setItems(list);
        // Set Typing Speed Spinner properties for numeric values only
        typingSpeed.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2000, 50, 50));
        // Set Delay Spinner properties for numeric values only
        delayBeforeStart.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 3));
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
                        setText("⋮⋮ "+item);
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
                list.add(new InputContent(lineNumber, line));
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
        Map<Integer, InputContent> linesMap = new HashMap<>();
        String selectedFilePath = outputFileField.getText();
        File file = new File(selectedFilePath);
        openFile(file);

        //Start typing into selected file
        try (RandomAccessFile writer = new RandomAccessFile(file, "rw")) {
            for (InputContent content : list) {
                String line = content.getLine();
                String lineWithSpaces = line.replaceAll(".", " ");
                writer.writeBytes(lineWithSpaces + System.lineSeparator());
                linesMap.put(content.getLineNumber(), content);
            }
            // Delay before start
            Thread.sleep(delayBeforeStart.getValue() * 1000);
            // Let's write lines in the file with their priority. But keeping the position same
            for (int i = 1; i <= list.size(); i++) {
                writer.seek(0);
                InputContent inputContent = linesMap.get(i);
                long pointerPosition = getPositionOfLine(writer, list.indexOf(inputContent));
                writer.seek(pointerPosition);
                for (char c : inputContent.getLine().toCharArray()) {
                    writer.write(c);
                    Thread.sleep(typingSpeed.getValue());
                }
            }
        } catch (IOException | InterruptedException e) {
            Alert.error("Typing Error: " + e.getMessage());
        }
    }

    @FXML
    void onReset() {
        if (resetTab.isSelected()) {
            ButtonType buttonType = Alert.confirm("Are you sure you want to reset the table?");
            if(buttonType == ButtonType.YES){
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
}