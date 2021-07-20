package openjfx.Controllers;

import com.twelvemonkeys.imageio.metadata.Directory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import openjfx.Exceptions.AlertPanes;
import openjfx.Models.KerasLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoadingPaneController {

    @FXML
    private Label activeWeightsFilePath;
    @FXML
    private Label activeArchitectureFilePath;
    @FXML
    private Button loadModelFullH5Button;
    @FXML
    private Button loadModelArchitectureJSONButton;
    @FXML
    private Button loadModelWeightsH5Button;
    @FXML
    private Button convertModelArchitectureFromYAMLToJSONButton;

    private final FileChooser fileChooser;
    private final DirectoryChooser directoryChooser;
    private final FileChooser.ExtensionFilter h5Filter;
    private final FileChooser.ExtensionFilter jsonFilter;
    private final FileChooser.ExtensionFilter yamlFilter;

    public LoadingPaneController() {
        fileChooser = new FileChooser();
        directoryChooser = new DirectoryChooser();
        h5Filter = new FileChooser.ExtensionFilter("H5 files (*.h5)", "*.h5");
        jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        yamlFilter = new FileChooser.ExtensionFilter("YAML files (*.yaml)", "*.yaml");

    }

    public void loadModelFullH5(ActionEvent actionEvent) {
        fileChooser.setTitle("Выберите файл с архитектурой и весами модели в формате .h5");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(h5Filter);

        File file = fileChooser.showOpenDialog(loadModelFullH5Button.getScene().getWindow());
        if(file == null){
            AlertPanes.showWarningAlertPane("Выбор файла модели для загрузки","Результат операции: неудачно","Вы закрыли панель выбора файла");
        } else{
            String filepath = file.getAbsolutePath();
            KerasLoader loader = KerasLoader.getInstance();
            loader.loadModelFullH5(filepath);
            activeArchitectureFilePath.setText(filepath);
            activeWeightsFilePath.setText(filepath);
        }
    }

    public void loadModelArchitectureJSON(ActionEvent actionEvent) {
        fileChooser.setTitle("Выберите файл с архитектурой модели в формате .json");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(jsonFilter);

        File file = fileChooser.showOpenDialog(loadModelArchitectureJSONButton.getScene().getWindow());
        if(file == null){
            AlertPanes.showWarningAlertPane("Выбор файла архитектуры модели для загрузки","Результат операции: неудачно","Вы закрыли панель выбора файла");
        } else{
            String filepath = file.getAbsolutePath();
            KerasLoader loader = KerasLoader.getInstance();
            loader.loadModelArchitectureJSON(filepath);

            activeArchitectureFilePath.setText(filepath);
            activeWeightsFilePath.setText("Empty");
        }
    }

    public void loadModelWeightsH5(ActionEvent actionEvent) {
        fileChooser.setTitle("Выберите файл с весами модели в формате .h5");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(h5Filter);

        File file = fileChooser.showOpenDialog(loadModelWeightsH5Button.getScene().getWindow());
        if(file == null){
            AlertPanes.showWarningAlertPane("Выбор файла весов модели для загрузки","Результат операции: неудачно","Вы закрыли панель выбора файла");
        } else{
            String filepath = file.getAbsolutePath();
            KerasLoader loader = KerasLoader.getInstance();
            loader.loadModelWeightsH5(filepath);

            activeWeightsFilePath.setText(filepath);
        }


    }

    public void convertModelArchitectureFromYAMLToJSON(ActionEvent actionEvent) {
        fileChooser.setTitle("Выберите файл с архитектурой модели в формате .yaml для конвертации в формат .json");
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(yamlFilter);
        directoryChooser.setTitle("Выберите директорию для сохранения файла с архитектурой модели в формате .json");

        File inputFile = fileChooser.showOpenDialog(convertModelArchitectureFromYAMLToJSONButton.getScene().getWindow());
        if(inputFile == null){
            AlertPanes.showWarningAlertPane("Выбор файла архитектуры модели для трансформации","Результат операции: неудачно","Вы закрыли панель выбора файла");
        } else{
            File outputDirectory = directoryChooser.showDialog(convertModelArchitectureFromYAMLToJSONButton.getScene().getWindow());
            if(outputDirectory == null){
                AlertPanes.showWarningAlertPane("Выбор директории для сохранения файла архитектуры","Результат операции: неудачно","Вы закрыли панель выбора директории");
            } else{
                String filepath = inputFile.getAbsolutePath();
                String directoryPath = outputDirectory.getAbsolutePath();
                KerasLoader loader = KerasLoader.getInstance();
                String architectureJSON = loader.convertModelArchitectureFromYAMLToJSON(filepath);

                LocalDateTime myDateObj = LocalDateTime.now();
                DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd•MM•yyyy HH•mm•ss");
                String formattedDate = myDateObj.format(myFormatObj);
                FileWriter file = null;
                try {
                    file = new FileWriter(directoryPath+"\\YAMLToJSON"+formattedDate+".json");
                    file.write(architectureJSON);
                    file.close();
                    AlertPanes.showInfoAlertPane("Трансформация файла архитектуры модели","Результат операции: успешно","Файл архитектуры был успешно сгенерирован");
                } catch (Exception e) {
                    AlertPanes.showErrorAlertPane("Трансформация файла архитектуры модели","Результат операции: неудачно","Ошибка генерации нового файла архитектуры", ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }
}
