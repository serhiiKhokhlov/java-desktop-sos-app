package swe4.sos.gui.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import swe4.sos.gui.infrastructure.AuthenticationService;
import swe4.sos.gui.manager.SceneManager;
import swe4.sos.gui.model.User;

public class LoginController extends Controller {

  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Label statusLabel;

  private User getUserIfDataIsValid(String username, String password) {
    User user = null;
    try {
      user = repository.getUserByUsername(username);
    } catch (java.rmi.RemoteException e) {
      throw new RuntimeException(e);
    }
    if (!(user == null || !user.getPassword().equals(password))) return user; else return null;
  }

  @FXML
  public void initialize() {
  }

  @FXML
  private void handleLogin() {
    String username = usernameField.getText();
    String password = passwordField.getText();

    User user = getUserIfDataIsValid(username, password);

    if (user != null) {

      statusLabel.setText("Login Successful");
      statusLabel.setStyle("-fx-text-fill: green");

      PauseTransition delay = new PauseTransition(Duration.millis(800));
      delay.setOnFinished(event -> {
        AuthenticationService.setCurrentUser(user);
        SceneManager.getInstance().switchTo("/swe4/sos/gui/view/DashboardView.fxml");
      });

      delay.play();
    } else {
      statusLabel.setText("Invalid credentials.");
      statusLabel.setStyle("-fx-text-fill: red");
    }
  }

  @FXML
  private void handleAddNewUser() {
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/AddNewUserView.fxml");
  }

  @Override
  public void refreshData() {

  }
}
