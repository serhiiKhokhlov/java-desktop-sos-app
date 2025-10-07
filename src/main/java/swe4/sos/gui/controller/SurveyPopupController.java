package swe4.sos.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import swe4.sos.gui.infrastructure.AuthenticationService;
import swe4.sos.gui.manager.SceneManager;

public class SurveyPopupController extends Controller {
  @FXML private TextField surveyIdField;

  @FXML
  public void initialize() {
  }

  @FXML
  private void handleCreateNew() {
    // Close popup first
    ((Stage) surveyIdField.getScene().getWindow()).close();
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/CreateSurveyView.fxml");
  }

  @FXML
  private void handleParticipate() {
    try {
       String keyJoin = surveyIdField.getText();
      boolean success = repository.participateInSurvey(keyJoin, AuthenticationService.getCurrentUserId());
      if (success) {
        ((Stage) surveyIdField.getScene().getWindow()).close();
        SceneManager.getInstance().refreshCurrentView();  // refresh the dashboard view
      } else {
        surveyIdField.setStyle("-fx-border-color: red;");
      }
    } catch (NumberFormatException e) {
      surveyIdField.setStyle("-fx-border-color: red;");
    } catch (java.rmi.RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void refreshData() {

  }
}