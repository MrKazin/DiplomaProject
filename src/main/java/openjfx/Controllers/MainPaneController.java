package openjfx.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import openjfx.Switcher.Panes;
import openjfx.Switcher.PanesSwitcher;

public class MainPaneController {
    @FXML
    private BorderPane mainBorderPane;

    public void setLoadingPane(ActionEvent actionEvent) {
        Pane pane = PanesSwitcher.switchTo(Panes.LOADING);
        mainBorderPane.setCenter(pane);
    }

    public void setDisplayingPane(ActionEvent actionEvent) {
        Pane pane = PanesSwitcher.switchTo(Panes.DISPLAYING);
        mainBorderPane.setCenter(pane);
    }

    public void setInteractingPane(ActionEvent actionEvent) {
        Pane pane = PanesSwitcher.switchTo(Panes.INTERACTING);
        mainBorderPane.setCenter(pane);
    }
}
