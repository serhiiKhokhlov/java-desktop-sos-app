package swe4.sos.gui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import swe4.sos.gui.infrastructure.AuthenticationService;
import swe4.sos.gui.manager.SceneManager;
import swe4.sos.gui.model.AppointmentSurvey;
import swe4.sos.gui.model.SurveyOption;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;


public class SurveyController extends Controller {
  @FXML
  private Label titleLabel;
  @FXML private Label descriptionLabel;
  @FXML private ListView<SurveyOption> optionsList;
  @FXML private Button submitButton;
  @FXML private Button revokeButton;
  @FXML private Button closeSurveyButton;
  @FXML private Button inviteSurveyButton;
  private boolean hasVoted = false;

  private AppointmentSurvey currentSurvey;
  private int currentUserId;
  private final ObservableSet<SurveyOption> selectedOptions = FXCollections.observableSet(new HashSet<>());
  private SurveyOption preferredOption;

  @FXML
  public void initialize() {
  }

  public void initializeSurvey(int surveyId) {
    try {
      currentSurvey = repository.getSurvey(surveyId);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    currentUserId = AuthenticationService.getCurrentUserId();

    // Show close button only for owner
    closeSurveyButton.setVisible(currentSurvey.getCreatedByUserId() == currentUserId);

    initializeUI();
  }

  private void initializeUI() {
    titleLabel.setText(currentSurvey.getLabel());
    descriptionLabel.setText(currentSurvey.getDescription());

    // Check if user has already voted
    hasVoted = currentSurvey.getOptions().stream()
      .anyMatch(option -> option.hasVoted(currentUserId));

    optionsList.setCellFactory(param -> new SurveyOptionCell());
    optionsList.getItems().setAll(currentSurvey.getOptions());

    if (!currentSurvey.isOpen()) {
      optionsList.setDisable(true);
      submitButton.setVisible(false);
      revokeButton.setVisible(false);
    }

    if (!currentSurvey.isOpen()) {
      titleLabel.setStyle("-fx-text-fill: gray;");
      descriptionLabel.setText(descriptionLabel.getText() + " (CLOSED)");
    }

    updateButtonStates();
  }

  private void updateButtonStates() {
    boolean isOpen = currentSurvey.isOpen();
    boolean isOwner = currentSurvey.getCreatedByUserId() == currentUserId;

    // Voting buttons
    submitButton.setVisible(isOpen && !hasVoted && !selectedOptions.isEmpty());
    revokeButton.setVisible(isOpen && hasVoted);

    // Owner buttons
    closeSurveyButton.setVisible(isOpen && isOwner);
    inviteSurveyButton.setVisible(isOpen && isOwner);
  }

  @Override
  public void refreshData() {
    try {
      if (currentSurvey != null) {

        ScrollPane scrollPane = (ScrollPane) optionsList.lookup(".scroll-pane");
        double scrollPosition = (scrollPane != null) ? scrollPane.getVvalue() : 0.0;

        AppointmentSurvey updatedSurvey = repository.getSurvey(currentSurvey.getId());

        if (updatedSurvey == null) {
          // Survey was deleted - return to dashboard
          SceneManager.getInstance().switchTo("/swe4/sos/gui/view/DashboardView.fxml");
          showInfoAlert("Survey Closed", "This survey has been removed");
          return;
        }

        currentSurvey = updatedSurvey;
        currentUserId = AuthenticationService.getCurrentUserId();

        initializeUI();

        selectedOptions.clear();
        preferredOption = null;

        Platform.runLater(() -> optionsList.scrollTo((int)scrollPosition));

        hasVoted = currentSurvey.getOptions().stream()
          .anyMatch(option -> option.hasVoted(currentUserId));

        updateButtonStates();
        optionsList.refresh();
      }
    } catch (RemoteException e) {
      showAlert("Connection Error",
        "Failed to refresh survey data:\n" + e.getMessage());
    }
  }

  private class SurveyOptionCell extends ListCell<SurveyOption> {
    private final CheckBox checkBox = new CheckBox();
    private final Label timeLabel = new Label();
    private final Label statsLabel = new Label();
    private final Label preferredStar = new Label("★");
    private final ContextMenu contextMenu = new ContextMenu();
    private final HBox container = new HBox(10);

    public SurveyOptionCell() {
      // Configure UI elements
      preferredStar.setVisible(false);
      preferredStar.setStyle("-fx-text-fill: gold;");
      timeLabel.setStyle("-fx-font-weight: bold;");

      // Build layout
      HBox timeContainer = new HBox(5, timeLabel, preferredStar);
      VBox textContainer = new VBox(2, timeContainer, statsLabel);
      container.getChildren().addAll(checkBox, textContainer);

      // Context menu setup
      MenuItem preferItem = new MenuItem("Mark Preferred");
      preferItem.setOnAction(e -> handleSetPreferred(getItem()));
      contextMenu.getItems().add(preferItem);

      // Event handlers
      checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
        if (!hasVoted) {  // Only allow changes if not voted
          SurveyOption option = getItem();
          if (newVal) {
            selectedOptions.add(option);
          } else {
            selectedOptions.remove(option);
            if (preferredOption == option) {
              preferredOption = null;
            }
          }
          updateButtonStates();
          updatePreferredDisplay();
        }
      });

      setOnContextMenuRequested(e -> {
        if (checkBox.isSelected() && selectedOptions.size() > 1) {
          contextMenu.show(this, e.getScreenX(), e.getScreenY());
        }
      });
    }

