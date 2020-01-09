package kindleExtender;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kindleExtender.helpers.CleanUpHelper;
import kindleExtender.helpers.SQLHelper;

public class CleanUpPageController {
    public CheckBox nonAlpCharactersCheckbox;
    public CheckBox inconsistentCheckbox;
    public CheckBox duplicatesCheckbox;

    private CleanUpHelper cleanUpHelper;

    public void setCleanUpHelper(CleanUpHelper cleanUpHelper) {
        this.cleanUpHelper = cleanUpHelper;
    }

    public void setSQLHelper(SQLHelper sqlHelper)
    {
        this.cleanUpHelper.sqlHelper = sqlHelper;
    }

    public void start(ActionEvent actionEvent) {
        int mergedDuplicatesCount = 0;
        int removedInconsistentDataCount = 0;
        int editedRowsCount = 0;
        // Preform clean-up operations selected by the user
        if (inconsistentCheckbox.isSelected()) {
            removedInconsistentDataCount = cleanUpHelper.removeInconsistentData();
        }

        if (nonAlpCharactersCheckbox.isSelected()) {
            editedRowsCount = cleanUpHelper.removeNonAlphabeticCharacters();
        }

        if (duplicatesCheckbox.isSelected()) {
            mergedDuplicatesCount = cleanUpHelper.mergeDuplicates();
        }
        // Construct summary message
        StringBuilder summaryMessage = new StringBuilder();
        summaryMessage.append("Successfully merged ");
        summaryMessage.append(mergedDuplicatesCount);
        summaryMessage.append(" duplicates.\n");
        summaryMessage.append("Successfully edited ");
        summaryMessage.append(editedRowsCount);
        summaryMessage.append(" words. \n");
        summaryMessage.append("Removed ");
        summaryMessage.append(removedInconsistentDataCount);
        summaryMessage.append(" inconsistent entries.");
        // Display clean-up summary
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Clean-up summary.");
        alert.setContentText(summaryMessage.toString());
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButton);
        alert.showAndWait();
        // Close window
        var window = ((Button)actionEvent.getSource()).getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage)window).close();
        }
    }
}
