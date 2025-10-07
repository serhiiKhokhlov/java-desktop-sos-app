package swe4.sos.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import swe4.sos.gui.controller.Controller;
import swe4.sos.gui.infrastructure.FakeRepository;
import swe4.sos.gui.manager.SceneManager;

public class GuiDemo extends Application {
  @Override
  public void start(Stage primaryStage) {
    SceneManager.getInstance().setStage(primaryStage);
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/LoginView.fxml");
    primaryStage.setTitle("JavaFX App");
    primaryStage.show();
  }

  public static void main(String[] args) {
    Controller.setRepository(FakeRepository.getInstance());
    launch(args);
  }
}
