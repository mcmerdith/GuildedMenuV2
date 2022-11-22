package net.mcmerdith.guildedmenu.integration

import com.palmergames.bukkit.towny.Towny
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.command.TownCommand
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import com.palmergames.bukkit.towny.`object`.TownyPermission
import com.palmergames.bukkit.towny.`object`.TownyPermission.ActionType
import com.palmergames.bukkit.towny.`object`.TownyPermission.PermLevel
import com.palmergames.bukkit.towny.permissions.TownyPerms
import com.palmergames.bukkit.towny.utils.MoneyUtil
import net.mcmerdith.guildedmenu.gui.util.ItemTemplates
import net.mcmerdith.guildedmenu.util.ItemStackUtils.setLore
import net.mcmerdith.guildedmenu.util.ItemStackUtils.setName
import net.mcmerdith.guildedmenu.util.capitalize
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class TownyIntegration : Integration("Towny") {
    override fun onEnable(): Boolean {
        return true
    }

    private val api: TownyAPI by lazy { TownyAPI.getInstance() }
    private val townCommand: TownCommand by lazy { TownCommand(Towny.getPlugin()) }

    /**
     * Get a [Resident] for [uuid]
     *
     * Returns null if Towny does not have a [Resident] registered for [uuid]
     */
    fun getResident(uuid: UUID) = api.getResident(uuid)

    /**
     * Get all town ranks
     */
    fun getTownRanks(): List<String> = TownyPerms.getTownRanks()

    /**
     * Get all nation ranks
     */
    fun getNationRanks(): List<String> = TownyPerms.getNationRanks()

    /**
     * Claim [count] town blocks for [player]
     *
     * [shape]: true=rect, false=circle
     */
    fun townClaim(player: Player, count: Int, shape: Boolean = true) =
        TownCommand.parseTownClaimCommand(
            player,
            arrayOf(
                if (shape) "rect" else "circle",
                if (count < 0) "auto" else count.toString()
            )
        )

    /**
     * Claim maximum town blocks for [player]
     */
    fun townClaimAuto(player: Player) =
        TownCommand.parseTownClaimCommand(
            player,
            arrayOf("auto")
        )

    /**
     * Claim output for [player]
     */
    fun townClaimOutpost(player: Player) =
        TownCommand.parseTownClaimCommand(
            player,
            arrayOf("outpost")
        )

    /**
     * Teleport [player] to spawn of [outpostId]
     */
    fun townOutpostSpawn(player: Player, outpostId: Int) =
        TownCommand.townSpawn(player, arrayOf(outpostId.toString()), true, false)

    /**
     * Delete a [town]
     *
     * [player]'s town will be used if [town] is not provided
     */
    fun deleteTown(player: Player, town: Town? = null) {
        townCommand.townDelete(
            player,
            town?.let { arrayOf(town.name) } ?: arrayOf()
        )
    }

    /**
     * Teleport [player] to the spawn of [town]
     *
     * [player]'s hometown will be used if [town] is null
     */
    fun townSpawn(player: Player, town: Town? = null) {
        TownCommand.townSpawn(
            player,
            town?.let { arrayOf(it.name) } ?: arrayOf(),
            false,
            false
        )
    }

    /**
     * Returns a map of each rank for [town] to a list of [Resident]s with that rank
     */
    fun townRankList(town: Town): Map<String, List<Resident>> {
        val res = mutableMapOf<String, List<Resident>>()

        for (rank in getTownRanks()) {
            res[rank] = town.getRank(rank)
        }

        return res
    }

    /**
     * [player]: The calling player
     *
     * [action]: true=add, false=remove
     *
     * Add/remove [rank] from [target] ([player] if not provided)
     */
    fun townRankUpdate(player: Player, action: Boolean, target: Player?, rank: String) {
        townCommand.townRank(
            player, arrayOf(
                if (action) "add" else "remove",
                target?.name ?: player.name,
                rank
            )
        )
    }

    /**
     * [player]: The calling player
     *
     * Deposit [amount] to [town] from [resident]'s account
     */
    fun townDeposit(player: Player, resident: Resident, town: Town, amount: Int) {
        MoneyUtil.townDeposit(player, resident, town, null, amount)
    }

    /**
     * [player]: The calling player
     *
     * Withdraw [amount] from [town] to [resident]'s account
     */
    fun townWithdraw(player: Player, resident: Resident, town: Town, amount: Int) {
        MoneyUtil.townWithdraw(player, resident, town, amount)
    }

    /**
     * [player]: The calling player
     *
     * Set [town]'s board to [board]
     */
    fun townSetBoard(player: Player, town: Town, board: String) {
        TownCommand.townSet(player, arrayOf("board", *board.split(" ").toTypedArray()), false, town)
    }

    /**
     * [player]: The calling player
     *
     * Set [town]'s [action] permission for [level] to [value]
     */
    fun townSetPerm(player: Player, action: ActionType, level: PermLevel, value: Boolean, town: Town) {
        TownCommand.townSet(
            player, arrayOf(
                "perm", level.name, action.name,
                if (value) "on" else "off"
            ), false, town
        )
    }

    /**
     * Get an item representing [town]'s current permission level for [action]
     */
    fun getPermissionsItem(action: ActionType, town: Town): ItemStack {
        val lore = mutableListOf<String>()

        town.permissions.let { tPerm ->
            for (perm in TownyPermission.PermLevel.values()) {
                lore.add(
                    "${perm.name.capitalize()}: ${
                        if (tPerm.getPerm(perm, action)) "${ChatColor.GREEN}Yes"
                        else "${ChatColor.RED}No"
                    }"
                )
            }
        }

        return ItemTemplates.UI.getInfo(action.commonName).setLore(lore)
    }

    /**
     * Get an item representing [town]'s current permission of [level] for [action]
     */
    fun getPermissionItem(action: ActionType, level: PermLevel, town: Town): ItemStack {
        val allowed = town.permissions.getPerm(level, action)

        return ItemStack(if (allowed) Material.GREEN_CONCRETE else Material.RED_CONCRETE)
            .setName(action.commonName)
            .setLore(
                "${level.name.capitalize()}: ${
                    if (allowed) "${ChatColor.GREEN}Yes"
                    else "${ChatColor.RED}No"
                }"
            )
    }

    enum class Settings(val description: String) {
        EXPLOSION("Explosions"),
        FIRE("Fire Spread"),
        MOBS("Mob Spawning"),
        PUBLIC("Public Spawn TP and Visible Coordinates"),
        PVP("PVP"),
        TAXPERCENT("Taxation (percent)"),
        NATIONZONE("Nation Zone?"),
        OPEN("Public Joining");

        /**
         * Get an item representing this setting if [enabled]
         */
        fun getItem(enabled: Boolean): ItemStack {
            return if (enabled) {
                ItemStack(Material.LIME_CONCRETE).setName(description).setLore("${ChatColor.GREEN}Enabled")
            } else {
                ItemStack(Material.RED_CONCRETE).setName(description).setLore("${ChatColor.RED}Disabled")
            }
        }

        /**
         * [player]: The calling player
         *
         * Toggle this setting for [town]
         */
        fun toggle(player: Player, town: Town) {
            TownCommand.townToggle(player, arrayOf(name), false, town)
        }
    }
}