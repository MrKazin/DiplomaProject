package openjfx.Switcher;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PanesSwitcher {

    private static Pane pane;
    private static Map<Panes, Pane> cache = new HashMap<>();

    public static Pane switchTo(Panes panes){
        try {
            if(cache.containsKey(panes)){
                pane = cache.get(panes);
            } else{
                pane = FXMLLoader.load(Panes.class.getClassLoader().getResource(panes.getFilename()));
                cache.put(panes, pane);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pane;
    }
}
