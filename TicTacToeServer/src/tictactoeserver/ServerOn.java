package tictactoeserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ServerOn extends AnchorPane {

    protected final ImageView background;
    protected final ImageView view;
    protected final ToggleButton toggle_btn;
    protected final Text txt_off;
    protected final CategoryAxis categoryAxis;
    protected final NumberAxis numberAxis;
    protected final BarChart barChart;

    protected Image server_status;
    protected Image back_img;

    ServerSocket serverSocket;
    protected Socket client;
    DataInputStream dis;
    PrintStream ps;

    public ServerOn() {

        view = new ImageView();
        background = new ImageView();
        toggle_btn = new ToggleButton();
        txt_off = new Text();
        categoryAxis = new CategoryAxis();
        numberAxis = new NumberAxis();
        barChart = new BarChart(categoryAxis, numberAxis);

        server_status = new Image("/images/off.png");
        back_img = new Image("/images/background.jpg");
        view.setImage(server_status);
        background.setImage(back_img);

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(500.0);
        setPrefWidth(500.0);

        AnchorPane.setBottomAnchor(background, -284.0);
        AnchorPane.setLeftAnchor(background, 0.0);
        AnchorPane.setRightAnchor(background, 0.0);
        AnchorPane.setTopAnchor(background, -16.0);

        background.setFitHeight(900.0);
        background.setFitWidth(800.0);
        background.setLayoutY(-16.0);
        background.setPickOnBounds(true);
        background.setPreserveRatio(true);

        view.setFitHeight(50);
        view.setFitWidth(50);
        toggle_btn.setLayoutX(372.0);
        toggle_btn.setLayoutY(62.0);
        toggle_btn.setMnemonicParsing(false);
        toggle_btn.setGraphic(view);
        toggle_btn.setStyle("-fx-background-color: transparent;");

        txt_off.setLayoutX(150.0);
        txt_off.setLayoutY(250.0);
        txt_off.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        txt_off.setStrokeWidth(0.0);
        txt_off.setStyle("-fx-font-size: 30; -fx-font-weight: bold;");
        txt_off.setText("Server is Offline");
        txt_off.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        categoryAxis.setSide(javafx.geometry.Side.BOTTOM);
        categoryAxis.setLabel("Player Status");

        numberAxis.setSide(javafx.geometry.Side.LEFT);
        numberAxis.setLabel("Count");

        barChart.setLayoutX(28.0);
        barChart.setLayoutY(169.0);
        barChart.setPrefHeight(289.0);
        barChart.setPrefWidth(444.0);
        barChart.setTitle("Player Status");
        barChart.setVisible(false);

        getChildren().add(background);
        getChildren().add(toggle_btn);
        getChildren().add(txt_off);
        getChildren().add(barChart);

        toggle_btn.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                toggle_isSelected();

            }

            public void toggle_isSelected() {
                if (toggle_btn.isSelected()) {
                    server_status = new Image("/images/on.png");
                    view.setImage(server_status);
                    view.setFitHeight(50);
                    view.setPreserveRatio(true);
                    toggle_btn.setGraphic(view);
                    txt_off.setVisible(false);
                    barChart.setVisible(true);
                    draw();
                    server_on();

                } else {
                    server_status = new Image("/images/off.png");
                    view.setImage(server_status);
                    view.setFitHeight(50);
                    view.setPreserveRatio(true);
                    toggle_btn.setGraphic(view);
                    txt_off.setVisible(true);
                    barChart.setVisible(false);
                }
            }

            public void draw() {
                try {
                    int users = DataAccessLayer.getInstance().getAllplayerList();
                    int online = DataAccessLayer.getInstance().getOnlineplayerList();
                    int offline = users - online;
                    ObservableList<XYChart.Series<String, Number>> barChartData = FXCollections.observableArrayList(
                            new XYChart.Series<>("Online", FXCollections.observableArrayList(
                                    new XYChart.Data<>("online", online)
                            )),
                            new XYChart.Series<>("Offline", FXCollections.observableArrayList(
                                    new XYChart.Data<>("offline", offline)
                            ))
                    );

                    barChart.setData(barChartData);
                } catch (SQLException ex) {
                    Logger.getLogger(ServerOn.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            public void server_on() {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Timeline timeLine = new Timeline(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                draw();
                            }

                        }));
                        timeLine.setCycleCount(Timeline.INDEFINITE);
                        timeLine.play();
                    }
                });

                try {
                    serverSocket = new ServerSocket(5005);
                    new Thread() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    client = serverSocket.accept();
                                    new RequestHandler(client);
                                } catch (IOException ex) {
                                    try {
                                        if (client != null) {
                                            client.close();
                                        }
                                    } catch (IOException ex1) {
                                        Logger.getLogger(ServerOn.class.getName()).log(Level.SEVERE, null, ex1);
                                    }
                                }
                            }
                        }
                    }.start();
                } catch (IOException ex) {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch (IOException ex1) {
                        Logger.getLogger(ServerOn.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

            }
        });

    }

}
