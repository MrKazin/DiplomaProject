package openjfx.Models;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import openjfx.Exceptions.AlertPanes;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.deeplearning4j.nn.api.Layer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;

public class KerasLoader {

    private String weightsFilePath;
    private MultiLayerNetwork model;
    private String architectureFilePath;
    private static KerasLoader single_instance = null;

    //Загрузить/////////////////////////////////////////////////////////////////////////////////
    private KerasLoader() {
        model = null;
        weightsFilePath = null;
        architectureFilePath = null;
    }
    public static KerasLoader getInstance(){
        if(single_instance == null)
            single_instance = new KerasLoader();

        return single_instance;
    }

    public void loadModelFullH5(String filepath){
        //загружаю модель
        try {
            model = KerasModelImport.importKerasSequentialModelAndWeights(filepath);
            //ставлю переменые на эту модель
            architectureFilePath = filepath;
            weightsFilePath = filepath;
            //в чекере делаю активным керас
            FrameworkChecker checker = FrameworkChecker.getInstance();
            checker.setActiveFramework("Keras");
            AlertPanes.showInfoAlertPane("Загрузка файла модели","Результат операции: успешно","Модель была успешно загружена");
        } catch (Exception e) {
            AlertPanes.showErrorAlertPane("Загрузка файла модели","Результат операции: неудачно","Неверный тип расширения файла",ExceptionUtils.getStackTrace(e));
        }
    }

    public void loadModelArchitectureJSON(String filepath){
        //если архитектуру, то добавляю в архитектуру и обнуляю веса
        architectureFilePath = filepath;
        weightsFilePath = null;
        //если до этого было что-то
        FrameworkChecker checker = FrameworkChecker.getInstance();
        if(checker.giveActiveFramework() != null){
            //если это было керасом, то обнуляю, ибо пока веса не загружены
            if(checker.giveActiveFramework().equals("Keras")){
                checker.setActiveFramework(null);
            }
        }
        AlertPanes.showWarningAlertPane("Загрузка файла весов модели","Результат операции: незавершено","Не указан файл весов модели");
    }

    public void loadModelWeightsH5(String filepath){
        //если пытаюсь загрузить веса без архитектуры
        if(architectureFilePath == null){
            AlertPanes.showWarningAlertPane("Загрузка файла весов модели","Результат операции: неудачно","Не указан файл архитектуры модели");
        }
        //если все-же архитектура есть
        else{
            try {
                //сохраняю веса в переменную
                weightsFilePath = filepath;
                //гружу всю модель с архитектурой и весами
                model = KerasModelImport.importKerasSequentialModelAndWeights(architectureFilePath,weightsFilePath);
                //делаю активным в чекере керас
                FrameworkChecker checker = FrameworkChecker.getInstance();
                checker.setActiveFramework("Keras");
                AlertPanes.showInfoAlertPane("Загрузка файла модели","Результат операции: успешно","Модель была успешно загружена");
            } catch (Exception e) {
                AlertPanes.showErrorAlertPane("Загрузка файла весов модели","Результат операции: неудачно","Неверный тип расширения одного из файлов",ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public String convertModelArchitectureFromYAMLToJSON(String filepath){
        String architectureYAML = null;
        try {
            architectureYAML = new String(Files.readAllBytes(Paths.get(filepath)));
            String architectureJSON = convertation(architectureYAML);
            return architectureJSON;
        } catch (Exception e) {
            AlertPanes.showErrorAlertPane("Загрузка файла архитектуры модели","Результат операции: неудачно","Ошибка чтения файла архитектуры",ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    private String convertation(String architectureYAML){
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(architectureYAML, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            AlertPanes.showErrorAlertPane("Трансформация файла архитектуры модели","Результат операции: неудачно","Ошибка трансформации файла архитектуры",ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    //Показать/////////////////////////////////////////////////////////////////////////////////
    public String giveModelSummary(){
        return model.summary();
    }
    public List<List<String>> giveModelSimpleConfig(){
        List<String> layerNames = new ArrayList<String>(model.getLayerNames());
        List<String> layerInputs = new ArrayList<>();
        List<String> layerOutputs = new ArrayList<>();
        List<List<String>> result = new ArrayList<>();
        layerNames.remove(layerNames.size()-1);
        for (int i = 0; i < layerNames.size(); i++) {
            try {
                Object rawObject = new JSONParser().parse(model.getLayer(i).conf().toJson());
                JSONObject rawJSONObject = (JSONObject) rawObject;
                JSONObject JSONObjectWithParameters = (JSONObject) rawJSONObject.get("layer");
                layerInputs.add(String.valueOf(JSONObjectWithParameters.get("nin")));
                layerOutputs.add(String.valueOf(JSONObjectWithParameters.get("nout")));
            } catch (ParseException e) {
                //e.printStackTrace();
                String stacktrace = ExceptionUtils.getStackTrace(e);
            }
        }
        result.add(layerNames);
        result.add(layerInputs);
        result.add(layerOutputs);
        return result;
    }
    //Вычислить/////////////////////////////////////////////////////////////////////////////////
    public List<List<String>> feedModel(List<List<String>> fileData){
        double[][] doubles = new double[fileData.size()][];
        for (int i = 0; i < fileData.size(); i++) {
            double[] newDouble = new double[fileData.get(i).size()];
            for (int j = 0; j < fileData.get(i).size(); j++) {
                newDouble[j] = Double.valueOf(fileData.get(i).get(j));
            }
            doubles[i] = newDouble;
        }
        INDArray input = Nd4j.create(doubles);
        INDArray output = model.output(input);
        double[][] rawDoubles = output.toDoubleMatrix();
        List<List<String>> rawResult = new ArrayList<>();
        for (int i = 0; i < rawDoubles.length; i++) {
            List<String> strings = new ArrayList<>();
            for (int j = 0; j < rawDoubles[i].length; j++) {
                strings.add(String.valueOf(rawDoubles[i][j]));
            }
            rawResult.add(strings);
        }
        return rawResult;

    }


}
