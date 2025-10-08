package swe4.sos.server.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import swe4.sos.gui.controller.Controller;
import swe4.sos.gui.infrastructure.Repository;
import swe4.sos.gui.manager.SceneManager;

import java.rmi.Naming;
import java.rmi.RemoteException;

public final class Client extends Application implements ClientCallback {

  private Repository repository;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() throws Exception {
    // Read host from parameters or default to localhost
    String hostAndPort = getParameters().getUnnamed().isEmpty() ? "localhost" : getParameters().getUnnamed().getFirst();

    String sosUrl = "rmi://%s/SOS".formatted(hostAndPort);
    System.out.printf("Connecting to '%s'%n", sosUrl);
    repository = (Repository) Naming.lookup(sosUrl);
  }

  @Override
  public void start(Stage primaryStage) {
    Controller.setRepository(repository);

    SceneManager.getInstance().setStage(primaryStage);
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/LoginView.fxml");
    primaryStage.setTitle("JavaFX App");
    primaryStage.show();
  }

  @Override
  public void notifyChange() throws RemoteException {
    // Refresh data when notified by the server
    Platform.runLater(SceneManager.getInstance()::refreshCurrentView);
  }
}
