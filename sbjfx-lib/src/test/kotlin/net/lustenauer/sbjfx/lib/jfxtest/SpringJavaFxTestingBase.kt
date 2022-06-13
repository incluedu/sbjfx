package net.lustenauer.sbjfx.lib.jfxtest

import javafx.scene.Scene
import javafx.stage.Stage
import net.lustenauer.sbjfx.lib.AbstractFxmlView
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.BeansException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import java.lang.System.getProperty
import java.lang.System.setProperty

@SpringBootTest(classes = [SampleConfig::class])
@ExtendWith(SpringExtension::class, ApplicationExtension::class)
internal open class SpringJavaFxTestingBase : ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext
    private var controllerViewBean: AbstractFxmlView? = null

    internal open fun init(viewBean: AbstractFxmlView) {
        controllerViewBean = viewBean
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    @Start
    @Throws(Exception::class)
    open fun start(stage: Stage) {
        controllerViewBean?.let {
            with(stage) {
                scene = it.view.scene ?: Scene(it.view)
                show()
                centerOnScreen()
                toFront()
            }
        } ?: kotlin.run {
            throw Exception("The view is null! Have you called init() before?")
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            val headless = getProperty("JAVAFX_HEADLESS", "true").toBooleanStrict()
            val geometryProp = getProperty("JAVAFX_GEOMETRY", "1600x1200-32")
            if (headless) {
                setProperty("testfx.robot", "glass")
                setProperty("testfx.headless", "true")
                setProperty("prism.order", "sw")
                setProperty("prism.text", "t2k")
                setProperty("java.awt.headless", "true")
                setProperty("headless.geometry", geometryProp)
            } else {
                setProperty("java.awt.headless", "false")
            }
        }
    }
}
