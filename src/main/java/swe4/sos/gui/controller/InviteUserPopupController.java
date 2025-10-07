package swe4.sos.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe4.sos.gui.manager.SceneManager;
import swe4.sos.gui.model.AppointmentSurvey;
import swe4.sos.gui.model.User;

import java.rmi.RemoteException;

public class InviteUserPopupController extends Controller {
  @FXML
  private TextField joinKeyLabel;
  @FXML
  private TextField emailField;
  private AppointmentSurvey currentSurvey;

  public void setCurrentSurvey(AppointmentSurvey currentSurvey) {
    this.currentSurvey = currentSurvey;
    joinKeyLabel.setText(currentSurvey.getJoinKey());
  }

  @FXML
  public void initialize() {
  }

  @FXML
  public void handleSubmit() throws RemoteException {
    if (emailField.getText().isEmpty()) {
      showAlert("Error", "Email field is empty!");
    } else {
      String email = emailField.getText();
      User user = null;
      try {
        user = repository.getUserByEmail(email);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
      if (user == null) showAlert("Error", "User not found!");
      else {
        currentSurvey.inviteUser(user.getId());
        repository.updateSurvey(currentSurvey);
        showInfoAlert("Success", "User invited successfully");
        ((Stage) emailField.getScene().getWindow()).close();
        SceneManager.getInstance().refreshCurrentView();
      }
    }
  }

  @FXML
  public void handleCancel() {
    ((Stage) emailField.getScene().getWindow()).close();
  }

  @Override
  public void refreshData() {

  }
}
