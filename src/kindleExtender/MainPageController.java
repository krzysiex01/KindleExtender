package kindleExtender;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import kindleExtender.cell.EditCell;
import kindleExtender.helpers.*;
import kindleExtender.models.Book;
import kindleExtender.models.LookUp;
import kindleExtender.models.Word;

public class MainPageController implements Initializable {
    public Stage primaryStage;

    // Table with list of words
    public TableView wordsListTableView;
    private ObservableList<Word> wordsObservableList;
    TableColumn<Word, String> wordColumn;

    // Table with list of books
    public TableView booksListTableView;
    private ObservableList<Book> booksObservableList;

    // Table with search history
    public TableView lookUpsListTableView;
    private ObservableList<LookUp> lookUpsObservableList;

    // VBox which stores all charts and diagrams with statistics for currently open file
    public VBox statsVBox;

    // Menu element that contains available languages codes for opened file
    public Menu languageMenu;

    // Helper class that allows to get and edit data from .db file
    private SQLHelper sqlHelper;
    // Helper class providing statistics to be displayed for user
    private StatsHelper statsHelper;
    // Helper class providing methods to maintain consistency of data and improving readability of entries
    private CleanUpHelper cleanUpHelper;
    // Helper class providing methods to export collected data to various formats
    private ExportHelper exportHelper;
    // Helper class providing methods that allow translate words using translate API
    private TranslateHelper translateHelper;

