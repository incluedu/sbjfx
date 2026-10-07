package net.lustenauer.sbjfx.lib

import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Application
import javafx.application.HostServices
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle
import net.lustenauer.sbjfx.lib.exceptions.ResourceNotFoundException
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import java.awt.SystemTray
import java.util.concurrent.CompletableFuture

/**
 * Abstract base class providing core bootstrap logic to bridge the lifecycles
 * of a Spring Boot application context and the JavaFX UI toolkit runtime.
 *
 * @author Felix Roske
 * @author Patric Hollenstein
 */
@Suppress("unused")
abstract class AbstractJavaFxApplicationSupport : Application() {
    private val defaultIcons: MutableList<Image> = mutableListOf()
    private val splashIsShowing: CompletableFuture<Runnable> = CompletableFuture()

    /**
     * Resolves and loads application icon resources from the current environment context.
     */
    private fun loadIcons(ctx: ConfigurableApplicationContext) {
        runCatching {
            val configuredIcons = PropertyReaderHelper[ctx.environment, KEY_APP_ICONS]
            if (configuredIcons.isNotEmpty()) {
                configuredIcons.forEach { iconPath -> icons.add(loadIcon(iconPath)) }
            } else {
                icons.addAll(defaultIcons)
            }
        }.onFailure { e ->
            logger.error(e) { "Failed to properly load application icons: ${e.message}" }
        }
    }

    /**
     * Initializer hook invoked by the JavaFX launcher thread.
     * Asynchronously starts the Spring Boot context.
     */
    @Throws(Exception::class)
    override fun init() {
        defaultIcons.addAll(loadDefaultIcons())

        CompletableFuture.supplyAsync {
            SpringApplication.run(this.javaClass, *savedArgs)
        }.whenComplete { ctx: ConfigurableApplicationContext?, throwable: Throwable? ->
            if (throwable != null) {
                logger.error(throwable) { "Failed to load Spring application context: " }
                Platform.runLater { errorAction(throwable) }
            } else {
                Platform.runLater {
                    ctx?.let {
                        loadIcons(it)
                        launchApplicationView(it)
                    }
                }
            }
        }.thenAcceptBothAsync(splashIsShowing) { _, closeSplash ->
            Platform.runLater(closeSplash)
        }
    }

    /**
     * Entry point invoked when the JavaFX toolkit initializes the primary window [Stage].
     */
    @Throws(Exception::class)
    override fun start(stage: Stage) {
        GUIState.stage = stage
        GUIState.hostServices = hostServices

        with(Stage(StageStyle.TRANSPARENT)) {
            val launchInitialView = Runnable {
                val initialView = savedInitialView
                if (initialView != null) {
                    showInitialView(initialView)
                } else {
                    logger.error { "savedInitialView is null! The application was likely not launched via the support bootstrap." }
                    Platform.exit()
                    kotlin.system.exitProcess(1)
                }
            }

            val currentSplash = splashScreen
            if (currentSplash == null) {
                splashIsShowing.complete(launchInitialView)
                return@with
            } else {
                if (currentSplash.visible) {
                    scene = Scene(currentSplash.parent, Color.TRANSPARENT)
                    beforeShowingSplash(this)
                    show()
                }

                splashIsShowing.complete(Runnable {
                    launchInitialView.run()
                    if (currentSplash.visible) {
                        close()
                    }
                })
            }
        }
    }

    /**
     * Sets the active initialized Spring application context instance.
     */
    private fun launchApplicationView(ctx: ConfigurableApplicationContext) {
        applicationContext = ctx
    }

    /**
     * Lifecycle shutdown hook. Ensures the Spring Boot context closes gracefully when JavaFX exits.
     */
    @Throws(Exception::class)
    override fun stop() {
        super.stop()
        if (isApplicationContextInitialized()) {
            applicationContext.close()
        }
    }

    /**
     * Hook method triggered right before the initial view is displayed.
     * Override this to append custom operations (e.g., AWT SystemTray setups).
     */
    open fun beforeInitialView(stage: Stage, ctx: ConfigurableApplicationContext?) {}

    /**
     * Hook method triggered immediately before the splash screen stage is shown.
     */
    open fun beforeShowingSplash(splashStage: Stage) {}

    /**
     * Populates the internal fallback system icon set.
     */
    fun loadDefaultIcons(): Collection<Image> = listOf(
        loadIcon("/icons/gear_16x16.png"),
        loadIcon("/icons/gear_24x24.png"),
        loadIcon("/icons/gear_36x36.png"),
        loadIcon("/icons/gear_42x42.png"),
        loadIcon("/icons/gear_64x64.png")
    )

