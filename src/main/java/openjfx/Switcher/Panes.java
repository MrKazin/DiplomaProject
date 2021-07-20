package openjfx.Switcher;

public enum Panes {
    LOADING("LoadingPane.fxml"),
    DISPLAYING("DisplayingPane.fxml"),
    INTERACTING("InteractingPane.fxml");

    private String filename;

    Panes(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
