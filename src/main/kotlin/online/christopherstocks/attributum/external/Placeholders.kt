package online.christopherstocks.attributum.external

import me.clip.placeholderapi.external.EZPlaceholderHook
import online.christopherstocks.attributum.libs.Config
import online.christopherstocks.attributum.libs.Storage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class Placeholders(instance: Plugin) : EZPlaceholderHook(instance, "attributum") {

    override fun onPlaceholderRequest(player: Player, s: String?): String? {
        val config = Config()
        val storage: Storage = config.getStorage()
        if (storage.checkPlayerExists(player.name)) storage.updatePlayer(player)
        else storage.createPlayer(player)

        if (s.equals("slot", ignoreCase = true)) return (storage.getActive(player.name) + 1).toString()
        config.getConfig().getStringList("attributes").forEach {if (s.equals(it, ignoreCase = true)) return storage.getAttribute(player.name, it).toString()}

        return "Empty"
    }
}