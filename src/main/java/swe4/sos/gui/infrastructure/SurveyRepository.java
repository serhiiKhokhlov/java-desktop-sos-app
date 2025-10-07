package swe4.sos.gui.infrastructure;

import swe4.sos.gui.model.AppointmentSurvey;
import swe4.sos.gui.model.SurveyOption;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SurveyRepository extends Remote {
  List<AppointmentSurvey> getParticipatedSurveys(int userId) throws RemoteException;
  List<AppointmentSurvey> getInvitedSurveys(int userId) throws RemoteException;
  void addSurvey(int userId, String label, String description) throws RemoteException;
  void addSurvey(int userId, String label, String description, List<SurveyOption> options) throws RemoteException;
  AppointmentSurvey getSurvey(int surveyId) throws RemoteException;
  void removeSurvey(int surveyId) throws RemoteException;
  boolean participateInSurvey(String keyJoin, int userId) throws RemoteException;
  boolean declineSurvey(String keyJoin, int userId) throws RemoteException;

  void updateSurvey(AppointmentSurvey currentSurvey) throws RemoteException;
}
