package online.christopherstocks.attributum.libs

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

@Suppress("SqlResolve")
class MySQL : Storage() {

    override fun createDatabase() {
        val connection = openConnection()
        Bukkit.getScheduler().runTaskAsynchronously(config.getInstance(), Runnable {
            connection.prepareStatement("CREATE DATABASE IF NOT EXISTS $database;").executeUpdate()
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS $database.${table_prefix}Attributes (attribute varchar(3072) NOT NULL UNIQUE);").executeUpdate()
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS $database.${table_prefix}Players (uuid varchar(3072) NOT NULL, username varchar(3072) NOT NULL, slot bigint NOT NULL, last bigint NOT NULL);").executeUpdate()
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS $database.${table_prefix}Active (uuid varchar(3072) NOT NULL UNIQUE, username varchar(3072) NOT NULL, active bigint UNSIGNED NOT NULL);").executeUpdate()
            connection.close()
            updateAttributes()
        })
    }

    // This will serve updates to databases should there be any additions or removals!
    override fun updatePlugin() {
        val connection = openConnection()
        // V6.1.0.2
        Bukkit.getScheduler().runTaskAsynchronously(config.getInstance(), Runnable {
            connection.prepareStatement("ALTER TABLE $database.${table_prefix}Players DROP COLUMN id").executeUpdate()
            connection.close()
        })
    }


    override fun updateAttributes() {
        val connection = openConnection()
        val attributes = config.getConfig().getStringList("attributes")
        Bukkit.getScheduler().runTaskAsynchronously(config.getInstance(), Runnable {
            val resultSet = connection.prepareStatement("SELECT attribute FROM $database.${table_prefix}Attributes;").executeQuery()
            val stored = ArrayList<String>()
            if (resultSet.isBeforeFirst) {
                while (resultSet.next()) stored.add(resultSet.getString("attribute"))

                attributes.forEach {
                    if (!stored.contains(it)) {
                        connection.prepareStatement("ALTER TABLE $database.${table_prefix}Players ADD COLUMN $it bigint;").executeUpdate()
                        connection.prepareStatement("INSERT IGNORE INTO $database.${table_prefix}Attributes (attribute) VALUES ('$it');").executeUpdate()
                    }
                }
                stored.forEach {
                    if (!attributes.contains(it)) {
                        connection.prepareStatement("ALTER TABLE $database.${table_prefix}Players DROP COLUMN $it;").executeUpdate()
                        connection.prepareStatement("DELETE FROM $database.${table_prefix}Attributes WHERE attribute = '$it';").executeUpdate()
                    }
                }
            } else {
                attributes.forEach {
                    connection.prepareStatement("ALTER TABLE $database.${table_prefix}Players ADD COLUMN $it bigint").executeUpdate()
                    connection.prepareStatement("INSERT IGNORE INTO $database.${table_prefix}Attributes (attribute) VALUES ('$it');").executeUpdate()
                }
            }
            connection.close()
        })
    }

    override fun deleteInactivePlayers() {
        val connection = openConnection()
        if (inactive == 0.00) {
            connection.close()
            return
        }
        else {
            val delete = System.currentTimeMillis() - (inactive * 8.64e+7)
            Bukkit.getScheduler().runTaskAsynchronously(config.getInstance(), Runnable {
                connection.prepareStatement("DELETE FROM $database.${table_prefix}Players WHERE '$delete' > last").executeUpdate()
                connection.close()
            })
        }
    }

    override fun createPlayer(player: Player) {
        val connection = openConnection()
        val name = player.name
        val uuid = player.uniqueId.toString()
        val time = System.currentTimeMillis()
        Bukkit.getScheduler().runTaskAsynchronously(config.getInstance(), Runnable {
            for (i in 0 until config.getConfig().getInt("slots")) {
                connection.prepareStatement("INSERT IGNORE INTO $database.${table_prefix}Players (uuid, username, slot, last) VALUES ('$uuid', '$name', '$i', '$uuid:$i', '$time');").executeUpdate()
            }
            connection.prepareStatement("INSERT IGNORE INTO $database.${table_prefix}Active (uuid, username, active) VALUES ('$uuid', '$name', 0);").executeUpdate()
            connection.close()
        })
    }

    override fun updatePlayer(player: Player) {
        val connection = openConnection()
        val name = player.name
        val uuid = player.uniqueId.toString()
        val time = System.currentTimeMillis()
        Bukkit.getScheduler().runTaskAsynchronously(config.getInstance(), Runnable {
            connection.prepareStatement("UPDATE $database.${table_prefix}Players SET username = '$name', last = '$time' WHERE uuid = '$uuid';").executeUpdate()
            connection.prepareStatement("UPDATE $database.${table_prefix}Active SET username = '$name' WHERE uuid = '$uuid';").executeUpdate()
            connection.close()
        })
    }

    override fun openConnection(): Connection {
        return DriverManager.getConnection("jdbc:mysql://:host:::port:/?verifyServerCertificate=true&useSSL=true&autoReconnect=true&".replace(":host:", config.getConfig().getString("host")!!)
                .replace(":port:", config.getConfig().getString("port")!!), config.getConfig().getString("username"), config.getConfig().getString("password"))
    }

    override fun getAttribute(name: String, attribute: String, slot: Int): Int {
        val connection = openConnection()
        val resultSet = connection.prepareStatement("SELECT $attribute FROM $database.${table_prefix}Players WHERE username = '$name' AND slot = '$slot'").executeQuery()
        if (!resultSet.isBeforeFirst) {
            connection.close()
            return 0
        }
        resultSet.next()
        val result = resultSet.getString(attribute)
        if (result == null) {
            connection.close()
            return 0
        }
        connection.close()
        return Integer.parseInt(result)
    }

    override fun setAttribute(name: String, attribute: String, amount: Int, slot: Int) {
        val connection = openConnection()
        connection.prepareStatement("UPDATE $database.${table_prefix}Players SET $attribute = '$amount' WHERE username = '$name' AND slot = '$slot'").executeUpdate()
        connection.close()
    }

    override fun checkPlayerExists(name: String): Boolean {
        val connection = openConnection()
        val resultSet = connection.prepareStatement("SELECT COUNT(slot) FROM $database.${table_prefix}Players WHERE username = '$name'").executeQuery()
        if (resultSet.next()) {
            val exists = resultSet.getInt(1) == config.getConfig().getInt("slots")
            connection.close()
            return exists
        }
        connection.close()
        return false
    }

    override fun getActive(name: String): Int {
        val connection = openConnection()
        val resultSet = connection.prepareStatement("SELECT active FROM $database.${table_prefix}Active WHERE username = '$name'").executeQuery()
        if (!resultSet.isBeforeFirst) {
            connection.close()
            return 0
        }
        resultSet.next()
        val active = resultSet.getInt("active")
        connection.close()
        return active
    }

    override fun setActive(name: String, value: Int) {
        val connection = openConnection()
        connection.prepareStatement("UPDATE $database.${table_prefix}Active SET active = '$value' WHERE username = '$name'").executeUpdate()
        connection.close()
    }

    override fun reset(name: String, slot: Int) {
        val connection = openConnection()
        config.getStringList("attributes").forEach {attribute-> connection.prepareStatement("UPDATE $database.${table_prefix}Players SET $attribute = 0 WHERE slot = $slot AND username = '$name'").executeUpdate() }
        connection.close()
    }
}