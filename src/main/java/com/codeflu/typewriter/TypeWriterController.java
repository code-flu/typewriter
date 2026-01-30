package com.codeflu.typewriter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for the Typewriter application UI.
 * Manages the syntax editor, reordering functionality, and typewriter simulation.
 */
public class TypeWriterController implements Initializable {

    // Constants for typing speed
    private static final int MIN_TYPING_SPEED = 10;
    private static final int MAX_TYPING_SPEED = 1000;
    private static final int DEFAULT_TYPING_SPEED = 50;

    // UI Constants
    private static final String GRIP_ICON = "⠿";

    // FXML injected components
    @FXML
    private AnchorPane editorContainer;
    @FXML
    private Spinner<Integer> typingSpeed;
    @FXML
    private Button startTypingButton, stopButton;
    @FXML
    private ComboBox<String> languageSelector;
    @FXML
    private ToggleButton reorderModeToggle;
    @FXML
    private TableView<ReorderLine> inputTable;
    @FXML
    private TableColumn<ReorderLine, Integer> lineNumber;
    @FXML
    private TableColumn<ReorderLine, String> lineText;

    // ...existing code...

    private final ObservableList<ReorderLine> list = FXCollections.observableArrayList();
    private final Map<String, String> languageMap = new LinkedHashMap<>();
    private RSyntaxTextArea rsyntaxArea;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> currentTypingTask;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SwingNode swingNode = new SwingNode();
        createSwingEditor(swingNode);

        AnchorPane.setTopAnchor(swingNode, 0.0);
        AnchorPane.setBottomAnchor(swingNode, 0.0);
        AnchorPane.setLeftAnchor(swingNode, 0.0);
        AnchorPane.setRightAnchor(swingNode, 0.0);
        editorContainer.getChildren().add(swingNode);

        setupLanguageSelector();
        setupTableColumns();
        enableDragAndDropReordering();

