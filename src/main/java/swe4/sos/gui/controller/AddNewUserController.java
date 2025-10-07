package swe4.sos.gui.controller;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import swe4.sos.gui.manager.SceneManager;
import swe4.sos.gui.model.User;

public class AddNewUserController extends Controller {

  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private PasswordField confirmPasswordField;
  @FXML private TextField emailField;
  @FXML private Label statusLabel;

  private boolean newUserNameIsNotUsed(String username) {
    User user = null;
    try {
      user = repository.getUserByUsername(username);
    } catch (java.rmi.RemoteException e) {
      throw new RuntimeException(e);
    }
    return (user == null);
  }

  @FXML
  public void initialize() {
  }

  @FXML
  public void handleSubmitAddNewUser() {
    String username = usernameField.getText();
    String password = passwordField.getText();
    String email = emailField.getText();

    String confirmPassword = confirmPasswordField.getText();

    if (!username.isBlank() && newUserNameIsNotUsed(username)
        && !password.isBlank()
        && !email.isBlank()
    ) {
      if (password.equals(confirmPassword)) {
        statusLabel.setText("Successfully added user");
        statusLabel.setStyle("-fx-text-fill: green;");

        try {
          repository.addUser(username, password, email);
        } catch (java.rmi.RemoteException e) {
          throw new RuntimeException(e);
        }

        PauseTransition delay = new PauseTransition(Duration.millis(800));
        delay.setOnFinished(event -> SceneManager.getInstance().switchTo("/swe4/sos/gui/view/LoginView.fxml"));

        delay.play();
      } else {
        statusLabel.setText("Passwords do not match");
        statusLabel.setStyle("-fx-text-fill: red");
      }
    } else {
      statusLabel.setText("Make sure all fields are valid");
      statusLabel.setStyle("-fx-text-fill: red");
    }
  }

  @FXML
  public void cancelAddNewUser() {
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/LoginView.fxml");
  }

  @Override
  public void refreshData() {

  }
}
