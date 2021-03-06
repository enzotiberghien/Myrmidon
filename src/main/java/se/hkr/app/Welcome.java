package se.hkr.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;


public class Welcome extends Application {

    /**
     * Launch the Welcome page of the GUI.
     */
    @Override
    public void start(final Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Welcome.class
            .getResource("welcome-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        String css = this.getClass()
            .getResource("welcome.css")
            .toExternalForm();
        scene.getStylesheets()
            .add(css);
        Image appIcon = new Image("File:tinyicon.png");
        stage.getIcons().add(appIcon);
        stage.setTitle("Myrmidon");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Start the application by launching the GUI.
     * @param args
     */
    public static void main(final String[] args) {
        launch();
    }
}
