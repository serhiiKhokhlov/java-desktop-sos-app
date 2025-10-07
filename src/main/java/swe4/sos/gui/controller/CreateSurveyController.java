package swe4.sos.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import swe4.sos.gui.infrastructure.AuthenticationService;
import swe4.sos.gui.manager.SceneManager;
import swe4.sos.gui.model.SurveyOption;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateSurveyController extends Controller {

  @FXML private TextField titleField;
  @FXML private TextArea descriptionField;
  @FXML private ListView<LocalDateTime> optionsList;

  private final ObservableList<LocalDateTime> timeOptions = FXCollections.observableArrayList();

  @FXML
  public void initialize() {
    optionsList.setItems(timeOptions);
    optionsList.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(LocalDateTime time, boolean empty) {
        super.updateItem(time, empty);
        if (empty || time == null) {
          setText(null);
        } else {
          setText(time.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        }
      }
    });
  }

  @FXML
  private void handleAddOption() {
    TimeOptionPopupController controller = SceneManager.getInstance()
      .showPopupWithController("/swe4/sos/gui/view/TimeOptionPopup.fxml");

    if (controller != null) {
      controller.setTimeConsumer(time -> {
        if (time != null) {
          timeOptions.add(time);
        }
      });
    }
  }

  @FXML
  private void handleSubmit() {
    String title = titleField.getText().trim();
    String description = descriptionField.getText().trim();

    if (title.isEmpty()) {
      showAlert("Error", "Title cannot be empty");
      return;
    }

    if (timeOptions.isEmpty()) {
      showAlert("Error", "Please add at least one time option");
      return;
    }

    try {
      // Convert LocalDateTime list to SurveyOption list
      List<SurveyOption> surveyOptions = new ArrayList<>();
      int optionId = 1; // Starting ID for new options
      for (LocalDateTime time : timeOptions) {
        surveyOptions.add(new SurveyOption(optionId++, time));
      }

      // Create survey with the current user as owner
      int userId = AuthenticationService.getCurrentUserId();
      repository.addSurvey(userId, title, description, surveyOptions);

      showInfoAlert("Success", "Survey created successfully");
      SceneManager.getInstance().switchTo("/swe4/sos/gui/view/DashboardView.fxml");
    } catch (Exception e) {
      showAlert("Error", "Failed to create survey: " + e.getMessage());
    }
  }

  @FXML
  private void handleCancel() {
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/DashboardView.fxml");
  }

  @Override
  public void refreshData() {

  }
}