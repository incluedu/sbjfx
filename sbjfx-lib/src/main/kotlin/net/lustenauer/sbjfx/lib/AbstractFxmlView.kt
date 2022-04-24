package de.felixroske.jfxsupport

import de.felixroske.jfxsupport.anotations.FXMLView
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import javafx.util.Callback
import mu.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.io.IOException
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Base class for fxml-based view classes.
 *
 * It is derived from Adam Bien's
 * [afterburner.fx](http://afterburner.adam-bien.com/) project.
 *
 *
 * [AbstractFxmlView] is a stripped down version of [FXMLView](https://github.com/AdamBien/afterburner.fx/blob/02f25fdde9629fcce50ea8ace5dec4f802958c8d/src/main/java/com/airhacks/afterburner/views/FXMLView.java) that provides DI for Java FX Controllers via Spring.
 *
 *
 *
 * Supports annotation driven creation of FXML based view beans with [FXMLView]
 *
 *
 * @author Thomas Darimont
 * @author Felix Roske
 * @author Andreas Jay
 * @author Patric Hollenstein
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class AbstractFxmlView : ApplicationContextAware {
    private val resource: URL
    private val resourceBundle: ResourceBundle?
    private val presenterProperty = SimpleObjectProperty<Any>()
    private val annotation: FXMLView = javaClass.getAnnotation(FXMLView::class.java)
    private val fxmlRoot = PropertyReaderHelper.determineFilePathFromPackageName(javaClass)
    private var fxmlLoader: FXMLLoader
    private lateinit var applicationContext: ApplicationContext
    private val stage: Stage get() = GUIState.stage
    private var currentStageModality: Modality? = null
    private var isPrimaryStageView = false

    /**
     * Instantiates a new abstract fxml view.
     */
    init {
        logger.debug { "AbstractFxmlView initialize" }
        resource = getResource()
        resourceBundle = getResourceBundle(bundleName)
        fxmlLoader = loadSynchronously(resource, resourceBundle)
    }

    /**
     * Gets the resource URL. This will be derived from applied annotation value
     * or from naming convention.
     *
     * @return the URL resource
     */
    private fun getResource(): URL {
        val path = annotation.value.ifEmpty { fxmlPath }
        val url = javaClass.getResource(path)
        if (url == null) {
            logger.error { "Failed to load resource file '$path'" }
            Platform.exit()
        }
        return url as URL
    }

    /**
     * Creates the controller for type.
     *
     * @param type
     * the type
     * @return the object
     */
    private fun createControllerForType(type: Class<*>): Any {
        return applicationContext.getBean(type)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }


    /**
     * Load synchronously.
     *
     * @param resource
     * the resource
     * @param bundle
     * the bundle
     * @return the FXML loader
     * @throws IllegalStateException
     * the illegal state exception
     */
    @Throws(IllegalStateException::class)
    private fun loadSynchronously(resource: URL, bundle: ResourceBundle?): FXMLLoader {
        val loader = FXMLLoader(resource, bundle)
        loader.controllerFactory = Callback { type: Class<*> -> createControllerForType(type) }
        try {
            loader.load<Any>()
        } catch (e: IOException) {
            throw IllegalStateException("Cannot load $conventionalName", e)
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Cannot load $conventionalName", e)
        }
        return loader
    }

    /**
     * Sets up the first view using the primary [Stage]
     */
    fun initFirstView() {
        isPrimaryStageView = true
        val scene = if (view.scene != null) view.scene else Scene(view)
        stage.scene = scene
        GUIState.scene = scene
    }

    /**
     * todo missing doc
     */
    fun hide() = stage.hide()

    /**
     * Shows the FxmlView instance being the child stage of the given [Window]
     *
     * @param window
     * The owner of the FxmlView instance
     * @param modality
     * See `javafx.stage.Modality`.
     */
    fun showView(window: Window, modality: Modality = Modality.NONE) {
        if (!isPrimaryStageView && (currentStageModality != modality || stage.owner != window)) {
            GUIState.stage = createStage(modality)
            stage.initOwner(window)
        }
        stage.show()
    }

    /**
     * Shows the FxmlView instance on a top level [Window]
     *
     * @param modality
     * See `javafx.stage.Modality`.
     */
    fun showView(modality: Modality = Modality.NONE) {
        if (!isPrimaryStageView && (currentStageModality != modality)) {
            GUIState.stage = createStage(modality)
        }
        stage.show()
    }

    /**
     * Shows the FxmlView instance being the child stage of the given [Window] and waits
     * to be closed before returning to the caller.
     *
     * @param window
     * The owner of the FxmlView instance
     * @param modality
     * See `javafx.stage.Modality`.
     */
    fun showViewAndWait(window: Window, modality: Modality = Modality.NONE) {
        if (isPrimaryStageView) {
            showView(modality) // this modality will be ignored anyway
            return
        }
        if (currentStageModality != modality || stage.owner != window) {
            GUIState.stage = createStage(modality)
            stage.initOwner(window)
        }
        stage.showAndWait()
    }

    /**
     * Shows the FxmlView instance on a top level [Window] and waits to be closed before
     * returning to the caller.
     *
     * @param modality
     * See `javafx.stage.Modality`.
     */
    fun showViewAndWait(modality: Modality = Modality.NONE) {
        if (isPrimaryStageView) {
            showView(modality) // this modality will be ignored anyway
            return
        }
        if (currentStageModality != modality) {
            GUIState.stage = createStage(modality)
        }
        stage.showAndWait()
    }

    /**
     * todo missing doc
     */
    private fun createStage(modality: Modality): Stage = with(Stage()) {
        currentStageModality = modality
        initModality(modality)
        title = defaultTitle
        initStyle(defaultStyle)
        stage.icons?.let { icons.addAll(it) }
        scene = if (view.scene != null) view.scene else Scene(view)
        return this
    }

    /**
     * Initializes the view by loading the FXML (if not happened yet) and
     * returns the top Node (parent) specified in the FXML file.
     *
     * @return the root view as determined from [FXMLLoader].
     */
    val view: Parent
        get() {
            val parent = fxmlLoader.getRoot<Parent>()
            addCSSIfAvailable(parent)
            return parent
        }

    /**
     * Initializes the view synchronously and invokes the consumer with the
     * created parent Node within the FX UI thread.
     *
     * @param consumer
     * - an object interested in received the [Parent] as
     * callback
     */
    fun getView(consumer: Consumer<Parent>) {
        CompletableFuture.supplyAsync({ view }) { runnable: Runnable? -> Platform.runLater(runnable) }
                .thenAccept(consumer)
    }

    /**
     * Scene Builder creates for each FXML document a root container. This
     * method omits the root container (e.g. AnchorPane) and gives you
     * the access to its first child.
     *
     * @return the first child of the AnchorPane or null if there are no
     * children available from this view.
     */
    val viewWithoutRootContainer: Node?
        get() {
            val children = view.childrenUnmodifiable
            return if (children.isEmpty()) {
                null
            } else children.listIterator().next()
        }

    /**
     * Adds the CSS if available.
     *
     * @param parent
     * the parent
     */
    fun addCSSIfAvailable(parent: Parent) {

        // Read global css when available:
        val list = PropertyReaderHelper[applicationContext.environment, "javafx.css"]
        if (list.isNotEmpty()) {
            list.forEach(Consumer { css -> parent.stylesheets.add(javaClass.getResource(css)?.toExternalForm()) })
        }
        addCSSFromAnnotation(parent)
        val uri = javaClass.getResource(styleSheetName) ?: return
        val uriToCss = uri.toExternalForm()
        parent.stylesheets.add(uriToCss)
    }

    /**
     * Adds the CSS from annotation to parent.
     *
     * @param parent
     * the parent
     */
    private fun addCSSFromAnnotation(parent: Parent) {
        if (annotation.css.isNotEmpty()) {
            annotation.css.forEach { cssFile ->
                val uri = javaClass.getResource(cssFile)
                if (uri != null) {
                    val uriToCss = uri.toExternalForm()
                    parent.stylesheets.add(uriToCss)
                    logger.debug { "css file added to parent: $cssFile" }
                } else {
                    logger.warn { "referenced $cssFile css file could not be located" }
                }
            }
        }
    }

    /**
     * Gets the default title for to be shown in a (un)modal window.
     *
     */
    val defaultTitle: String get() = annotation.title

    /**
     * Gets the default style for a (un)modal window.
     */
    val defaultStyle: StageStyle get() = StageStyle.valueOf(annotation.stageStyle.uppercase(Locale.getDefault()))

    /**
     * Gets the style sheet name.
     *
     * @return the style sheet name
     */
    private val styleSheetName: String get() = fxmlRoot + getConventionalName(".css")

    /**
     * In case the view was not initialized yet, the conventional fxml
     * (airhacks.fxml for the AirhacksView and AirhacksPresenter) are loaded and
     * the specified presenter / controller is going to be constructed and
     * returned.
     *
     * @return the corresponding controller / presenter (usually for a
     * AirhacksView the AirhacksPresenter)
     */
    val presenter: Any get() = presenterProperty.get()

    /**
     * Does not initialize the view. Only registers the Consumer and waits until
     * the view is going to be created / the method FXMLView#getView or
     * FXMLView#getViewAsync invoked.
     *
     * @param presenterConsumer
     * listener for the presenter construction
     */
    fun getPresenter(presenterConsumer: Consumer<Any?>) {
        presenterProperty.addListener { _, _, newValue -> presenterConsumer.accept(newValue) }
    }

    /**
     * Gets the conventional name.
     *
     * @param ending
     * the suffix to append
     * @return the conventional name with stripped ending
     */
    private fun getConventionalName(ending: String): String = conventionalName + ending

    /**
     * Gets the conventional name.
     *
     * @return the name of the view without the "View" prefix in lowerCase. For
     * AirhacksView just airhacks is going to be returned.
     */
    private val conventionalName: String
        get() = stripEnding(javaClass.simpleName.lowercase(Locale.getDefault()))

    /**
     * Gets the bundle name.
     *
     * @return the bundle name
     */
    private val bundleName: String
        get() {
            return if (annotation.bundle.isEmpty()) {
                val bundle = "${javaClass.getPackage().name}.$conventionalName"
                logger.debug { "Bundle: $bundle based on conventional name." }
                bundle
            } else {
                val bundle = annotation.bundle
                logger.debug { "Annotated bundle: $bundle" }
                bundle
            }
        }

    /**
     * Gets the fxml file path.
     *
     * @return the relative path to the fxml file derived from the FXML view.
     * e.g. The name for the AirhacksView is going to be
     * <PATH>/airhacks.fxml.
    </PATH> */
    val fxmlPath: String
        get() {
            val fxmlPath = fxmlRoot + getConventionalName(".fxml")
            logger.debug { "Determined fxmlPath: $fxmlPath" }
            return fxmlPath
        }

    /**
     * Returns a resource bundle if available
     *
     * @param name
     * the name of the resource bundle.
     * @return the resource bundle
     */
    private fun getResourceBundle(name: String): ResourceBundle? {
        return try {
            logger.debug { "Resource bundle: $name" }
            ResourceBundle.getBundle(name, ResourceBundleControl(resourceBundleCharset))
        } catch (ex: MissingResourceException) {
            logger.debug { "No resource bundle could be determined: ${ex.message}" }
            null
        }
    }

    /**
     * Returns the charset to use when reading resource bundles as specified in
     * the annotation.
     *
     * @return  the charset
     */
    private val resourceBundleCharset: Charset
        get() = Charset.forName(annotation.encoding)

    companion object {
        private val logger = KotlinLogging.logger { }

        /**
         * Strip ending.
         *
         * @param clazz
         * the clazz
         * @return the string
         */
        private fun stripEnding(clazz: String): String =
            if (!clazz.endsWith("view")) clazz
            else clazz.substring(0, clazz.lastIndexOf("view"))
    }
}
