package swe4.sos.gui.infrastructure;

import swe4.sos.server.client.ClientCallback;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface Repository extends UserRepository, SurveyRepository {


  void addObserver(ClientCallback observer) throws RemoteException;

  void removeObserver(ClientCallback observer) throws RemoteException;

}
