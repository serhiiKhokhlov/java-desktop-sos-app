  package swe4.sos.gui.manager;

  import javafx.fxml.FXMLLoader;
  import javafx.scene.Parent;
  import javafx.scene.Scene;
  import javafx.stage.Modality;
  import javafx.stage.Stage;
  import swe4.sos.gui.controller.Controller;

  import java.io.IOException;

  public class SceneManager {
    private static SceneManager instance;
    private Stage mainStage;
    private Controller currentController;

    private SceneManager() {}

    public static SceneManager getInstance() {
      if (instance == null) {
        instance = new SceneManager();
      }
      return instance;
    }

    public void setStage(Stage stage) {
      this.mainStage = stage;
    }

    public void switchTo(String fxmlPath) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        currentController = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/swe4/sos/gui/css/styles.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.sizeToScene();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void refreshCurrentView() {
      if (currentController != null) {
        currentController.refreshData();
      }
    }

    public <T extends Controller> T switchToWithController(String fxmlPath) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        mainStage.setScene(new Scene(root));
        mainStage.sizeToScene();
        return loader.getController();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    public <T> T showPopupWithController(String fxmlPath) {
      try {
        Stage popupStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        popupStage.setScene(new Scene(root));
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(mainStage); // Set main stage as owner
        popupStage.show();

        return loader.getController();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }

    public void showPopup(String fxmlPath) {
      try {
        Stage popupStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

        popupStage.setScene(new Scene(root));
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(mainStage);
        popupStage.show();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
