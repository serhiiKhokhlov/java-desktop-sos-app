package swe4.sos.db;

@SuppressWarnings("serial")
public class DataAccessException extends RuntimeException {
  public DataAccessException(String msg) {
    super(msg);
  }
}
