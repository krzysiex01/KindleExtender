package kindleExtender;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader load = new FXMLLoader(getClass().getResource("mainPage.fxml"));
        Parent root = load.load();
        MainPageController mainPageController = load.getController();
        mainPageController.primaryStage = primaryStage;

        primaryStage.setTitle("Kindle Extender");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                mainPageController.exitAction(new ActionEvent());
                event.consume();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