    @Override
    protected void updateItem(SurveyOption option, boolean empty) {
      super.updateItem(option, empty);
      if (empty || option == null) {
        setGraphic(null);
      } else {
        timeLabel.setText(formatTime(option.getTimeOption()));

        // ====== START OF VOTING CONTROL LOGIC ======
        boolean votingEnabled = currentSurvey.isOpen() && !hasVoted;

        checkBox.setDisable(!votingEnabled);
        checkBox.setSelected(hasVoted ? option.hasVoted(currentUserId) : selectedOptions.contains(option));

        // Disable context menu if voting not allowed
        if (votingEnabled && selectedOptions.size() > 1) {
          setOnContextMenuRequested(e -> contextMenu.show(this, e.getScreenX(), e.getScreenY()));
        } else {
          setOnContextMenuRequested(null);
        }
        // ====== END OF VOTING CONTROL LOGIC ======

        // Preferred star visibility
        preferredStar.setVisible(option.hasPreferred(currentUserId));

        // Stats display
        if (currentSurvey.isOpen()) {
          statsLabel.setVisible(false);
        } else {
          double percentage = calculatePercentage(option);
          statsLabel.setText(String.format("%.1f%% (%d votes)",
            percentage, option.getVoteCount()));
          statsLabel.setVisible(true);
        }

        setGraphic(container);
      }
    }
  }

  // Helper methods
  private String formatTime(LocalDateTime time) {
    return time.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
  }

  private double calculatePercentage(SurveyOption option) {
    int total = currentSurvey.getParticipantUserIds().size();
    return total > 0 ? (option.getVoteCount() * 100.0) / total : 0;
  }

  private void updatePreferredDisplay() {
    optionsList.refresh(); // This will force all visible cells to update
  }

  private void handleSetPreferred(SurveyOption option) {
    if (selectedOptions.contains(option)) {
      preferredOption = option;
      optionsList.refresh(); // Refresh the entire list
    }
  }

  @FXML
  private void handleRevoke() {
    // Clear all votes and preferences
    currentSurvey.getOptions().forEach(option -> {
      option.revokeVote(currentUserId);
      option.revokePreferVote(currentUserId);
    });

    // Reset UI state
    hasVoted = false;
    selectedOptions.clear();
    preferredOption = null;

    // Force refresh of all options while maintaining sort order
    ObservableList<SurveyOption> options = FXCollections.observableArrayList(currentSurvey.getOptions());
    optionsList.setItems(options);
    try {
      repository.updateSurvey(currentSurvey);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    updateButtonStates();
    optionsList.refresh();
  }

  @FXML
  private void handleSubmit() {
    if (currentSurvey.isOpen()) {
      // Clear previous votes
      currentSurvey.getOptions().forEach(option -> {
        option.revokeVote(currentUserId);
        option.revokePreferVote(currentUserId);
      });

      // Apply new votes
      selectedOptions.forEach(option -> option.vote(currentUserId));
      if (preferredOption != null) {
        preferredOption.prefer(currentUserId);
      }

      // Persist changes
      try {
        repository.updateSurvey(currentSurvey);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }

      // Reset UI state
      selectedOptions.clear();
      preferredOption = null;
      optionsList.refresh();
      submitButton.setVisible(false);
      hasVoted = true;
      updateButtonStates();
    }
  }

  @FXML
  private void handleBack() {
    SceneManager.getInstance().switchTo("/swe4/sos/gui/view/DashboardView.fxml");
  }

  @FXML
  private void handleInvite() {
    InviteUserPopupController controller = SceneManager.getInstance().showPopupWithController("/swe4/sos/gui/view/InviteUserPopupView.fxml");
    controller.setCurrentSurvey(currentSurvey);

    //System.out.println("Invite button clicked");
  }

  @FXML
  private void handleCloseSurvey() {
    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Close Survey");
    confirmation.setHeaderText("Are you sure you want to close this survey?");
    confirmation.setContentText("Once closed, no further votes can be submitted.");

    Optional<ButtonType> result = confirmation.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      currentSurvey.close();
      try {
        repository.updateSurvey(currentSurvey);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }

      // Immediately update UI
      updateButtonStates();
      optionsList.refresh();

      new Alert(Alert.AlertType.INFORMATION, "Survey closed successfully").show();
    }
  }
}