package net.lustenauer.sbjfx.lib

import io.github.oshai.kotlinlogging.KotlinLogging
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
import net.lustenauer.sbjfx.lib.anotations.FXMLView
import net.lustenauer.sbjfx.lib.exceptions.ResourceNotFoundException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Base class for fxml-based view classes.
 *
 * It is derived from Adam Bien's [afterburner.fx](http://afterburner.adam-bien.com/) project.
 * [AbstractFxmlView] provides DI for Java FX Controllers via Spring.
 *
 * Supports annotation driven creation of FXML based view beans with [FXMLView]
 *
 * @author Thomas Darimont
 * @author Felix Roske
 * @author Andreas Jay
 * @author Patric Hollenstein
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class AbstractFxmlView : ApplicationContextAware {
    private val resource: URL?
    private val resourceBundle: ResourceBundle?
    private val presenterProperty = SimpleObjectProperty<Any>()
    private val annotation: FXMLView? = javaClass.getAnnotation(FXMLView::class.java)
    private val fxmlRoot = PropertyReaderHelper.determineFilePathFromPackageName(javaClass)
    private lateinit var fxmlLoader: FXMLLoader
    private lateinit var applicationContext: ApplicationContext
    private val stage: Stage get() = GUIState.stage
    private var currentStageModality: Modality? = null
    private var isPrimaryStageView = false

    /**
     * Instantiates a new abstract fxml view.
     */
    init {
        logger.debug { "AbstractFxmlView initialize" }
        resource = runCatching { getResource() }
            .onFailure { e -> logger.error(e) { "Failed to determine resource for FXML view: ${e.message}" } }
            .getOrNull()
        resourceBundle = getResourceBundle(bundleName)
    }

    /**
     * Gets the resource URL. This will be derived from applied annotation value
     * or from naming convention.
     *
     * @return the URL resource
     */
    @Throws(ResourceNotFoundException::class)
    private fun getResource(): URL {
        val path = annotation?.value?.ifEmpty { fxmlPath } ?: fxmlPath
        return javaClass.getResource(path)
            ?: throw ResourceNotFoundException("Failed to load resource file '$path'")
    }

    /**
     * Creates the controller for type.
     *
     * @param type the type
     * @return the object
     */
    private fun createControllerForType(type: Class<*>): Any {
        return applicationContext.getBean(type)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    /**
     * Loads the FXML resource synchronously and links it to the Spring-managed controller.
     *
     * @throws IllegalStateException If the FXML resource cannot be loaded.
     */
    @Throws(IllegalStateException::class)
    private fun loadSynchronously(resource: URL?, bundle: ResourceBundle?): FXMLLoader {
        val message = "Cannot load '$conventionalName'"
        val loader = FXMLLoader(resource, bundle)
        loader.controllerFactory = Callback { type: Class<*> -> createControllerForType(type) }
        return runCatching {
            loader.load<Any>()
            loader
        }.onFailure { e ->
            logger.error(e) { message }
        }.getOrElse { e ->
            throw IllegalStateException(message, e)
        }
    }

    /**
     * Ensures that the underlying [FXMLLoader] is initialized and the presenter bean is set.
     */
    private fun ensureFxmlLoaderInitialized() {
        if (::fxmlLoader.isInitialized) return

        fxmlLoader = loadSynchronously(resource, resourceBundle)
        presenterProperty.set(fxmlLoader.getController())
    }

    /**
     * Sets up the first view using the primary [Stage].
     */
    fun initFirstView() {
        isPrimaryStageView = true
        val scene = view.scene ?: Scene(view)
        stage.scene = scene
        GUIState.scene = scene
    }

    /**
     * Hides the underlying [Stage].
     */
    fun hide() = stage.hide()

    /**
     * Shows the view as a child stage of the given owner [Window].
     *
     * @param window The owner window of this view.
     * @param modality The modality configuration, defaults to the annotation value.
     */
    fun showView(window: Window, modality: Modality = annotation?.modality ?: Modality.NONE) {
        if (!isPrimaryStageView && (currentStageModality != modality || stage.owner != window)) {
            GUIState.stage = createStage(modality)
            stage.initOwner(window)
        }
        stage.show()
    }

    /**
     * Shows the view instance on a top-level [Window].
     *
     * @param modality The modality configuration, defaults to the annotation value.
     */
    fun showView(modality: Modality = annotation?.modality ?: Modality.NONE) {
        if (!isPrimaryStageView && (currentStageModality != modality)) {
            GUIState.stage = createStage(modality)
        }
        stage.show()
    }

    /**
     * Shows the view as a child stage of the given owner [Window] and blocks
     * execution until the stage is closed.
     *
     * @param window The owner window of this view.
     * @param modality The modality configuration, defaults to the annotation value.
     */
    fun showViewAndWait(window: Window, modality: Modality = annotation?.modality ?: Modality.NONE) {
        if (isPrimaryStageView) {
            showView(modality)
            return
        }
        if (currentStageModality != modality || stage.owner != window) {
            GUIState.stage = createStage(modality)
            stage.initOwner(window)
        }
        stage.showAndWait()
    }

    /**
     * Shows the view instance on a top-level [Window] and blocks execution
     * until the stage is closed.
     *
     * @param modality The modality configuration, defaults to the annotation value.
     */
    fun showViewAndWait(modality: Modality = annotation?.modality ?: Modality.NONE) {
        if (isPrimaryStageView) {
            showView(modality)
            return
        }
        if (currentStageModality != modality) {
            GUIState.stage = createStage(modality)
        }
        stage.showAndWait()
    }

    /**
     * Helper factory to create a configured [Stage] instance.
     */
    private fun createStage(modality: Modality): Stage = with(Stage()) {
        currentStageModality = modality
        initModality(modality)
        title = defaultTitle
        initStyle(defaultStyle)
        GUIState.stage.icons?.let { icons.addAll(it) }
        scene = view.scene ?: Scene(view)
        this
    }

    /**
     * Returns the root [Parent] node specified in the FXML file.
     * Initializes the FXML structure on the first call.
     */
    val view: Parent
        get() {
            ensureFxmlLoaderInitialized()
            val parent = fxmlLoader.getRoot<Parent>()
            addCSSIfAvailable(parent)
            return parent
        }

    /**
     * Asynchronously retrieves the view and passes the [Parent] node to the consumer within the UI thread.
     */
    fun getView(consumer: Consumer<Parent>) {
        CompletableFuture.supplyAsync({ view }) { Platform.runLater(it) }
            .thenAccept(consumer)
    }

    /**
     * Returns the first child of the root container, omitting the main layout pane.
     */
    val viewWithoutRootContainer: Node?
        get() {
            val children = view.childrenUnmodifiable
            return if (children.isEmpty()) null else children.listIterator().next()
        }

    /**
     * Injects both global and local CSS stylesheets into the target [Parent] node.
     */
    fun addCSSIfAvailable(parent: Parent) {
        val list = PropertyReaderHelper[applicationContext.environment, "javafx.css"]
        if (list.isNotEmpty()) {
            list.forEach { css ->
                val resourceUri = javaClass.getResource(css)?.toExternalForm()
                    ?: throw ResourceNotFoundException("Cannot find resource '$css'")
                parent.stylesheets.add(resourceUri)
            }
        }

        addCSSFromAnnotation(parent)
        javaClass.getResource(styleSheetName)?.toExternalForm()?.let { conventionalCss ->
            parent.stylesheets.add(conventionalCss)
        }
    }

    /**
     * Helper to map and inject stylesheets declared via the [FXMLView] annotation.
     */
    private fun addCSSFromAnnotation(parent: Parent) {
        val cssFiles = annotation?.css ?: emptyArray()
        if (cssFiles.isNotEmpty()) {
            cssFiles.forEach { cssFile ->
                val uri = javaClass.getResource(cssFile)
                if (uri != null) {
                    parent.stylesheets.add(uri.toExternalForm())
                    logger.debug { "CSS file successfully injected from annotation: $cssFile" }
                } else {
                    logger.warn { "Referenced CSS file could not be located: $cssFile" }
                }
            }
        }
    }

    /**
     * The default title to be shown in a window, derived from the annotation.
     */
    val defaultTitle: String get() = annotation?.title.orEmpty()

    /**
     * The default style for the window stage.
     */
    val defaultStyle: StageStyle
        get() = annotation?.stageStyle?.let { style ->
            runCatching { StageStyle.valueOf(style.uppercase()) }.getOrElse { StageStyle.DECORATED }
        } ?: StageStyle.DECORATED

    /**
     * The default modality configuration for the window.
     */
    val defaultModality: Modality get() = annotation?.modality ?: Modality.NONE

    /**
     * Resolves the stylesheet resource path using conventional naming rules.
     */
    private val styleSheetName: String get() = fxmlRoot + getConventionalName(".css")

    /**
     * Retrieves the corresponding Spring-managed controller/presenter instance.
     * Initializes the view hierarchy if it hasn't been loaded yet.
     */
    val presenter: Any
        get() {
            ensureFxmlLoaderInitialized()
            return presenterProperty.get()
        }

    /**
     * Registers a callback listener to capture the construction of the presenter instance.
     */
    fun getPresenter(presenterConsumer: Consumer<Any?>) {
        presenterProperty.addListener { _, _, newValue -> presenterConsumer.accept(newValue) }
    }

    /**
     * Appends the designated file extension suffix to the conventional name.
     */
    private fun getConventionalName(ending: String): String = conventionalName + ending

    /**
     * Formats the view class name into its baseline convention (lowercase without the "view" suffix).
     */
    private val conventionalName: String
        get() = stripEnding(javaClass.simpleName.lowercase())

    /**
     * Evaluates and returns the target resource bundle name.
     */
    private val bundleName: String
        get() {
            val annotatedBundle = annotation?.bundle.orEmpty()
            return if (annotatedBundle.isEmpty()) {
                val conventionalBundle = "${javaClass.packageName}.$conventionalName"
                logger.debug { "Bundle: $conventionalBundle based on conventional name." }
                conventionalBundle
            } else {
                logger.debug { "Annotated bundle: $annotatedBundle" }
                annotatedBundle
            }
        }

    /**
     * Resolves the relative path to the FXML file derived from this view class structure.
     */
    val fxmlPath: String
        get() {
            val resolvedPath = fxmlRoot + getConventionalName(".fxml")
            logger.debug { "Determined fxmlPath: $resolvedPath" }
            return resolvedPath
        }

    /**
     * Safe lookup to retrieve a localized [ResourceBundle] using a specific charset control.
     */
    private fun getResourceBundle(name: String): ResourceBundle? {
        logger.debug { "Resource bundle lookup: $name" }
        return runCatching {
            ResourceBundle.getBundle(name, ResourceBundleControl(resourceBundleCharset))
        }.onFailure { ex ->
            logger.debug(ex) { "No resource bundle could be determined: ${ex.message}" }
        }.getOrNull()
    }

    /**
     * The target encoding charset specified in the annotation parameters.
     */
    private val resourceBundleCharset: Charset
        get() = runCatching {
            Charset.forName(annotation?.encoding ?: "UTF-8")
        }.getOrElse { Charset.defaultCharset() }

    companion object {
        private val logger = KotlinLogging.logger { }

        /**
         * Strips the trailing "view" suffix from the formatted class name.
         */
        private fun stripEnding(clazz: String): String =
            if (!clazz.endsWith("view")) clazz
            else clazz.substring(0, clazz.lastIndexOf("view"))
    }
}
