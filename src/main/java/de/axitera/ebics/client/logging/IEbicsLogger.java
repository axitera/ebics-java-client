package de.axitera.ebics.client.logging;


/**
 * A mean to log all ebics client operations. The log process
 * ensures four functions:
 * <ol>
 *   <li> debug: debug information
 *   <li> info: inform the user about an operation</li>
 *   <li> warn: warn the user about a risk that may affect the transfer or the key activation process</li>
 *   <li> error: report an error to the user with its details and causes</li>
 * </ol>
 *
*
 */
public interface IEbicsLogger {

    /**
     * Informs a given message to the client application user.
     *
     * @param message the message to be informed.
     */
    public void debug(String message);

    /**
     * Informs a given message to the client application user.
     *
     * @param message the message to be informed.
     */
    public void info(String message);

    /**
     * Warns a given message to the client application user.
     *
     * @param message the given message.
     */
    public void warn(String message);

    /**
     * Warns a given message and its causes to the client application user.
     *
     * @param message   the given message.
     * @param throwable message causes.
     */
    public void warn(String message, Throwable throwable);

    /**
     * Reports an error to the client application user.
     *
     * @param message the error message
     */
    public void error(String message);

    /**
     * Reports an error and its causes to the client application user.
     *
     * @param message   the error message.
     * @param throwable the error causes.
     */
    public void error(String message, Throwable throwable);

}