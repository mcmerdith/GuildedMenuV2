package net.mcmerdith.guildedmenu.integration

import net.mcmerdith.guildedmenu.GuildedMenu

object IntegrationManager {
    private val integrations: MutableMap<Class<out Integration>, Integration> = HashMap()

    fun register(clazz: Class<out Integration>, i: Integration) {
        integrations[clazz] = i
    }

    fun setup() {
        for (i in integrations.values) if (i.pluginInstalled) i.setup()
    }

    fun enable() {
        for (i in integrations.values) {
            if (!i.pluginEnabled) continue

            if (i.onEnable()) {
                i.resetError()
                GuildedMenu.plugin.logger.info("Enabled " + i.pluginName + " integration")
            } else {
                i.setError()
                GuildedMenu.plugin.logger.warning("Failed to enable " + i.pluginName + " integration")
            }
        }
    }

    /**
     * Gets an integration
     *
     * @param integration The integration to retrieve
     * @param <T>   something...
     * @return The integration, if available, otherwise null
    </T> */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Integration> get(integration: Class<T>): T? {
        val i = integrations[integration] ?: return null
        return i as T
    }

    init {
        register(SignShopIntegration::class.java, SignShopIntegration())
        register(VaultIntegration::class.java, VaultIntegration())
        register(EssentialsIntegration::class.java, EssentialsIntegration())
    }
}