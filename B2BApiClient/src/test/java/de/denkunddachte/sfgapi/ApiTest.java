package de.denkunddachte.sfgapi;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

import de.denkunddachte.exception.ApiException;

public abstract class ApiTest {
  private Logger LOGGER;
  public ApiTest() {
    LOGGER = getLogger();
  }
  
  protected abstract Logger getLogger();
  
  
  protected void printJSON(ApiClient object) throws ApiException {
    try {
      LOGGER.log(Level.INFO, "JSON: {0}", object.toJSON());
    } catch (JSONException e) {
      throw new ApiException(e);
    }
  }

  protected void printList(List<? extends ApiClient> list) {
    LOGGER.log(Level.INFO, "Loaded {0} objects.", list.size());
    int i = 0;
    for (Object o : list) {
      LOGGER.log(Level.INFO, String.format("[%04d]: %s%n", ++i, o));
    }
  }
}
