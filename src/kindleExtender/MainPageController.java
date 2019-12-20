package kindleExtender;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.*;
import kindleExtender.cell.EditCell;
import kindleExtender.helpers.SQLHelper;
import kindleExtender.models.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    // Table with list of words
    public TableView wordsListTableView;
    public ObservableList<Word> wordsObservableList;
    TableColumn<Word, String> wordColumn;

    // Table with list of books
    public TableView booksListTableView;
    public ObservableList<Book> booksObservableList;

    // Table with search history
    public TableView lookUpsListTableView;
    public ObservableList<LookUp> lookUpsObservableList;

    // Helper class that allows user to get and edit data from .db file
    private SQLHelper sqlHelper;

    // Action called on application exit
    public void exitAction(ActionEvent actionEvent) {
        if (sqlHelper != null) {
            if (sqlHelper.hasUnsavedChanges) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Current file is modified");
                alert.setContentText("Save?");
                ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
                var type = alert.showAndWait();
                if (!type.isPresent())
                    return;
                if (type.get().getButtonData() == ButtonBar.ButtonData.YES) {
                    sqlHelper.commit();
                } else if (type.get().getButtonData() == ButtonBar.ButtonData.NO) {
                    sqlHelper.rollback();
                } else {
                    return;
                }
            }
            sqlHelper.close();
        }
        Platform.exit();
    }

    // Action called when user want to open new file
    public void openFileAction(ActionEvent actionEvent) {
        // Let user choose the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQLite file", "*.db")
        );
        File selectedFile = fileChooser.showOpenDialog(wordsListTableView.getScene().getWindow());


        if (selectedFile != null) {
            // Create new instance of SQLHelper for selected file
            sqlHelper = new SQLHelper(selectedFile.getAbsolutePath());

            // Get data from database
            wordsObservableList = FXCollections.observableList(sqlHelper.getWords());
            booksObservableList = FXCollections.observableList(sqlHelper.getBooks());
            lookUpsObservableList = FXCollections.observableList(sqlHelper.getLookUps());

            // Fill tables with data
            wordsListTableView.setItems(wordsObservableList);
            booksListTableView.setItems(booksObservableList);
            lookUpsListTableView.setItems(lookUpsObservableList);
        }
    }

    public void saveAction(ActionEvent actionEvent) {
        if (sqlHelper != null && sqlHelper.hasUnsavedChanges) {
            sqlHelper.commit();
        }
    }

    public void saveAsAction(ActionEvent actionEvent) {
        // TODO: saveAsAction
    }

    public void exportToCSVAction(ActionEvent actionEvent) {
        // TODO: exportToCSVAction
    }

    private void setTableEditable() {
        wordsListTableView.setEditable(true);
        // allows the individual cells to be selected
        wordsListTableView.getSelectionModel().cellSelectionEnabledProperty().set(true);
        // when character or numbers pressed it will start edit in editable
        // fields
        wordsListTableView.setOnKeyPressed(event -> {
            if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                editFocusedCell();
            } else if (event.getCode() == KeyCode.RIGHT
                    || event.getCode() == KeyCode.TAB) {
                wordsListTableView.getSelectionModel().selectNext();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                wordsListTableView.getSelectionModel().selectPrevious();
                event.consume();
            }
        });
    }

    private void editFocusedCell() {
        final TablePosition<Word, ?> focusedCell = wordsListTableView.getEditingCell();
        wordsListTableView.edit(focusedCell.getRow(), focusedCell.getTableColumn());
    }

    private void setupWordColumn() {

        wordColumn.setCellFactory(EditCell.forTableColumn());

        wordColumn.setOnEditCommit(event -> {
            // Get new value
            final String value = event.getNewValue() != null
                    ? event.getNewValue() : event.getOldValue();
            // Get Word instance corresponding to selected row
            Word w = (Word) event.getTableView().getItems()
                    .get(event.getTablePosition().getRow());
            // Update value
            w.setWord(value);
            // TODO: Remove debug messages
            System.out.println(value);
            // TODO: Include in update method
            sqlHelper.hasUnsavedChanges = true;

            wordsListTableView.refresh();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        wordColumn = new TableColumn<Word, String>("Word");
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));
        wordColumn.setMinWidth(150);
        setupWordColumn(); // allows editing specified columns
        setTableEditable(); // make word table editable

        TableColumn<String, Word> countColumn = new TableColumn<>("Count");
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        countColumn.setMinWidth(50);

        wordsListTableView.getColumns().add(wordColumn);
        wordsListTableView.getColumns().add(countColumn);

        TableColumn<String, Book> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setMinWidth(150);
        TableColumn<String, Book> checkedWordsColumn = new TableColumn<>("Checked words");
        checkedWordsColumn.setCellValueFactory(new PropertyValueFactory<>("wordCount"));
        checkedWordsColumn.setMinWidth(150);

        booksListTableView.getColumns().add(titleColumn);
        booksListTableView.getColumns().add(checkedWordsColumn);

        TableColumn<String, LookUp> lookUpsColumn1 = new TableColumn<>("Word");
        lookUpsColumn1.setCellValueFactory(new PropertyValueFactory<>("word"));
        lookUpsColumn1.setMinWidth(150);

        TableColumn<String, LookUp> lookUpsColumn2 = new TableColumn<>("Usage");
        lookUpsColumn2.setCellValueFactory(new PropertyValueFactory<>("usage"));
        lookUpsColumn2.setMinWidth(300);

        TableColumn<String, LookUp> lookUpsColumn3 = new TableColumn<>("Book");
        lookUpsColumn3.setCellValueFactory(new PropertyValueFactory<>("book"));
        lookUpsColumn3.setMinWidth(300);


        lookUpsListTableView.getColumns().add(lookUpsColumn1);
        lookUpsListTableView.getColumns().add(lookUpsColumn2);
        lookUpsListTableView.getColumns().add(lookUpsColumn3);

    }
}
