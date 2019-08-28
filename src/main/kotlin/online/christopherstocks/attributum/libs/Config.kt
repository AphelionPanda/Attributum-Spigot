package online.christopherstocks.attributum.libs

import online.christopherstocks.attributum.Attributum
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

class Config {

    fun getInstance(): Plugin { return Attributum.instance }

    fun getConfig(): FileConfiguration { return getInstance().config }
    fun getString(path: String): String { return getConfig().getString(path) ?: "Empty" }
    fun getStringList(path: String): MutableList<String> { return getConfig().getStringList(path)}
    fun getBoolean(path: String): Boolean { return getConfig().getBoolean(path)}
    fun getDouble(path: String): Double { return getConfig().getDouble(path) }
    fun getInt(path: String): Int { return getConfig().getInt(path) }

    fun set(path: String, value: Any?) { getConfig().set(path, value) }


    fun disableSlots() { getConfig().set("slot-handler", true) }
    fun enableSlots() { getConfig().set("slot-handler", false) }

    fun reloadConfig() { getInstance().reloadConfig() }

    fun isInteger(string: String) = (string.toIntOrNull() != null)

    fun getStorage(): Storage {
        when(getConfig().getString("storage")!!.toLowerCase()){
            "mysql" -> return MySQL()
            else -> return MySQL()
        }
    }

    fun checkSlot(slot: Int): Boolean { return slot > -1 && slot < getInt("slots") }

}