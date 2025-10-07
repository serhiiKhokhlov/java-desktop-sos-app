package swe4.sos.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import swe4.sos.gui.manager.SceneManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class TimeOptionPopupController extends Controller {

  @FXML private DatePicker datePicker;
  @FXML private Spinner<Integer> hourSpinner;
  @FXML private Spinner<Integer> minuteSpinner;

  private Consumer<LocalDateTime> timeConsumer;

  @FXML
  public void initialize() {
    datePicker.setValue(LocalDate.now());
  }

  public void setTimeConsumer(Consumer<LocalDateTime> timeConsumer) {
    this.timeConsumer = timeConsumer;
  }

  @FXML
  private void handleAdd() {
    try {
      LocalDate date = datePicker.getValue();
      if (date == null) {
        showAlert("Error", "Please select a date");
        return;
      }

      LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
      LocalDateTime dateTime = LocalDateTime.of(date, time);

      if (timeConsumer != null) {
        timeConsumer.accept(dateTime);
      }

      closeWindow();
    } catch (Exception e) {
      showAlert("Error", "Invalid time value: " + e.getMessage());
    }
  }

  @FXML
  private void handleCancel() {
    if (timeConsumer != null) {
      timeConsumer.accept(null);
    }
    closeWindow();
  }

  private void closeWindow() {
    ((Stage) datePicker.getScene().getWindow()).close();
  }

  @Override
  public void refreshData() {

  }
}