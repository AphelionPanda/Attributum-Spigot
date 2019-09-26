package online.christopherstocks.attributum.libs

import org.bukkit.entity.Player
import java.sql.Connection

abstract class Storage {

    val config = Config()
    val database = config.getConfig().getString("database")!!
    val table_prefix = config.getConfig().getString("table_prefix")!!
    val inactive = config.getConfig().getDouble("inactive-days")

    // Async
    abstract fun createDatabase()
    abstract fun updatePlugin()
    abstract fun updateAttributes()
    abstract fun deleteInactivePlayers()
    abstract fun createPlayer(player: Player)
    abstract fun updatePlayer(player: Player)

    // Sync
    abstract fun openConnection(): Connection

    abstract fun getAttribute(name: String, attribute: String, slot: Int = getActive(name)): Int
    abstract fun setAttribute(name: String, attribute: String, amount: Int, slot: Int = getActive(name))

    abstract fun checkPlayerExists(name: String): Boolean
    abstract fun getActive(name: String): Int
    abstract fun setActive(name: String, value: Int)
    abstract fun reset(name: String, slot: Int = getActive(name))
}