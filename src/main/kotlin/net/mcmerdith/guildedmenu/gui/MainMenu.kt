package net.mcmerdith.guildedmenu.gui

import net.mcmerdith.guildedmenu.GuildedMenu
import net.mcmerdith.guildedmenu.gui.framework.BaseMenu
import net.mcmerdith.guildedmenu.gui.framework.BasicMenu
import net.mcmerdith.guildedmenu.gui.util.GuiUtil.openOnClick
import net.mcmerdith.guildedmenu.gui.util.ItemTemplates
import net.mcmerdith.guildedmenu.integration.EssentialsIntegration
import net.mcmerdith.guildedmenu.integration.IntegrationManager
import net.mcmerdith.guildedmenu.integration.vault.VaultIntegration

/**
 * Main Menu
 *
 * If [admin] is true a "Switch to Admin View" button will be rendered
 */
class MainMenu(private val admin: Boolean = false) : BasicMenu() {
    private val config = GuildedMenu.plugin.menuConfig

    override fun getBuilder(): BaseMenu.Builder = BaseMenu.Builder(5).title(GuildedMenu.plugin.menuConfig.title)

    override fun setup(menu: BaseMenu) {
        menu.apply {
            if (admin) {
                // Render the "Admin View" button
                getSlot(config.admin.mainButton.index).apply {
                    item = ItemTemplates.UI.getExclamation("Admin View")
                    openOnClick(AdminMenu(this@MainMenu))
                }
            }

            if (config.vault.enabled
                && IntegrationManager[VaultIntegration::class.java]?.run { ready && hasEconomy() } == true
            ) {
                // BALTOP
                getSlot(config.vault.index).apply {
                    item = ItemTemplates.getEconomyIcon()
                    openOnClick(EconomyMenu(this@MainMenu), PlayerBalanceMenu(this@MainMenu))
                }
            }

            val essentials = IntegrationManager[EssentialsIntegration::class.java]

            if (essentials?.ready == true) {
                if (config.tpa.enabled) {
                    // TPA
                    getSlot(config.tpa.index).apply {
                        item = ItemTemplates.getTPAIcon()
                        openOnClick(
                            PlayerSelectMenu(this@MainMenu, true, null, essentials.getTPAExecutor())
                        )
                    }
                }

                if (config.tpaHere.enabled) {
                    // TPA HERE
                    getSlot(config.tpaHere.index).apply {
                        item = ItemTemplates.getTPAHereIcon()
                        openOnClick(
                            PlayerSelectMenu(this@MainMenu, true, null, essentials.getTPAHereExecutor())
                        )
                    }
                }
            }

            if (config.signshop.enabled) {
                getSlot(config.signshop.index).apply {
                    item = ItemTemplates.getSignshop()
                    openOnClick(BusinessSelectMenu(this@MainMenu))
                }
            }
        }
    }
}