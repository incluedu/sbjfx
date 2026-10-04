package net.lustenauer.sbjfx.lib.exceptions

/**
 * Exception thrown by the framework when a designated layout asset,
 * CSS stylesheet, or resource bundle configuration path cannot be located.
 *
 * @author Felix Roske
 * @author Patric Hollenstein
 */
@Suppress("unused")
class ResourceNotFoundException : Exception {

    /**
     * Constructs a new exception instance with the specified detail message.
     */
    constructor(message: String) : super(message)

    /**
     * Constructs a new exception instance with the specified detail message and cause.
     */
    constructor(message: String, cause: Throwable) : super(message, cause)
}