        typingSpeed.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_TYPING_SPEED, MAX_TYPING_SPEED, DEFAULT_TYPING_SPEED
            )
        );
        inputTable.setItems(list);
    }

    /**
     * Creates the Swing-based syntax highlighting editor and embeds it in JavaFX.
     *
     * @param swingNode the SwingNode to embed the editor in
     */
    private void createSwingEditor(SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            rsyntaxArea = new RSyntaxTextArea();
            rsyntaxArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            rsyntaxArea.setCodeFoldingEnabled(true);
            rsyntaxArea.setAntiAliasingEnabled(true);

            RTextScrollPane sp = new RTextScrollPane(rsyntaxArea);
            sp.setLineNumbersEnabled(true);
            swingNode.setContent(sp);
        });
    }

    /**
     * Configures the table columns to display line numbers and text.
     */
    private void setupTableColumns() {
        lineNumber.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        lineText.setCellValueFactory(new PropertyValueFactory<>("lineText"));
    }

    /**
     * Toggles between editor mode and reorder mode.
     * In reorder mode, the editor content is split into lines that can be dragged to reorder.
     */
    @FXML
    private void handleToggleMode() {
        if (reorderModeToggle.isSelected()) {
            // Ensure editor is initialized before accessing
            if (rsyntaxArea == null) {
                showErrorAlert();
                reorderModeToggle.setSelected(false);
                return;
            }

            // Capture current text from RSyntaxTextArea
            String currentCode = rsyntaxArea.getText();
            if (currentCode == null || currentCode.isEmpty()) {
                list.clear();
            } else {
                String[] lines = currentCode.split("\\R");

                // Fill the TableView list for reordering
                list.clear();
                for (int i = 0; i < lines.length; i++) {
                    list.add(new ReorderLine(i + 1, lines[i]));
                }
            }

            editorContainer.setVisible(false);
            inputTable.setVisible(true);
            startTypingButton.setDisable(true);
        } else {
            inputTable.setVisible(false);
            editorContainer.setVisible(true);
            startTypingButton.setDisable(false);
        }
    }

    /**
     * Initiates the typewriter simulation with prioritized line typing.
     * Lines are typed in the order determined by the user's drag-and-drop reordering.
     */
    @FXML
    private void handleStartTyping() {
        if (rsyntaxArea == null) {
            showErrorAlert();
            return;
        }

        // Stop any existing typing task
        if (currentTypingTask != null && !currentTypingTask.isDone()) {
            currentTypingTask.cancel(true);
        }

        // Preserve the original spatial layout (visual positions in editor)
        List<ReorderLine> spatialLayout = new ArrayList<>(list);

        // Determine the typing priority (Sorted by the user's dragged line numbers)
        List<ReorderLine> priorityQueue = new ArrayList<>(list);
        priorityQueue.sort(Comparator.comparingInt(ReorderLine::getLineNumber));

        SwingUtilities.invokeLater(() -> {
            // Pre-fill the editor with empty lines to establish the positions
            StringBuilder hollowFrame = new StringBuilder();
            for (ReorderLine ignored : spatialLayout) {
                hollowFrame.append("\n");
            }
            rsyntaxArea.setText(hollowFrame.toString());
            rsyntaxArea.setEditable(false);

            // Start the prioritized simulation with both spatial layout and priority queue
            startPrioritySimulation(spatialLayout, priorityQueue);
        });

        startTypingButton.setDisable(true);
        stopButton.setDisable(false);
        reorderModeToggle.setDisable(true);
    }

    /**
     * Starts the priority-based simulation that types lines in user-defined order.
     *
     * @param spatialLayout  the original spatial layout of lines (visual positions)
     * @param priorityQueue  the lines sorted by priority (typing order)
     */
    private void startPrioritySimulation(List<ReorderLine> spatialLayout, List<ReorderLine> priorityQueue) {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "TypewriterExecutor");
                thread.setDaemon(true);
                return thread;
            });
        }

        // Start processing the priority queue
        processPriorityQueue(spatialLayout, priorityQueue, 0);
    }

    /**
     * Processes the queue of lines to be typed in priority order.
     * Types lines in the order determined by priority, but in their original spatial positions.
     *
     * @param spatialLayout  the original spatial layout (visual positions)
     * @param queue          the priority queue of lines to type
     * @param queueIndex     the current index in the queue
     */
    private void processPriorityQueue(List<ReorderLine> spatialLayout, List<ReorderLine> queue, int queueIndex) {
        if (queueIndex >= queue.size() || executor == null || executor.isShutdown()) {
            handleStopTyping();
            return;
        }

        ReorderLine currentLine = queue.get(queueIndex);
        // Find where this line belongs SPATIALLY (its visual position in the original layout)
        int spatialIndex = spatialLayout.indexOf(currentLine);

        char[] chars = currentLine.getLineText().toCharArray();
        AtomicInteger charOffset = new AtomicInteger(0);
        final int nextIndex = queueIndex + 1;

        // Schedule typing for this specific line
        currentTypingTask = executor.scheduleAtFixedRate(() -> {
            if (currentTypingTask.isCancelled() || executor.isShutdown()) {
                return;
            }

            if (charOffset.get() < chars.length) {
                int column = charOffset.getAndIncrement();
                char c = chars[column];

                SwingUtilities.invokeLater(() -> {
                    try {
                        // Calculate the absolute caret position for this line/column
                        // Uses spatialIndex to place text in original visual position
                        int offset = rsyntaxArea.getLineStartOffset(spatialIndex) + column;
                        rsyntaxArea.insert(String.valueOf(c), offset);
                    } catch (Exception e) {
                        System.err.println("Error inserting character: " + e.getMessage());
                    }
                });
            } else {
                // Line finished, cancel this task and process next line in priority queue
                if (currentTypingTask != null) {
                    currentTypingTask.cancel(false);
                }
                // Schedule next line processing after current task completes
                executor.schedule(() -> processPriorityQueue(spatialLayout, queue, nextIndex), 0, TimeUnit.MILLISECONDS);
            }
        }, 0, typingSpeed.getValue(), TimeUnit.MILLISECONDS);
    }

    /**
     * Legacy method for simple sequential line typing (not currently used).
     * This method types content sequentially without priority reordering.
     *
     * @param content the text content to type
     * @deprecated Use startPrioritySimulation for priority-based typing
     */
    @Deprecated
    private void startSimulation(String content) {
        executor = Executors.newSingleThreadScheduledExecutor();
        char[] chars = content.toCharArray();
        AtomicInteger index = new AtomicInteger(0);

        executor.scheduleAtFixedRate(() -> {
            if (index.get() < chars.length) {
                char c = chars[index.getAndIncrement()];
                SwingUtilities.invokeLater(() -> rsyntaxArea.append(String.valueOf(c)));
            } else {
                handleStopTyping();
            }
        }, 0, typingSpeed.getValue(), TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the ongoing typewriter simulation and re-enables the editor.
     */
    @FXML
    private void handleStopTyping() {
        if (currentTypingTask != null && !currentTypingTask.isDone()) {
            currentTypingTask.cancel(true);
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }

        Platform.runLater(() -> {
            startTypingButton.setDisable(false);
            stopButton.setDisable(true);
            reorderModeToggle.setDisable(false);
        });

        SwingUtilities.invokeLater(() -> {
            if (rsyntaxArea != null) {
                rsyntaxArea.setEditable(true);
            }
        });
    }

    /**
     * Initializes the language selector with supported syntax highlighting options.
     * Updates the editor syntax style when a language is selected.
     */
    private void setupLanguageSelector() {
        languageMap.put("Java", SyntaxConstants.SYNTAX_STYLE_JAVA);
        languageMap.put("Python", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        languageMap.put("C#", SyntaxConstants.SYNTAX_STYLE_CSHARP);
        languageMap.put("JavaScript", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        languageMap.put("HTML", SyntaxConstants.SYNTAX_STYLE_HTML);
        languageMap.put("XML", SyntaxConstants.SYNTAX_STYLE_XML);
        languageMap.put("SQL", SyntaxConstants.SYNTAX_STYLE_SQL);
        languageMap.put("C++", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);

        languageSelector.getItems().addAll(languageMap.keySet());
        languageSelector.getSelectionModel().select("Java");

        languageSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && rsyntaxArea != null) {
                String style = languageMap.get(newVal);
                SwingUtilities.invokeLater(() -> rsyntaxArea.setSyntaxEditingStyle(style));
            }
        });
    }

    /**
     * Enables drag-and-drop reordering of lines in the table view.
     * Users can drag lines to change their typing priority order.
     */
    private void enableDragAndDropReordering() {
        lineNumber.setCellFactory(column -> {
            TableCell<ReorderLine, Integer> cell = new TableCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(GRIP_ICON + " " + item);
                    }
                }

                {
                    setOnMouseEntered(event -> {
                        if (!isEmpty()) {
                            getTableView().getScene().setCursor(Cursor.OPEN_HAND);
                        }
                    });
                    setOnMousePressed(event -> {
                        if (!isEmpty()) {
                            getTableView().getScene().setCursor(Cursor.CLOSED_HAND);
                        }
                    });
                    setOnMouseExited(event -> {
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
                    try {
                        int draggedIndex = Integer.parseInt(db.getString());
                        int dropIndex = cell.getIndex();
                        ObservableList<ReorderLine> items = inputTable.getItems();

                        // Validate bounds
                        if (draggedIndex >= 0 && draggedIndex < items.size() &&
                            dropIndex >= 0 && dropIndex < items.size()) {
                            ReorderLine draggedItem = items.get(draggedIndex);
                            ReorderLine droppedItem = items.get(dropIndex);

                            // Swap the line numbers
                            int tempLineNumber = draggedItem.getLineNumber();
                            draggedItem.setLineNumber(droppedItem.getLineNumber());
                            droppedItem.setLineNumber(tempLineNumber);

                            inputTable.refresh();
                            event.setDropCompleted(true);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid drag index: " + e.getMessage());
                    }
                }
                event.consume();
            });

            return cell;
        });
    }

    /**
     * Displays an error alert dialog to the user.
     */
    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Editor not initialized");
        alert.setHeaderText("Editor not initialized");
        alert.setContentText("Please wait for the editor to load.");
        alert.showAndWait();
    }
}
