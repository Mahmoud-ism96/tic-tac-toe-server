/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoeserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 *
 * @author HP
 */
public class TicTacToeServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(TicTacToeServer.class.getResource("/fonts/Stroke-082d.ttf").toExternalForm(), 10);
        Parent root = new FXMLDocumentBase();
        root.setStyle("-fx-font-family: Stroke;");

        Scene scene = new Scene(root);

        stage.setOnCloseRequest(event -> {
            FXMLDocumentBase.isRunning = false;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FXMLDocumentBase.isRunning = false;
        }));

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (FXMLDocumentBase.serverSocket != null) {
            FXMLDocumentBase.serverSocket.close();
        }
        if (FXMLDocumentBase.client != null) {
            FXMLDocumentBase.client.close();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
