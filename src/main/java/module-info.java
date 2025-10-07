module swe4.sos.serhiiKhokhlov {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires java.rmi;
  requires java.sql;

  // Open only to javafx.fxml if FXML needs access to controllers
  opens swe4.sos.gui.controller to javafx.fxml;
  opens swe4.sos.gui to javafx.graphics;



  // Export packages if they are used by other modules (e.g., RMI stubs or shared models)
  exports swe4.sos.gui.controller;
  exports swe4.sos.gui.model;
  exports swe4.sos.server.client;
  exports swe4.sos.gui.infrastructure to java.rmi;
}
