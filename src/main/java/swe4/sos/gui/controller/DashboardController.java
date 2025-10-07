package swe4.sos.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import swe4.sos.gui.infrastructure.AuthenticationService;
import swe4.sos.gui.manager.SceneManager;
import swe4.sos.gui.model.AppointmentSurvey;

import java.rmi.RemoteException;

public class DashboardController extends Controller {

  @FXML private TextField mySurveysFilter;
  @FXML private TextField invitationsFilter;
  @FXML private CheckBox personalOnlyCheckbox;
  @FXML private ListView<AppointmentSurvey> listViewOne;
  @FXML private ListView<AppointmentSurvey> listViewTwo;

  private final ObservableList<AppointmentSurvey> participatedSurveys = FXCollections.observableArrayList();
  private final ObservableList<AppointmentSurvey> invitedSurveys = FXCollections.observableArrayList();
  private final FilteredList<AppointmentSurvey> filteredParticipated = new FilteredList<>(participatedSurveys);
  private final FilteredList<AppointmentSurvey> filteredInvited = new FilteredList<>(invitedSurveys);

  @FXML
  public void initialize() {

    loadData(AuthenticationService.getCurrentUserId());

    // Setup ListViews
    listViewOne.setItems(filteredParticipated);
    listViewTwo.setItems(filteredInvited);

    // Configure cell factories
    listViewOne.setCellFactory(param -> createParticipatedSurveyCell());
    listViewTwo.setCellFactory(param -> createInvitedSurveyCell());

    // Setup filtering
    setupFilterBindings();
  }

  private void setupFilterBindings() {
    // My Surveys filter
    mySurveysFilter.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
    personalOnlyCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilters());

    // Invitations filter
    invitationsFilter.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
  }

  private void updateFilters() {
    filteredParticipated.setPredicate(survey ->
      matchesMySurveysFilter(survey) &&
        (!personalOnlyCheckbox.isSelected() || survey.getCreatedByUserId() == AuthenticationService.getCurrentUserId())
    );

    filteredInvited.setPredicate(this::matchesInvitationsFilter);
  }

  private boolean matchesMySurveysFilter(AppointmentSurvey survey) {
    String filter = mySurveysFilter.getText().trim().toLowerCase();
    return survey.getLabel().toLowerCase().contains(filter) ||
      survey.getDescription().toLowerCase().contains(filter);
  }

  private boolean matchesInvitationsFilter(AppointmentSurvey survey) {
    String filter = invitationsFilter.getText().trim().toLowerCase();
    return survey.getLabel().toLowerCase().contains(filter) ||
      survey.getDescription().toLowerCase().contains(filter);
  }

  private ListCell<AppointmentSurvey> createParticipatedSurveyCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(AppointmentSurvey survey, boolean empty) {
        super.updateItem(survey, empty);
        if (empty || survey == null) {
          setText(null);
          setGraphic(null);
        } else {
          HBox hbox = new HBox(10);
          hbox.setAlignment(Pos.CENTER_LEFT);

          VBox vbox = new VBox();
          Label titleLabel = new Label(survey.getLabel());
          titleLabel.getStyleClass().add("survey-title");

          // Add status label
          Label statusLabel = new Label(survey.isOpen() ? "Status: Open" : "Status: Closed");
          statusLabel.setStyle(survey.isOpen()
            ? "-fx-text-fill: green; -fx-font-weight: bold;"
            : "-fx-text-fill: red; -fx-font-weight: bold;");

          Label participants = new Label("Participants: " + survey.getParticipantUserIds().size());

          vbox.getChildren().addAll(titleLabel, statusLabel, participants);
          hbox.getChildren().add(vbox);
          setGraphic(hbox);

          setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && !isEmpty()) {
              handleOpenSurvey(survey);
            }
          });
        }
      }
    };
  }

  private ListCell<AppointmentSurvey> createInvitedSurveyCell() {
    return new ListCell<>() {
      private final Button acceptButton = new Button("Accept");
      private final Button declineButton = new Button("Decline");

      {
        acceptButton.getStyleClass().add("small-button");
        declineButton.getStyleClass().add("small-button");

        acceptButton.setOnAction(event -> handleAcceptInvitation(getItem()));
        declineButton.setOnAction(event -> handleDeclineInvitation(getItem()));
      }

      @Override
      protected void updateItem(AppointmentSurvey survey, boolean empty) {
        super.updateItem(survey, empty);
        if (empty || survey == null) {
          setText(null);
          setGraphic(null);
        } else {
          HBox hbox = new HBox(10);
          hbox.setAlignment(Pos.CENTER_LEFT);

          VBox vbox = new VBox();
          Label titleLabel = new Label(survey.getLabel());
          titleLabel.getStyleClass().add("survey-title");

          // Add status label
          Label statusLabel = new Label(survey.isOpen() ? "Status: Open" : "Status: Closed");
          statusLabel.setStyle(survey.isOpen()
            ? "-fx-text-fill: green; -fx-font-weight: bold;"
            : "-fx-text-fill: red; -fx-font-weight: bold;");

          Label participants = new Label("Participants: " + survey.getParticipantUserIds().size());

          vbox.getChildren().addAll(titleLabel, statusLabel, participants);

          HBox buttonBox = new HBox(5, acceptButton, declineButton);
          buttonBox.setAlignment(Pos.CENTER_RIGHT);

          hbox.getChildren().addAll(vbox, buttonBox);
          setGraphic(hbox);
        }
      }
    };
  }

  private void handleAcceptInvitation(AppointmentSurvey survey) {
    try {
      repository.participateInSurvey(survey.getJoinKey(), AuthenticationService.getCurrentUserId());
      refreshData();
    } catch (RemoteException e) {
      e.printStackTrace();
      // Handle gracefully
    }
  }

  private void handleDeclineInvitation(AppointmentSurvey survey) {
    try {
      repository.declineSurvey(survey.getJoinKey(), AuthenticationService.getCurrentUserId());
      refreshData();
    } catch (RemoteException e) {
      e.printStackTrace();
      // Handle gracefully
    }
  }


  @FXML
  private void handleOpenSurvey(AppointmentSurvey survey) {
    try {
      SurveyController controller = SceneManager.getInstance()
        .switchToWithController("/swe4/sos/gui/view/SurveyView.fxml");
      controller.initializeSurvey(survey.getId());
    } catch (Exception e) {
      e.printStackTrace();
      // Handle error
    }
  }

  @FXML
  private void handleAddSurvey() {
    SceneManager.getInstance().showPopupWithController("/swe4/sos/gui/view/SurveyPopupView.fxml");
  }

  @Override
  public void refreshData() {
    loadData(AuthenticationService.getCurrentUserId());
    updateFilters();
  }

  private void loadData(int userId) {
    try {
      participatedSurveys.setAll(repository.getParticipatedSurveys(userId));
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    try {
      invitedSurveys.setAll(repository.getInvitedSurveys(userId));
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  private void handleLogout() {
    AuthenticationService.clear();
    participatedSurveys.clear();
    invitedSurveys.clear();
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/LoginView.fxml");
  }
}