package openjfx.Controllers;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import openjfx.Exceptions.AlertPanes;
import openjfx.Models.FrameworkChecker;
import openjfx.Models.KerasLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InteractingPaneController {

    //Загрузить/////////////////////////////////////////////////////////////////////////////////
    @FXML
    private Button LoadCSVDataButton;
    private FileChooser fileChooser;
        private DirectoryChooser directoryChooser;
    //Показать/////////////////////////////////////////////////////////////////////////////////
    @FXML
    private TableView inputTableView;
    private List<String> fileColumns;
    private List<List<String>> fileData;
    private List<List<String>> resultData;
    private List<Integer> fileEmptyColumnsIndexes;
    //Нормализация/////////////////////////////////////////////////////////////////////////////////
    @FXML
    private ComboBox normalizationColumnComboBox;
    private Integer normalizationFileColumnIndex;
    @FXML
    private CheckBox manualNormalizationCheckBox;
    @FXML
    private CheckBox automaticNormalizationCheckBox;
    @FXML
    private TextField minValueField;
    private Double minNormalizeValue;
    @FXML
    private TextField maxValueField;
    private Double maxNormalizeValue;
    @FXML
    private Button normalizeButton;
    @FXML
    private Button cancelNormalizationButton;
    //Математика/////////////////////////////////////////////////////////////////////////////////
    @FXML
    private ComboBox typeOfOperationComboBox;
    @FXML
    private ComboBox mathMainColumnComboBox;
    @FXML
    private ComboBox mathAdditionalColumnComboBox;
    @FXML
    private CheckBox manualMathValueCheckBox;
    @FXML
    private CheckBox columnMathValueCheckBox;
    @FXML
    private TextField manualMathValueField;
    @FXML
    private Button mathButton;
    @FXML
    private Button cancelMathButton;
    private String typeOfMathOperation;
    private Integer mathMainFileColumnIndex;
    private Integer mathAdditionalFileColumnIndex;
    private Double manualMathFieldValue;
    private Boolean withResult;
    //Сохранить/////////////////////////////////////////////////////////////////////////////////
    @FXML
    private Button saveCSVDataButton;
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    //Загрузить/////////////////////////////////////////////////////////////////////////////////
    public InteractingPaneController() {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите директорию для сохранения файла с данными в формате .csv");
        fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с данными в формате .csv");
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);
    }
    public void LoadCSVData(ActionEvent actionEvent) {

        File file = fileChooser.showOpenDialog(LoadCSVDataButton.getScene().getWindow());
        if(file == null){
            AlertPanes.showWarningAlertPane("Выбор файла данных для загрузки","Результат операции: неудачно","Вы закрыли панель выбора файла");
        } else{
            //путь до csv файла
            String filepath = file.getAbsolutePath();
            try {
                //новый ридер
                CSVReader reader = new CSVReader(new FileReader(filepath));
                try {
                    //читаю с ридера. делай листу с колонками и листу с листами данных
                    List<String[]> list = reader.readAll();
                    fileData = new ArrayList<>();
                    fileColumns = new ArrayList<String>(Arrays.asList(list.remove(0)));
                    list.forEach(strings -> fileData.add(Arrays.asList(strings)));
                    displayDataInTable(fileData, fileColumns);
                    AlertPanes.showInfoAlertPane("Загрузка файла данных","Результат операции: успешно","Данные были успешно загружены");
                } catch (Exception e) {
                    AlertPanes.showErrorAlertPane("Загрузка файла данных","Результат операции: неудачно","Ошибка при чтении файла",ExceptionUtils.getStackTrace(e));
                }
            } catch (Exception e) {
                AlertPanes.showErrorAlertPane("Загрузка файла данных","Результат операции: неудачно","Неверный тип расширения файла", ExceptionUtils.getStackTrace(e));
            }
        }


    }
    //Показать/////////////////////////////////////////////////////////////////////////////////
    private void displayDataInTable(List<List<String>> fileData, List<String> fileColumns){
        //генерю под листу с колонками колонки в таблице
        for (int i = 0; i < fileColumns.size(); i++) {
            final int j = i;
            TableColumn column = new TableColumn(fileColumns.get(i));

            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>, ObservableValue<String>>(){
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });
            //добавляю все колонки в таблицу
            inputTableView.getColumns().addAll(column);
        }
        ObservableList<ObservableList> displayFileData = FXCollections.observableArrayList();
        for (int i = 0; i < fileData.size(); i++) {
            displayFileData.add(FXCollections.observableArrayList(fileData.get(i)));
        }
        //добавляю все данные в таблицу
        inputTableView.setItems(displayFileData);
        //отображаю выбор в колонке нормализации
        displayDataInNormalizationComboBox(fileColumns);
        //отображаю выбор в колонке математики
        displayDataInMathComboBox(fileColumns);
    }
    public void calculateResult(ActionEvent actionEvent) {
        //получаю активный фреймворк
        FrameworkChecker checker = FrameworkChecker.getInstance();
        switch (String.valueOf(checker.giveActiveFramework())){
            case "Keras":
                if(AlertPanes.showConfirmAlertPane("Вычисление результатов модели","Вы уверены, что готовы передать загруженные данные модели?","Вы можете отказаться от этой операции")){
                    //делаю копию данных
                    List<List<String>> dataForCalculation = copyFileData(fileData);
                    KerasLoader loader = KerasLoader.getInstance();
                    //кидаю копию и получаю результат от модельки
                    resultData = loader.feedModel(dataForCalculation);
                    for (int i = 0; i < resultData.size(); i++) {
                        resultData.get(i).addAll(0, dataForCalculation.get(i));
                    }
                    AlertPanes.showInfoAlertPane("Вычисление результатов модели","Результат операции: успешно","Результаты были успешно получены");
                    //отрисовываю с результатом
                    inputTableView.getItems().clear();
                    inputTableView.getColumns().clear();
                    displayDataInTable(resultData, fileColumns);
                }
                break;
            case "null":
                AlertPanes.showWarningAlertPane("Вычисление результатов модели","Результат операции: неудачно","Активная модель для вычислений отсутствует");
                break;
        }
    }
    private List<List<String>> copyFileData(List<List<String>> fileData){
        //делаю копию инфы ибо после возвращения из модели невозможно было норм отрисовать заново
        List<List<String>> dataForCalculation = new ArrayList<>();
        for (int i = 0; i < fileData.size(); i++) {
            dataForCalculation.add(new ArrayList<String>());
        }
        for (int i = 0; i < fileData.size(); i++) {
            for (int j = 0; j < fileData.get(i).size(); j++) {
                if(!fileData.get(i).get(j).isEmpty())
                    dataForCalculation.get(i).add(fileData.get(i).get(j));
            }
        }
        return dataForCalculation;
    }
    public void clearCSVData(ActionEvent actionEvent) {
        if((fileData != null) | (resultData != null)){
            if(AlertPanes.showConfirmAlertPane("Очистка окон отображения данных","Вы уверены, что хотите очистиь эти окна?","Вы всегда можете повторно загрузить файл с данными")){
                //Таблица/////////////////////////////////////////////
                fileData = null;
                resultData = null;
                fileColumns = null;
                inputTableView.getItems().clear();
                inputTableView.getColumns().clear();
                //Нормализация/////////////////////////////////////////////
                //очистить переменные
                minNormalizeValue = null;
                maxNormalizeValue = null;
                normalizationFileColumnIndex = null;
                //очистить и отключить поля для min/max
                minValueField.setDisable(true);
                minValueField.setText("");
                maxValueField.setDisable(true);
                maxValueField.setText("");
                //отменить и отключить чекеры
                manualNormalizationCheckBox.setSelected(false);
                manualNormalizationCheckBox.setDisable(true);
                automaticNormalizationCheckBox.setSelected(false);
                automaticNormalizationCheckBox.setDisable(true);
                //отключить кнопки
                normalizeButton.setDisable(true);
                cancelNormalizationButton.setDisable(true);
                //Математика/////////////////////////////////////////////
                //очистить переменные
                typeOfMathOperation = null;
                mathMainFileColumnIndex = null;
                mathAdditionalFileColumnIndex = null;
                manualMathFieldValue = null;
                //отключить и отменить чекеры
                manualMathValueCheckBox.setSelected(false);
                columnMathValueCheckBox.setSelected(false);
                manualMathValueField.setDisable(true);
                manualMathValueField.setText("");
            }
        }
        else{
            AlertPanes.showWarningAlertPane("Очистка окон отображения данных","Результат операции: неудачно","Нет загруженных данных");
        }
    }
    //Нормализация/////////////////////////////////////////////////////////////////////////////////
    private void displayDataInNormalizationComboBox(List<String> fileColumns){
        //очищаю старые варианты
        normalizationColumnComboBox.getItems().clear();
        //делаю листу для индексов null-овых шапок
        fileEmptyColumnsIndexes = new ArrayList<>();
        //делаю локальную копию данных с листы шапок
        List<String> localFileColumns = new ArrayList<>();
        localFileColumns.addAll(fileColumns);
        //нахожу null-овые шапки
        for (int i = 0; i < fileData.get(0).size(); i++) {
            if(fileData.get(0).get(i).isEmpty())
                fileEmptyColumnsIndexes.add(0,i);
        }
        //обрезаю null-овые шапки
        fileEmptyColumnsIndexes.forEach(integer -> {
            int index = integer;
            localFileColumns.remove(index);
        });
        //подаю оставшееся в combobox нормализации
        normalizationColumnComboBox.getItems().addAll(localFileColumns);
        normalizationColumnComboBox.setDisable(false);
    }
    public void saveChosenNormalizationColumnIndex(ActionEvent actionEvent) {
        //сохраняю индекс шапки для нормализации
        normalizationFileColumnIndex = normalizationColumnComboBox.getSelectionModel().getSelectedIndex();
        manualNormalizationCheckBox.setDisable(false);
        automaticNormalizationCheckBox.setDisable(false);
        normalizeButton.setDisable(false);
        cancelNormalizationButton.setDisable(false);
    }
    public void manualSelected(ActionEvent actionEvent) {
        //разблокирую поля для ввода мин/макс
        if(manualNormalizationCheckBox.isSelected()){
            automaticNormalizationCheckBox.setSelected(false);
            minValueField.setDisable(false);
            maxValueField.setDisable(false);
        }
    }
    public void automaticSelected(ActionEvent actionEvent) {
        //блокирую поля для ввода мин/макс
        if(automaticNormalizationCheckBox.isSelected()){
            manualNormalizationCheckBox.setSelected(false);
            minValueField.setDisable(true);
            minValueField.setText("");
            maxValueField.setDisable(true);
            maxValueField.setText("");
        }
    }
    public void normalizeData(ActionEvent actionEvent) {
        if(AlertPanes.showConfirmAlertPane("Нормализация сегмента данных","Вы уверены, что хотите нормализовать выбранный сегмент?","Вы можете отказаться от этой операции")){
            //если выбрано вручную а поля пуcтые, делаю их 0 и 100
            if(manualNormalizationCheckBox.isSelected()){
                if(minValueField.getText().equals("")){
                    minNormalizeValue = 0.0;
                } else{
                    minNormalizeValue = Double.valueOf(minValueField.getText());
                }
                if(maxValueField.getText().equals("")){
                    maxNormalizeValue = 100.0;
                } else{
                    maxNormalizeValue = Double.valueOf(maxValueField.getText());
                }
            }
            //если выбрано автоматически, нахожу min/max в данных колонки
            else if (automaticNormalizationCheckBox.isSelected()){
                findMinMax(fileData, normalizationFileColumnIndex);
            }
            //делаю массив doubles с данными выбранной колонки
            Double[] doubles = new Double[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                doubles[i] = Double.valueOf(fileData.get(i).get(normalizationFileColumnIndex));
            }
            //прохожу формулой по массиву и преобразовываю
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = ((doubles[i] - minNormalizeValue) / (maxNormalizeValue - minNormalizeValue));
            }
            //замещаю данные в массиве для отрисовки
            for (int i = 0; i < fileData.size(); i++) {
                fileData.get(i).set(normalizationFileColumnIndex, String.valueOf(doubles[i]));
            }
            AlertPanes.showInfoAlertPane("Нормализация сегмента данных","Результат операции: успешно","Данные были успешно нормализованы", "сегмент: "+fileColumns.get(normalizationFileColumnIndex)+"\nоперация: нормализация\nминимальное значение: "+minNormalizeValue+"->0\nмаксимальное значение: "+maxNormalizeValue+"->1");
            //повторная отрисовка
            inputTableView.getItems().clear();
            inputTableView.getColumns().clear();
            displayDataInTable(fileData, fileColumns);

        }
    }
    private void findMinMax(List<List<String>> fileData,Integer fileColumnIndex){
        //за основу беру первый элемент массива. потом сравниваю остальные
        minNormalizeValue = Double.valueOf(fileData.get(0).get(fileColumnIndex));
        maxNormalizeValue = Double.valueOf(fileData.get(0).get(fileColumnIndex));
        for (int i = 1; i < fileData.size(); i++) {
            minNormalizeValue = Double.valueOf(fileData.get(i).get(fileColumnIndex)) < minNormalizeValue ?
                    Double.valueOf(fileData.get(i).get(fileColumnIndex)) : minNormalizeValue;
            maxNormalizeValue = Double.valueOf(fileData.get(i).get(fileColumnIndex)) > maxNormalizeValue ?
                    Double.valueOf(fileData.get(i).get(fileColumnIndex)) : maxNormalizeValue;
        }

    }
    public void cancelNormalization(ActionEvent actionEvent) {
        if(AlertPanes.showConfirmAlertPane("Очистка параметров для нормализации","Вы уверены, что хотите очистиь эти параметры?","Вы всегда можете повторно выбрать нужные параметры")){
            //очистить переменные
            minNormalizeValue = null;
            maxNormalizeValue = null;
            normalizationFileColumnIndex = null;
            //очистить и отключить поля для min/max
            minValueField.setDisable(true);
            minValueField.setText("");
            maxValueField.setDisable(true);
            maxValueField.setText("");
            //отменить и отключить чекеры
            manualNormalizationCheckBox.setSelected(false);
            manualNormalizationCheckBox.setDisable(true);
            automaticNormalizationCheckBox.setSelected(false);
            automaticNormalizationCheckBox.setDisable(true);
            //отключить кнопки
            normalizeButton.setDisable(true);
            cancelNormalizationButton.setDisable(true);
            //заново включить combobox и внести в него колонки
            displayDataInNormalizationComboBox(fileColumns);
        }
    }
    //Математика/////////////////////////////////////////////////////////////////////////////////
    private void displayDataInMathComboBox(List<String> fileColumns){
        //очищаю старые варианты
        typeOfOperationComboBox.getItems().clear();
        mathMainColumnComboBox.getItems().clear();
        mathAdditionalColumnComboBox.getItems().clear();
        //добавляю типы операций в математику
        typeOfOperationComboBox.getItems().clear();
        typeOfOperationComboBox.getItems().addAll("прибавить","вычесть","умножить","поделить");
        typeOfOperationComboBox.setDisable(false);
        //делаю локальную копию данных с листы шапок
        List<String> localFileColumns = new ArrayList<>();
        localFileColumns.addAll(fileColumns);
        //если результатов из модели нет, не даю с ними работать
        if(resultData == null){
            //делаю флаг, чтобы читать не из результата
            withResult = false;
            //делаю листу для индексов null-овых шапок
            fileEmptyColumnsIndexes = new ArrayList<>();
            //нахожу null-овые шапки
            for (int i = 0; i < fileData.get(0).size(); i++) {
                if(fileData.get(0).get(i).isEmpty())
                    fileEmptyColumnsIndexes.add(0,i);
            }
            //обрезаю null-овые шапки
            fileEmptyColumnsIndexes.forEach(integer -> {
                int index = integer;
                localFileColumns.remove(index);
            });
            //подаю оставшееся в combobox
            mathMainColumnComboBox.getItems().addAll(localFileColumns);
            mathAdditionalColumnComboBox.getItems().addAll(localFileColumns);
            mathMainColumnComboBox.setDisable(false);
            cancelMathButton.setDisable(false);
        }
        //если результаты из модели есть, то даю с ними работать
        else {
            //делаю флаг, чтобы читать из результата
            withResult = true;
            //подаю все в combobox
            mathMainColumnComboBox.getItems().addAll(localFileColumns);
            mathAdditionalColumnComboBox.getItems().addAll(localFileColumns);
            mathMainColumnComboBox.setDisable(false);
            cancelMathButton.setDisable(false);
        }
    }
    public void saveChosenTypeOfOperation(ActionEvent actionEvent) {
        //сохраняю стрингу с типом операции
        switch (typeOfOperationComboBox.getSelectionModel().getSelectedIndex()){
            case 0:
                typeOfMathOperation = "add";
                break;
            case 1:
                typeOfMathOperation = "subtract";
                break;
            case 2:
                typeOfMathOperation = "multiply";
                break;
            case 3:
                typeOfMathOperation = "divide";
                break;
        }
    }
    public void saveMainMathColumnIndex(ActionEvent actionEvent) {
        //сохраняю индекс главной колонки операции
        mathMainFileColumnIndex = mathMainColumnComboBox.getSelectionModel().getSelectedIndex();
        //System.out.println("main"+mathMainFileColumnIndex);
        manualMathValueCheckBox.setDisable(false);
        columnMathValueCheckBox.setDisable(false);
        mathButton.setDisable(false);
    }
    public void manualMathValueSelected(ActionEvent actionEvent) {
        //если выбираю ручками значение, то блокирую выбор второй колонки
        if(manualMathValueCheckBox.isSelected()){
            columnMathValueCheckBox.setSelected(false);
            mathAdditionalColumnComboBox.setDisable(true);
            manualMathValueField.setDisable(false);
        }
    }
    public void columnMathValueSelected(ActionEvent actionEvent) {
        //если выбираю вторую колонку, то блокирую поле для ручного значения
        if(columnMathValueCheckBox.isSelected()){
            manualMathValueCheckBox.setSelected(false);
            mathAdditionalColumnComboBox.setDisable(false);
            manualMathValueField.setDisable(true);
            manualMathValueField.setText("");
        }
    }
    public void saveAdditionalMathColumnIndex(ActionEvent actionEvent) {
        //если выбрал вторую колонку, то сохраняю ее индекс
        mathAdditionalFileColumnIndex = mathAdditionalColumnComboBox.getSelectionModel().getSelectedIndex();
        //System.out.println("add"+mathAdditionalFileColumnIndex);
    }
    public void doMath(ActionEvent actionEvent) {
        if(AlertPanes.showConfirmAlertPane("Изменение данных","Вы уверены, что изменить выбранный сегмент данных?","Вы можете отказаться от этой операции")){
            //если решил ввести значение ручками
            if(manualMathValueCheckBox.isSelected()){
                //если ничего не поставил в поле, то сделаю 1
                if(manualMathValueField.getText().equals("")){
                    manualMathFieldValue = 1.0;
                }
                //если поставил, то молодец
                else{
                    manualMathFieldValue = Double.valueOf(manualMathValueField.getText());
                }
                //вызываю метод при одной колонке
                calculateMath(typeOfMathOperation,mathMainFileColumnIndex,manualMathFieldValue);
            }
            //если решил использовать другую колонку
            else if( columnMathValueCheckBox.isSelected()){
                //вызываю метод при двух колонках
                calculateMath(typeOfMathOperation,mathMainFileColumnIndex,mathAdditionalFileColumnIndex);
            }
        }
    }
    private void calculateMath(String typeOfMathOperation,Integer mathMainFileColumnIndex, Integer mathAdditionalFileColumnIndex){
        Double[] main;
        Double[] additional;
        //делаю массивы из результата
        if(withResult){
            main = new Double[resultData.size()];
            additional = new Double[resultData.size()];
            for (int i = 0; i < resultData.size(); i++) {
                main[i] = Double.valueOf(resultData.get(i).get(mathMainFileColumnIndex));
                additional[i] = Double.valueOf(resultData.get(i).get(mathAdditionalFileColumnIndex));
            }
            //System.out.println(Arrays.asList(main));
            //System.out.println(Arrays.asList(additional));
        }
        //делаю массивы из файлика
        else{
            main = new Double[fileData.size()];
            additional = new Double[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                main[i] = Double.valueOf(fileData.get(i).get(mathMainFileColumnIndex));
                additional[i] = Double.valueOf(fileData.get(i).get(mathAdditionalFileColumnIndex));
            }
            //System.out.println(Arrays.asList(main));
            //System.out.println(Arrays.asList(additional));
        }
        //разделяю по типу операции
        switch (typeOfMathOperation){
            case "add":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]+additional[i];
                }
                break;
            case "subtract":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]-additional[i];
                }
                break;
            case "multiply":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]*additional[i];
                }
                break;
            case "divide":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]/additional[i];
                }
                break;
        }
        AlertPanes.showInfoAlertPane("Изменение сегмента данных","Результат операции: успешно","Данные были успешно изменены", "сегмент: "+fileColumns.get(mathMainFileColumnIndex)+"\nоперация: "+typeOfMathOperation+"\nзначения для операции из сегмента: "+fileColumns.get(mathAdditionalFileColumnIndex));
        //пихаю в массив из результата
        if(withResult){
            for (int i = 0; i < resultData.size(); i++) {
                resultData.get(i).set(mathMainFileColumnIndex, String.valueOf(main[i]));
                //заново отрисовываю
                inputTableView.getItems().clear();
                inputTableView.getColumns().clear();
                displayDataInTable(resultData, fileColumns);
            }
        }
        //пихаю в массив из файлика
        else{
            for (int i = 0; i < fileData.size(); i++) {
                fileData.get(i).set(mathMainFileColumnIndex, String.valueOf(main[i]));
                //заново отрисовываю
                inputTableView.getItems().clear();
                inputTableView.getColumns().clear();
                displayDataInTable(fileData, fileColumns);
            }
        }

    }
    private void calculateMath(String typeOfMathOperation,Integer mathMainFileColumnIndex, Double manualValue){
        Double[] main;
        //делаю массив из результата
        if(withResult){
            main = new Double[resultData.size()];
            for (int i = 0; i < resultData.size(); i++) {
                main[i] = Double.valueOf(resultData.get(i).get(mathMainFileColumnIndex));
            }
        }
        //делаю массив из файлика
        else{
            main = new Double[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                main[i] = Double.valueOf(fileData.get(i).get(mathMainFileColumnIndex));
            }
        }
        //разделяю по типу операции
        switch (typeOfMathOperation){
            case "add":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]+manualValue;
                }
                break;
            case "subtract":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]-manualValue;
                }
                break;
            case "multiply":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]*manualValue;
                }
                break;
            case "divide":
                for (int i = 0; i < main.length; i++) {
                    main[i] = main[i]/manualValue;
                }
                break;
        }
        AlertPanes.showInfoAlertPane("Изменение сегмента данных","Результат операции: успешно","Данные были успешно изменены", "сегмент: "+fileColumns.get(mathMainFileColumnIndex)+"\nоперация: "+typeOfMathOperation+"\nзначение для операции: "+manualMathFieldValue);
        //пихаю в массив из результата
        if(withResult){
            for (int i = 0; i < resultData.size(); i++) {
                resultData.get(i).set(mathMainFileColumnIndex, String.valueOf(main[i]));
                //заново отрисовываю
                inputTableView.getItems().clear();
                inputTableView.getColumns().clear();
                displayDataInTable(resultData, fileColumns);
            }
        }
        //пихаю в массив из файлика
        else{
            for (int i = 0; i < fileData.size(); i++) {
                fileData.get(i).set(mathMainFileColumnIndex, String.valueOf(main[i]));
                //заново отрисовываю
                inputTableView.getItems().clear();
                inputTableView.getColumns().clear();
                displayDataInTable(fileData, fileColumns);
            }
        }
    }
    public void cancelMath(ActionEvent actionEvent) {
        if(AlertPanes.showConfirmAlertPane("Очистка параметров для изменения данных","Вы уверены, что хотите очистиь эти параметры?","Вы всегда можете повторно выбрать нужные параметры")){
            //очистить переменные
            typeOfMathOperation = null;
            mathMainFileColumnIndex = null;
            mathAdditionalFileColumnIndex = null;
            manualMathFieldValue = null;
            //отключить и отменить чекеры
            manualMathValueCheckBox.setSelected(false);
            columnMathValueCheckBox.setSelected(false);
            manualMathValueField.setDisable(true);
            manualMathValueField.setText("");

            displayDataInMathComboBox(fileColumns);
        }
    }
    //Сохранить/////////////////////////////////////////////////////////////////////////////////
    public void saveCSVData(ActionEvent actionEvent) {

        File outputDirectory = directoryChooser.showDialog(saveCSVDataButton.getScene().getWindow());
        if(outputDirectory == null){
            AlertPanes.showWarningAlertPane("Выбор директории для сохранения файла с результатами","Результат операции: неудачно","Вы закрыли панель выбора директории");
        } else{
            String directoryPath = outputDirectory.getAbsolutePath();
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd•MM•yyyy HH•mm•ss");
            String formattedDate = myDateObj.format(myFormatObj);
            try {
                CSVWriter csvWriter = new CSVWriter(new FileWriter(directoryPath+"\\ModelResult"+formattedDate+".csv"));
                csvWriter.writeNext(fileColumns.toArray(new String[fileColumns.size()]));
                resultData.forEach(strings -> {
                    csvWriter.writeNext(strings.toArray(new String[strings.size()]));
                });
                csvWriter.close();
                AlertPanes.showInfoAlertPane("Сохранение результатов модели","Результат операции: успешно","Файл результатов был успешно сгенерирован");
            } catch (Exception e) {
                AlertPanes.showErrorAlertPane("Сохранение результатов модели","Результат операции: неудачно","Ошибка генерации файла результатов", ExceptionUtils.getStackTrace(e));
            }
        }
    }
}
