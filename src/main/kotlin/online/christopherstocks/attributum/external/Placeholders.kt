package online.christopherstocks.attributum.external

import online.christopherstocks.attributum.libs.Config
import online.christopherstocks.attributum.libs.Storage
import org.bukkit.entity.Player
import me.clip.placeholderapi.expansion.PlaceholderExpansion

class Placeholders : PlaceholderExpansion() {
    val config = Config()
    val plugin = config.getInstance();

    override fun persist(): Boolean { return true }
    override fun canRegister(): Boolean { return true }
    override fun getAuthor(): String { return plugin.getDescription().getAuthors().toString() }
    override fun getIdentifier(): String { return "attributum" }
    override fun getVersion(): String { return plugin.getDescription().getVersion() }

    override fun onPlaceholderRequest(player: Player?, identifier: String?): String? {
        if (player == null){ return "" }
        val storage: Storage = config.getStorage()
        if (storage.checkPlayerExists(player.name)) storage.updatePlayer(player)
        else storage.createPlayer(player)

        if (identifier.equals("slot", ignoreCase = true)) return (storage.getActive(player.name) + 1).toString()
        config.getConfig().getStringList("attributes").forEach {if (identifier.equals(it, ignoreCase = true)) return storage.getAttribute(player.name, it).toString()}

        return "Empty"
    }
}