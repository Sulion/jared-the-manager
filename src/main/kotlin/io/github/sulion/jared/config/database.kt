package io.github.sulion.jared.config

import org.jooq.conf.RenderNameStyle
import org.jooq.conf.Settings

object DSL_CONFIG {
    val settings = Settings().withRenderNameStyle(RenderNameStyle.LOWER)
}