package swe4.sos.gui.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import swe4.sos.gui.infrastructure.Repository;

import java.util.ArrayList;
import java.util.List;

public abstract class Controller {
  protected static Repository repository;
  private static final List<Controller> activeControllers = new ArrayList<>();

  public static void setRepository(Repository repository) {
    Controller.repository = repository;
  }

  public static void refreshAllViews() {
    for (Controller controller : activeControllers) {
      Platform.runLater(controller::refreshData); // Update UI on JavaFX thread
    }
  }

  public static void registerController(Controller controller) {
    activeControllers.add(controller);
  }

  public static void unregisterController(Controller controller) {
    activeControllers.remove(controller);
  }

  // To be overridden by concrete controllers
  public abstract void refreshData();

  protected void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  protected void showInfoAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
