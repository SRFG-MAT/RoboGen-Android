package at.srfg.robogen.fitnesswatch.fitbit_API.exceptions;

/**
 * Created by jboggess on 9/19/16.
 */
public class FitbitAPIException extends RuntimeException {

    public FitbitAPIException() {
    }

    public FitbitAPIException(String message) {
        super(message);
    }
}
