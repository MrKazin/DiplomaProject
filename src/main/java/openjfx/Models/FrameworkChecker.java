package openjfx.Models;

public class FrameworkChecker {

    private String activeFramework;
    private static FrameworkChecker single_instance;

    private FrameworkChecker() {
        activeFramework = null;
    }

    public static FrameworkChecker getInstance(){
        if(single_instance == null)
            single_instance = new FrameworkChecker();

        return single_instance;
    }

    public String giveActiveFramework(){
        return activeFramework;
    }

    public void setActiveFramework(String activeFramework){
        this.activeFramework = activeFramework;
    }
}
