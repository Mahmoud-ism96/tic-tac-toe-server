package tictactoeserver;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public  class FXMLDocumentBase extends AnchorPane {

    protected final ImageView background;
    protected final ToggleButton toggle_btn;
    protected final CategoryAxis categoryAxis;
    protected final NumberAxis numberAxis;
    protected final  StackedBarChart barChart;
    protected final Text txt_off;
    protected  ImageView view;
    protected Image img;
    protected Image img_background;
    XYChart.Series series1;
    XYChart.Series series2;
    XYChart.Series series3;
    
    public FXMLDocumentBase() {

        background = new ImageView();
        toggle_btn = new ToggleButton();
        categoryAxis = new CategoryAxis();
        numberAxis = new NumberAxis();
        barChart = new StackedBarChart(categoryAxis, numberAxis);
        txt_off = new Text();
        
        img_background=new Image("/images/background.jpg");
        
        img = new Image("/images/off.png");
        view = new ImageView(img);
        view.setFitHeight(50);
        view.setPreserveRatio(true);
 

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(500.0);
        setPrefWidth(500.0);

        AnchorPane.setBottomAnchor(background, 0.0);
        AnchorPane.setLeftAnchor(background, 0.0);
        AnchorPane.setRightAnchor(background, 0.0);
        AnchorPane.setTopAnchor(background, 0.0);
        background.setFitHeight(800.0);
        background.setFitWidth(800.0);
        background.setPickOnBounds(true);
        background.setPreserveRatio(true);
        
        background.setImage(img_background);

        toggle_btn.setLayoutX(372.0);
        toggle_btn.setLayoutY(62.0);
        toggle_btn.setMnemonicParsing(false);
        toggle_btn.setGraphic(view);
        toggle_btn.setStyle("-fx-background-color: transparent;");
        categoryAxis.setLabel("Status Of Player");
        categoryAxis.setSide(javafx.geometry.Side.BOTTOM);
        

        numberAxis.setPrefHeight(250.0);
        numberAxis.setPrefWidth(50.0);
        numberAxis.setSide(javafx.geometry.Side.LEFT);
        numberAxis.setLabel("Count");
       
      
        barChart.setCategoryGap(60);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setLayoutX(28.0);
        barChart.setLayoutY(153.0);
        barChart.setPrefHeight(300.0);
        barChart.setPrefWidth(395.0);
      
        
        series1 = new XYChart.Series();
        series1.setName("Users");  
        series1.getData().add(new XYChart.Data("Users", 100));
        
        series2 = new XYChart.Series();
        series2.setName("Online");  
        series2.getData().add(new XYChart.Data("offline", 50));
        
        series3 = new XYChart.Series();
        series3.setName("Offline");  
        series3.getData().add(new XYChart.Data("online", 50));
        
        barChart.getData().addAll(series1,series2,series3);
        barChart.setVisible(false);

        txt_off.setLayoutX(150.0);
        txt_off.setLayoutY(250.0);
        txt_off.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        txt_off.setStrokeWidth(0.0);
        txt_off.setText("Server is Offline");
        txt_off.setStyle("-fx-font-size: 30; -fx-font-weight: bold;");
        txt_off.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        getChildren().add(background);
        getChildren().add(toggle_btn);
        getChildren().add(barChart);
        getChildren().add(txt_off);
        
        toggle_btn.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               toggle_isSelected();
            }
        });
        

    }
    public void toggle_isSelected(){
        if(toggle_btn.isSelected()){
             img=new Image("/images/on.png");
                view=new ImageView(img);
                view.setFitHeight(50);
                view.setPreserveRatio(true);
                toggle_btn.setGraphic(view);
                txt_off.setVisible(false);
                barChart.setVisible(true);
        }else{
             img=new Image("/images/off.png");
                view=new ImageView(img);
                view.setFitHeight(50);
                view.setPreserveRatio(true);
                toggle_btn.setGraphic(view);
                txt_off.setVisible(true);
                barChart.setVisible(false);
        }
    }

    
}
