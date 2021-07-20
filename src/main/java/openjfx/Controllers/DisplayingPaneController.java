package openjfx.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import openjfx.Exceptions.AlertPanes;
import openjfx.Models.FrameworkChecker;
import openjfx.Models.KerasLoader;
import org.deeplearning4j.nn.layers.feedforward.autoencoder.recursive.Tree;

import java.util.List;


public class DisplayingPaneController {

    @FXML
    private TextArea displayingConfigArea;
    @FXML
    private VBox displayingSimplifiedConfigArea;
    @FXML
    private Button displayModelConfigButton;

    public void showModelConfiguration(ActionEvent actionEvent) {
        FrameworkChecker checker = FrameworkChecker.getInstance();
        switch (String.valueOf(checker.giveActiveFramework())){
            case "Keras":
                KerasLoader loader = KerasLoader.getInstance();
                drawConfigSummary(loader);
                drawSimplifiedConfig(loader);
                AlertPanes.showInfoAlertPane("Отображение конфигурации модели","Результат операции: успешно","Конфигурация модели загружена и отображена");
                break;
            case "null":
                AlertPanes.showWarningAlertPane("Отображение конфигурации модели","Результат операции: неудачно","Активная модель для отображения отсутствует");
                break;
        }
    }

    private void drawConfigSummary(KerasLoader loader){
        displayingConfigArea.clear();
        displayingConfigArea.appendText(loader.giveModelSummary());
    }

    private void drawSimplifiedConfig(KerasLoader loader){

        List<List<String>> layersInfo =loader.giveModelSimpleConfig();

        displayingSimplifiedConfigArea.getChildren().clear();

        //панель для первого слоя
        TitledPane inputLayer = new TitledPane();
        inputLayer.setText("Имя первого(начального) слоя модели: ["+layersInfo.get(0).get(0)+"]");
        VBox inputLayerContent = new VBox();
        inputLayerContent.getChildren().add(new Label("Количество параметров, которые передает пользователь: "+layersInfo.get(1).get(0)));
        inputLayerContent.getChildren().add(new Label("Количество параметров, которые слой передает дальше: "+layersInfo.get(2).get(0)));
        inputLayer.setContent(inputLayerContent);

        displayingSimplifiedConfigArea.getChildren().add(inputLayer);
        //панели скрытых слоев (если есть)
        if(layersInfo.get(0).size()>2){
            for (int i = 1; i < layersInfo.get(0).size()-1; i++) {
                TitledPane hiddenLayer = new TitledPane();
                hiddenLayer.setText("Имя промежуточного(скрытого) слоя модели: ["+layersInfo.get(0).get(i)+"]");
                VBox hiddenLayerContent = new VBox();
                hiddenLayerContent.getChildren().add(new Label("Количество параметров, которые переходят от слоя ["+layersInfo.get(0).get(i-1)+"] : "+layersInfo.get(1).get(i)));
                hiddenLayerContent.getChildren().add(new Label("Количество параметров, которые слой передает дальше: "+layersInfo.get(2).get(i)));
                hiddenLayer.setContent(hiddenLayerContent);
                displayingSimplifiedConfigArea.getChildren().addAll(hiddenLayer);
            }
        }
        //панель последнего слоя
        TitledPane resultLayer = new TitledPane();
        resultLayer.setText("Имя последнего(конечного) слоя модели: "+layersInfo.get(0).get(layersInfo.get(0).size()-1)+"]");
        VBox resultLayerContent = new VBox();
        resultLayerContent.getChildren().add(new Label("Количество параметров, которые переходят от слоя ["+layersInfo.get(0).get(layersInfo.get(0).size()-2)+"] : "+layersInfo.get(1).get(layersInfo.get(1).size()-1)));
        resultLayerContent.getChildren().add(new Label("Количество параметров, которые слой передает пользователю: "+layersInfo.get(2).get(layersInfo.get(2).size()-1)));
        resultLayer.setContent(resultLayerContent);
        displayingSimplifiedConfigArea.getChildren().addAll(resultLayer);

    }

    public void clearDisplayModelConfigArea(ActionEvent actionEvent) {
        FrameworkChecker checker = FrameworkChecker.getInstance();
        switch (String.valueOf(checker.giveActiveFramework())){
            case "Keras":
                if(AlertPanes.showConfirmAlertPane("Очистка окон отображения конфигурации","Вы уверены, что хотите очистиь эти окна?","Вы всегда можете повторно отобразить конфигурацию модели")){
                    displayingConfigArea.clear();
                    displayingSimplifiedConfigArea.getChildren().clear();
                }
                break;
            case "null":
                AlertPanes.showWarningAlertPane("Очистка окон отображения конфигурации","Результат операции: неудачно","Активная модель отсутствует");
                break;
        }
    }
}
