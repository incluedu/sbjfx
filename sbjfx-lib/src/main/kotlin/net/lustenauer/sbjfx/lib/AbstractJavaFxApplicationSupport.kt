package net.lustenauer.sbjfx.lib

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
import javafx.stage.StageStyle.DECORATED
import javafx.stage.StageStyle.TRANSPARENT
import mu.KotlinLogging
import net.lustenauer.sbjfx.lib.PropertyReaderHelper.setIfPresent
import net.lustenauer.sbjfx.lib.exceptions.ResourceNotFoundException
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import java.awt.SystemTray
import java.util.*
import java.util.concurrent.CompletableFuture


/**
 * The Class AbstractJavaFxApplicationSupport.
 *
 * @author Felix Roske
 * @author Patric Hollenstein
 */
abstract class AbstractJavaFxApplicationSupport : Application() {
    private val defaultIcons: MutableList<Image> = mutableListOf()
    private val splashIsShowing: CompletableFuture<Runnable> = CompletableFuture()

    private fun loadIcons(ctx: ConfigurableApplicationContext) {
        try {
            PropertyReaderHelper[ctx.environment, KEY_APP_ICONS]
                .map { icons.add(loadIcon(it)) }
                .ifEmpty { icons.addAll(defaultIcons) }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load icons: " }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javafx.application.Application#init()
     */
    @Throws(Exception::class)
    override fun init() {
        // Load in JavaFx Thread and reused by Completable Future, but should not be a big deal.
        defaultIcons.addAll(loadDefaultIcons())
        CompletableFuture.supplyAsync { SpringApplication.run(this.javaClass, *savedArgs) }
            .whenComplete { ctx, throwable ->
                if (throwable != null) {
                    logger.error(throwable) { "Failed to load spring application context: " }
                    Platform.runLater { errorAction(throwable) }
                } else {
                    Platform.runLater {
                        loadIcons(ctx)
                        launchApplicationView(ctx)
                    }
                }
            }
            .thenAcceptBothAsync(splashIsShowing) { _, closeSplash ->
                Platform.runLater(closeSplash)
            }
    }

    /*
     * (non-Javadoc)
     *
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Throws(Exception::class)
    override fun start(stage: Stage) {
        GUIState.stage = stage
        GUIState.hostServices = hostServices

        with(Stage(TRANSPARENT)) {
            if (splashScreen.visible) {
                scene = Scene(splashScreen.parent, Color.TRANSPARENT)
                beforeShowingSplash(this)
                show()
            }

            splashIsShowing.complete(Runnable {
                showInitialView()
                if (splashScreen.visible) {
                    close()
                }
            })
        }
    }

    /**
     * Show initial view.
     */
    private fun showInitialView() {
        val stageStyle = applicationContext.environment.getProperty(KEY_STAGE_STYLE)
        if (stageStyle != null) {
            stage.initStyle(StageStyle.valueOf(stageStyle.uppercase(Locale.getDefault())))
        } else {
            stage.initStyle(DECORATED)
        }
        beforeInitialView(stage, applicationContext)
        showInitialView(savedInitialView)
    }

    /**
     * Launch application view.
     */
    private fun launchApplicationView(ctx: ConfigurableApplicationContext) {
        applicationContext = ctx
    }

    /*
     * (non-Javadoc)
     *
     * @see javafx.application.Application#stop()
     */
    @Throws(Exception::class)
    override fun stop() {
        super.stop()
        if (isApplicationContextInitialized()) applicationContext.close()
    }

    /**
     * Gets called after full initialization of Spring application context
     * and JavaFX platform right before the initial view is shown.
     * Override this method as a hook to add special code for your app. Especially meant to
     * add AWT code to add a system tray icon and behavior by calling
     * GUIState.getSystemTray() and modifying it accordingly.
     *
     *
     * By default, noop.
     *
     * @param stage can be used to customize the stage before being displayed
     * @param ctx   represents spring ctx where you can look for beans.
     */
    open fun beforeInitialView(stage: Stage, ctx: ConfigurableApplicationContext?) {}

    /**
     * Extension point called before show splash screen
     */
    open fun beforeShowingSplash(splashStage: Stage) {}

    /**
     * todo check private is ok for this function or not
     */
    fun loadDefaultIcons(): Collection<Image> = listOf(
        loadIcon("/icons/gear_16x16.png"),
        loadIcon("/icons/gear_24x24.png"),
        loadIcon("/icons/gear_36x36.png"),
        loadIcon("/icons/gear_42x42.png"),
        loadIcon("/icons/gear_64x64.png")
    )

    private fun loadIcon(name: String): Image = Image(
        javaClass.getResource(name)?.toExternalForm()
            ?: throw ResourceNotFoundException("cannot find resource $name")
    )

    companion object {
        private const val KEY_TITLE = "javafx.title"
        private const val KEY_STAGE_WIDTH = "javafx.stage.width"
        private const val KEY_STAGE_HEIGHT = "javafx.stage.height"
        private const val KEY_STAGE_RESIZABLE = "javafx.stage.resizable"
        private const val KEY_STAGE_STYLE = "javafx.stage.style"
        private const val KEY_APP_ICONS = "javafx.appIcons"


        lateinit var savedInitialView: Class<out AbstractFxmlView>
        lateinit var splashScreen: SplashScreen
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
         * Default error action that shows a message and closes the app.
         */
        private fun defaultErrorAction(): (Throwable) -> Unit = {
            Alert(
                AlertType.ERROR,
                "Oops! An unrecoverable error occurred.\nPlease contact your software vendor.\n\n" +
                        "The application will stop now."
            ).showAndWait().ifPresent { Platform.exit() }
        }
        /**
         * Apply env props to view.
         */
        private fun applyEnvPropsToView() {
            val env = applicationContext.environment
            setIfPresent(env, KEY_TITLE, String::class.java) { stage.title = it }
            setIfPresent(env, KEY_STAGE_WIDTH, Double::class.java) { stage.width = it }
            setIfPresent(env, KEY_STAGE_HEIGHT, Double::class.java) { stage.height = it }
            setIfPresent(env, KEY_STAGE_RESIZABLE, Boolean::class.java) { stage.isResizable = it }
        }

        /**
         * Sets the title. Allows overwriting values applied during construction at
         * a later time.
         *
         * @param title the new title
         */
        protected fun setTitle(title: String?) {
            stage.title = title
        }

        /**
         * Launch app.
         *
         * @param appClass the app class
         * @param view     the view
         * @param args     the args
         */
        fun launch(appClass: Class<out Application>, view: Class<out AbstractFxmlView>, args: Array<String>) =
            launch(appClass, view, SplashScreen(), args)

        /**
         * todo doc is missing
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
            if (splashScreen != null) {
                Companion.splashScreen = splashScreen
            } else {
                Companion.splashScreen = SplashScreen()
            }
            if (SystemTray.isSupported()) {
                GUIState.systemTray = SystemTray.getSystemTray()
            }
            launch(appClass, *args)
        }

        /**
         * Show view.
         *
         * @param newView the new view
         */
        @JvmStatic
        fun showInitialView(newView: Class<out AbstractFxmlView>) {
            try {
                val view = applicationContext.getBean(newView)
                view.initFirstView()
                applyEnvPropsToView()
                stage.icons.addAll(icons)
                stage.show()
            } catch (throwable: Throwable) {
                logger.error(throwable) { "Failed to load application: " }
                errorAction(throwable)
            }
        }

        /**
         * Extension point to override the error action
         */
        @JvmStatic
        fun setErrorAction(callback: (throwable: Throwable) -> Unit) {
            errorAction = callback
        }
        internal fun isApplicationContextInitialized() = ::applicationContext.isInitialized
    }
}
