package online.christopherstocks.attributum

import online.christopherstocks.attributum.commands.AttributesCommand
import online.christopherstocks.attributum.external.Placeholders
import online.christopherstocks.attributum.libs.Config
import online.christopherstocks.attributum.libs.Storage
import online.christopherstocks.attributum.listeners.PlayerJoin
import org.bukkit.Bukkit

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin


class Attributum : JavaPlugin() {

    companion object {
        lateinit var instance: Plugin
    }

    override fun onEnable() {
        saveDefaultConfig(); instance = this
        val pluginManager = server.pluginManager; val config = Config(); val storage: Storage = config.getStorage()
        config.enableSlots(); storage.createDatabase()
        pluginManager.registerEvents(PlayerJoin(), this)
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getConfig().set("placeholders", true)
            Placeholders(this).hook()
        } else getConfig().set("placeholders", false)
        if (config.getInt("slots") < 1) config.set("slots", 1)
        getCommand("attributum")!!.setExecutor(AttributesCommand())
        for (p in Bukkit.getOnlinePlayers()) {
            if (storage.checkPlayerExists(p.name)) storage.updatePlayer(p)
            else if (p.hasPermission("attributum.use")) { storage.createPlayer(p); storage.updatePlayer(p) }
        }
    }

}