    /**
     * Helper to load and format a raw graphics image resource asset.
     */
    private fun loadIcon(name: String): Image {
        val resourceUrl = javaClass.getResource(name)?.toExternalForm()
            ?: throw ResourceNotFoundException("Cannot find requested icon asset resource at path '$name'")
        return Image(resourceUrl)
    }

    companion object {
        private const val KEY_TITLE = "javafx.title"
        private const val KEY_STAGE_WIDTH = "javafx.stage.width"
        private const val KEY_STAGE_HEIGHT = "javafx.stage.height"
        private const val KEY_STAGE_RESIZABLE = "javafx.stage.resizable"
        private const val KEY_APP_ICONS = "javafx.appIcons"

        var savedInitialView: Class<out AbstractFxmlView>? = null
        var splashScreen: SplashScreen? = null
        lateinit var applicationContext: ConfigurableApplicationContext

        private val logger = KotlinLogging.logger { }
        private var savedArgs = emptyArray<String>()

        private val icons: MutableList<Image> = ArrayList()
        private var errorAction: (t: Throwable) -> Unit = defaultErrorAction()

        @JvmStatic
        val stage: Stage get() = GUIState.stage

        @JvmStatic
        val scene: Scene get() = GUIState.scene

        @JvmStatic
        val appHostServices: HostServices? get() = GUIState.hostServices

        @JvmStatic
        val systemTray: SystemTray? get() = GUIState.systemTray

        /**
         * Default error action fallback that displays a graphical error dialog and exits the platform execution context.
         */
        private fun defaultErrorAction(): (Throwable) -> Unit = {
            Alert(
                AlertType.ERROR,
                "Oops! An unrecoverable error occurred.\nPlease contact your software vendor.\n\nThe application will stop now."
            ).showAndWait().ifPresent { Platform.exit() }
        }

        /**
         * Dynamically reads and applies system environment properties configuration to the primary view window [Stage].
         */
        private fun applyEnvPropsToView() {
            val env = applicationContext.environment

            PropertyReaderHelper.setIfPresent(env, KEY_TITLE, String::class.java) { stage.title = it }
            PropertyReaderHelper.setIfPresent(env, KEY_STAGE_WIDTH, Double::class.java) { stage.width = it }
            PropertyReaderHelper.setIfPresent(env, KEY_STAGE_HEIGHT, Double::class.java) { stage.height = it }
            PropertyReaderHelper.setIfPresent(env, KEY_STAGE_RESIZABLE, Boolean::class.java) { stage.isResizable = it }
        }

        /**
         * Overwrites the window title dynamically at a later stage lifecycle execution point.
         */
        protected fun setTitle(title: String?) {
            stage.title = title
        }

        /**
         * Main launch hook variant that provisions a standard default splash window instance context.
         */
        fun launch(appClass: Class<out Application>, view: Class<out AbstractFxmlView>, args: Array<String>) =
            launch(appClass, view, SplashScreen(), args)

        /**
         * Entry framework method that triggers the initialization sequence for both JavaFX and the Spring environment.
         */
        @JvmStatic
        fun launch(
            appClass: Class<out Application>,
            view: Class<out AbstractFxmlView>,
            splashScreen: SplashScreen?,
            args: Array<String>
        ) {
            savedInitialView = view
            savedArgs = args
            Companion.splashScreen = splashScreen ?: SplashScreen()

            if (SystemTray.isSupported()) {
                GUIState.systemTray = SystemTray.getSystemTray()
            }
            launch(appClass, *args)
        }

        /**
         * Instructs the ApplicationContext environment to resolve the designated view bean instance and display it.
         */
        @JvmStatic
        fun showInitialView(newView: Class<out AbstractFxmlView>) {
            runCatching {
                val view = applicationContext.getBean(newView)
                view.initFirstView()
                applyEnvPropsToView()
                stage.icons.addAll(icons)
                stage.show()
            }.onFailure { throwable ->
                logger.error(throwable) { "Failed to properly bootstrap and load initial application view: ${throwable.message}" }
                errorAction(throwable)
            }
        }

        /**
         * Allows providing an extension callback method hook to customize behavior in case an unhandled lifecycle exception is caught.
         */
        @JvmStatic
        fun setErrorAction(callback: (throwable: Throwable) -> Unit) {
            errorAction = callback
        }

        internal fun isApplicationContextInitialized() = ::applicationContext.isInitialized
    }
}
