package net.lustenauer.sbjfx.lib.jfxtest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal open class SampleConfig {
    @Bean
    open fun testView(): SampleView = SampleView()

    @Bean
    open fun erroredView(): SampleIncorrectView = SampleIncorrectView()
}
