package com.zwendo.restrikt.plugin.frontend

import com.zwendo.restrikt_compiler_plugin.BuildConfig

internal object PluginConfiguration {

    var keepAnnotations: Boolean = true

    var defaultReason: String = BuildConfig.DEFAULT_REASON

    var enabled: Boolean = true

}