    private String localLanguage = System.getProperty("user.language").toLowerCase();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        wordColumn = new TableColumn<Word, String>("Word");
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));
        wordColumn.setMinWidth(150);
        setupWordColumn(); // allows editing specified columns
        setWordsListTableEditable(); // make word table editable

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
        lookUpsColumn3.setMinWidth(250);

        TableColumn<Boolean, LookUp> lookUpsColumn4 = new TableColumn<>("Delete");
        lookUpsColumn4.setCellValueFactory(new PropertyValueFactory<Boolean, LookUp>("delete"));
        lookUpsColumn4.setCellFactory(tc -> new CheckBoxTableCell<Boolean, LookUp>());

        lookUpsListTableView.setEditable(true);

        lookUpsListTableView.getColumns().add(lookUpsColumn1);
        lookUpsListTableView.getColumns().add(lookUpsColumn2);
        lookUpsListTableView.getColumns().add(lookUpsColumn3);
        lookUpsListTableView.getColumns().add(lookUpsColumn4);

        exportHelper = new ExportHelper();
        translateHelper = new TranslateHelper();
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
            // Create new instance of CleanUpHelper for selected file
            cleanUpHelper = new CleanUpHelper(sqlHelper);
            // Create new instance of StatsHelper for selected file
            statsHelper = new StatsHelper();

            // Creates menu items in languageMenu from language list created from opened file
            createLanguageMenuItems(sqlHelper.getCurrentLanguageFilters());

            // Get data from database
            wordsObservableList = FXCollections.observableList(sqlHelper.getWords());
            booksObservableList = FXCollections.observableList(sqlHelper.getBooks());
            lookUpsObservableList = FXCollections.observableList(sqlHelper.getLookUps());

            // Fill tables with data
            wordsListTableView.setItems(wordsObservableList);
            booksListTableView.setItems(booksObservableList);
            lookUpsListTableView.setItems(lookUpsObservableList);

            // Refresh statistics for just loaded data
            refreshStats();
        }
    }
    // Action saves all changes to currently opened file
    public void saveAction(ActionEvent actionEvent) {
        if (sqlHelper != null && sqlHelper.hasUnsavedChanges) {
            sqlHelper.commit();
        }
    }
    // Action creates new database file and saves to user specified location.
    public void saveAsAction(ActionEvent actionEvent) {
        if (sqlHelper == null)
            return;

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SQLite database files (*.db)", "*.db");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if(file != null) {
            // Export database to file
            sqlHelper.exportDatabase(file.getAbsolutePath());
        }
    }
    // Action exports data from currently open file to CSV file.
    public void exportToCSVAction(ActionEvent actionEvent) {
        if (wordsObservableList == null)
            return;

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(" (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            translateWords();
            exportHelper.exportToCSV(wordsObservableList, file);
        }
    }
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
                if (type.isEmpty())
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

    public void removeSelectedWords(ActionEvent actionEvent) {
    }

    public void removeSelectedLookUps(ActionEvent actionEvent) {
        for (LookUp l : lookUpsObservableList) {
            if (l.isDelete())
                sqlHelper.removeLookUp(l.id);
        }
        lookUpsObservableList.removeIf(w -> w.isDelete());
    }

    public void openCleanUpWindow(ActionEvent actionEvent) {
        if(sqlHelper == null)
            return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("cleanUpPage.fxml"));
            Parent root1 = fxmlLoader.load();
            // New window (Stage)
            Stage newWindow = new Stage();
            newWindow.setResizable(false);
            newWindow.setScene(new Scene(root1));
            newWindow.setTitle("Clean up");
            // Specifies the modality for new window.
            newWindow.initModality(Modality.WINDOW_MODAL);
            // Specifies the owner Window (parent) for new window
            newWindow.initOwner(primaryStage);
            // Set position of second window, related to primary window.
            newWindow.setX(primaryStage.getX() + 200);
            newWindow.setY(primaryStage.getY() + 100);
            CleanUpPageController cleanUpPageController = fxmlLoader.getController();
            cleanUpPageController.setCleanUpHelper(cleanUpHelper);
            cleanUpPageController.setSQLHelper(sqlHelper);
            newWindow.showAndWait();
            // Reload data from modified sqlHelper instance and refresh stats
            refreshAllView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void translateWords() {
        wordsObservableList.forEach(word -> {
            try {
                translateHelper.translate(word,localLanguage);
            } catch (IOException e) {
                showAlertTranslateFailure();
                return;
            }
        });
    }



    private void refreshStats() {
        // Create new charts
        PieChart bookPieChart = createBooksChart();
        BarChart wordBarChart = createWordsChart();
        ScatterChart lookUpsScatterChart = createLookUpsChart();

        // Remove existing charts from view
        statsVBox.getChildren().clear();

        // Create label for each chart
        Label l1 = new Label("Frequently checked words");
        Label l2 = new Label("Most popular books");
        Label l3 = new Label("Words checked history");

        // Setting properties of each label
        l1.setFont(new Font(20));
        l1.setPadding(new Insets(30, 0, 2, 0));
        l2.setFont(new Font(20));
        l2.setPadding(new Insets(30, 0, 2, 0));
        l3.setFont(new Font(20));
        l3.setPadding(new Insets(30, 0, 2, 0));

        // Add created elements to view
        statsVBox.getChildren().add(l1);
        statsVBox.getChildren().add(wordBarChart);
        statsVBox.getChildren().add(l2);
        statsVBox.getChildren().add(bookPieChart);
        statsVBox.getChildren().add(l3);
        statsVBox.getChildren().add(lookUpsScatterChart);
    }

    private void setWordsListTableEditable() {
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
            Word w = event.getTableView().getItems()
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

    private ScatterChart createLookUpsChart() {
        // Get data to display from stats helpers
        var lookUps = statsHelper.getLookUpTimeLine(8, lookUpsObservableList);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            DateFormat dateFormat = new SimpleDateFormat("MM/yyyy");

            @Override
            public String toString(Number number) {
                return dateFormat.format(new Date(number.longValue()));
            }

            @Override
            public Number fromString(String s) {
                return null;
            }
        });
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Words checked");

        ScatterChart scatterChart = new ScatterChart(xAxis, yAxis);
        XYChart.Series dataSeries1 = new XYChart.Series();

        for (var data : lookUps) {
            dataSeries1.getData().add(data);
        }
        scatterChart.getData().add(dataSeries1);

        return scatterChart;
    }

    private PieChart createBooksChart() {
        // Get data to display from stats helpers
        var books = statsHelper.getTopBooks(3, booksObservableList);

        // Create PieChart for books
        PieChart bookPieChart = new PieChart();
        for (var book : books) {
            PieChart.Data slice = new PieChart.Data(book.getTitle(), book.getWordCount());
            bookPieChart.getData().add(slice);
        }

        return bookPieChart;
    }

    private BarChart createWordsChart() {
        // Create axis for BarChart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Words");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frequency");

        // Create BarChart with defined axis
        BarChart wordBarChart = new BarChart(xAxis, yAxis);

        // Create series: one for each language
        for(String lang : sqlHelper.getCurrentLanguageFilters()) {
            XYChart.Series dataSeries = new XYChart.Series();
            dataSeries.setName(lang.toUpperCase());

            // Get data to display from stats helpers
            List<Word> words = statsHelper.getTopWords(10, wordsObservableList, lang);
            // Add data to each dataSeries
            for (var word : words) {

                    dataSeries.getData().add(new XYChart.Data(word.getWord(), word.getCount()));
            }
            wordBarChart.getData().add(dataSeries);
        }

        return wordBarChart;
    }

    private void createLanguageMenuItems(List<String> languages) {
        // Remove existing elements
        languageMenu.getItems().clear();
        // Create custom event handler for MenuItems
        EventHandler<ActionEvent> event = e -> {
            if (((CheckMenuItem)e.getSource()).isSelected()) {
                sqlHelper.addLanguage(((CheckMenuItem)e.getSource()).getText());
            }
            else {
                sqlHelper.removeLanguage(((CheckMenuItem)e.getSource()).getText());
            }
            refreshAllView();
        };
        // Add menu elements to languageMenu
        for(var lang: languages) {
            // Create menu item
            CheckMenuItem menuitem = new CheckMenuItem(lang);
            menuitem.setSelected(true);
            // Add event
            menuitem.setOnAction(event);
            // Add to language menu
            languageMenu.getItems().add(menuitem);
        }
    }

    private void refreshAllView() {
        statsHelper = new StatsHelper();
        // Get updated data from database
        wordsObservableList = FXCollections.observableList(sqlHelper.getWords());
        booksObservableList = FXCollections.observableList(sqlHelper.getBooks());
        lookUpsObservableList = FXCollections.observableList(sqlHelper.getLookUps());
        // Show user updated data
        wordsListTableView.setItems(wordsObservableList);
        booksListTableView.setItems(booksObservableList);
        lookUpsListTableView.setItems(lookUpsObservableList);
        // Refresh statistics for just updated data
        refreshStats();
    }

    private void showAlertTranslateSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success!");
        alert.setContentText("Translated successfully.");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }

    private void showAlertTranslateFailure() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error occurred!");
        alert.setContentText("Please check your internet connection.");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
    }
}
