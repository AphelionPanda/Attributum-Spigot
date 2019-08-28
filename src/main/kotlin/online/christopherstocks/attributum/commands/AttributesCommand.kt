package online.christopherstocks.attributum.commands

import online.christopherstocks.attributum.libs.ChatOptions
import online.christopherstocks.attributum.libs.Config
import online.christopherstocks.attributum.libs.Storage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.logging.Level

class AttributesCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, s: String, args: Array<String>): Boolean {
        val config = Config()
        val chatOptions = ChatOptions(sender)

        if (sender !is Player) { chatOptions.sendMessage(config.getString("sender")); return false }

        val player: Player = sender; val storage: Storage = Config().getStorage()

        if (!player.hasPermission("attributum.use")){ chatOptions.sendMessage(config.getString("permission")); return false }
        if (!storage.checkPlayerExists(player.name)) storage.createPlayer(player)
        storage.updatePlayer(player)

        if (args.size == 0) { chatOptions.sendMessage(config.getString("help")); return true }

        // /attributum help [o:page]
        if (args[0].equals("help", ignoreCase = true)){
            var page = 1; val items = config.getInt("help-items"); val pageItems = config.getStringList("help-pages")
            if (args.size == 2 && config.isInteger(args[1])) page = args[1].toInt()
            val pages = Math.ceil(pageItems.size.toDouble() / items)
            if (page > pages || page <= 0) { chatOptions.sendMessage(config.getString("help-invalid")); return true }
            for (i in (page - 1) * items until page * items){
                if (i >= pageItems.size) break
                if (pageItems.get(i).equals("empty", ignoreCase = true)) continue
                if (pageItems.get(i).equals("spacer", ignoreCase = true)) chatOptions.sendMessage("")
                else chatOptions.sendMessage(pageItems.get(i).replace(":page:".toRegex(), page.toString()).replace(":pages:".toRegex(), pages.toInt().toString()))
            }
            return true
        }

        // /attributum reload
        if (args[0].equals("reload", ignoreCase = true)){
            if (!player.hasPermission("attributum.reload")) { chatOptions.sendMessage(config.getString("permission")); return false }
            config.reloadConfig(); chatOptions.sendMessage(config.getString("reload")); return true
        }

        // /attributum debug [paramaters] ...
        if (args[0].equals("debug", ignoreCase = true)){
            if (!player.hasPermission("attributum.debug")){ chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size < 2) { chatOptions.sendMessage(config.getString("help")); return false }
            for (i in 1 until args.size) { chatOptions.sendMessage(config.getString(args[i])) }; return false
        }

        // /attributum slot [o:player]<->[o:slot]
        if (args[0].equals("slot", ignoreCase = true)){
            if (!player.hasPermission("attributum.slot")){ chatOptions.sendMessage(config.getString("permission")); return false }
            if (config.getBoolean("slot-handler")) { chatOptions.sendMessage(config.getString("slot-disabled")); return false }
            if (config.getInt("slots") == 1) { chatOptions.sendMessage(config.getString("slot-singular")); return false }
            if (args.size == 1) { chatOptions.sendMessage(config.getString("slot")); return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("attributum.slot.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 2 && config.isInteger(args[2])) {
                    storage.setActive(args[1], args[2].toInt())
                    chatOptions.sendTargetMessage(args[1], config.getString("switch-other"))
                    chatOptions.attemptMessage(args[1], config.getString("switch"))
                    return true
                } else { chatOptions.sendTargetMessage(args[1], config.getString("slot")); return true }
            }
            if (config.isInteger(args[1])) {
                if (args.size == 2 && storage.checkPlayerExists(args[2])){
                    if (!player.hasPermission("attributum.slot.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                    storage.setActive(args[2], args[1].toInt())
                    chatOptions.sendTargetMessage(args[2], config.getString("switch-other"))
                    chatOptions.attemptMessage(args[2], config.getString("switch"))
                    return true
                }else{ storage.setActive(player.name, args[1].toInt()); chatOptions.sendMessage(config.getString("switch")); return true }
            }
            else { chatOptions.sendMessage(config.getString("help")); return false }
        }

        // /attributum display [o:slot]<->[o:player]
        if (args[0].equals("display", ignoreCase = true)){
            if (!player.hasPermission("attributum.display")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1) { config.getStringList("display").forEach {display-> chatOptions.sendMessage(display)}; return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("attributum.display.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 3 && config.isInteger(args[2])){
                    if (!config.checkSlot(args[2].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                    config.getStringList("display").forEach {display-> chatOptions.sendTargetMessage(args[1], display, args[2].toInt() - 1)}
                    return true
                }
                else { config.getStringList("display").forEach {display-> chatOptions.sendTargetMessage(args[1], display)}; return true }
            }
            if (config.isInteger(args[1])){
                if (!config.checkSlot(args[1].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                if (args.size == 3 && storage.checkPlayerExists(args[2])){
                    if (!player.hasPermission("attributum.display.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                    config.getStringList("display").forEach {display-> chatOptions.sendTargetMessage(args[2], display, args[1].toInt() - 1)}
                    return true
                }
                else { config.getStringList("display").forEach {display-> chatOptions.sendMessage(display, args[1].toInt() - 1)} }
            }
            else { chatOptions.sendMessage(config.getString("help")); return false }
        }

        // /attributum reset [o:slot]<->[o:player]
        if (args[0].equals("reset", ignoreCase = true)){
            if (!player.hasPermission("attributum.reset")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1){ storage.reset(player.name); chatOptions.sendMessage(config.getString("reset")); return true }
            if (storage.checkPlayerExists(args[1])) {
                if (!player.hasPermission("attributum.reset.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                if (args.size == 3 && config.isInteger(args[2])){
                    if (!config.checkSlot(args[2].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                    storage.reset(args[1], args[2].toInt() - 1)
                    if (args[1] != player.name) {
                        chatOptions.sendTargetMessage(args[1], config.getString("reset-other").replace(":slot:", args[2]))
                        chatOptions.attemptMessage(args[1], config.getString("reset").replace(":slot:", args[2]))
                        return true
                    } else { chatOptions.sendMessage(config.getString("reset").replace(":slot:", args[2])); return true }
                }
                else {
                    storage.reset(args[1])
                    if (args[1] != player.name) {
                        chatOptions.sendTargetMessage(args[1], config.getString("reset-other"))
                        chatOptions.attemptMessage(args[1], config.getString("reset"))
                        return true
                    } else { chatOptions.sendMessage(config.getString("reset")); return true }
                }
            }
            if (config.isInteger(args[1])){
                if (!config.checkSlot(args[1].toInt() - 1)) { chatOptions.sendMessage(config.getString("slot-invalid")); return false }
                if (args.size == 3 && storage.checkPlayerExists(args[2])){
                    if (!player.hasPermission("attributum.reset.other")) { chatOptions.sendMessage(config.getString("permission")); return false }
                    storage.reset(args[2], args[1].toInt() - 1)
                    if (args[1] != player.name) {
                        chatOptions.sendTargetMessage(args[2], config.getString("reset-other").replace(":slot:", args[1]))
                        chatOptions.attemptMessage(args[2], config.getString("reset").replace(":slot:", args[1]))
                        return true
                    } else { chatOptions.sendMessage(config.getString("reset").replace(":slot:", args[1])); return true }
                }
                else {
                    storage.reset(player.name, args[1].toInt() - 1)
                    chatOptions.sendMessage(config.getString("reset").replace(":slot:", args[1]), args[1].toInt() - 1)
                    return true
                }
            }
        }

        // /attributum delete - attributum.delete
        if (args[0].equals("delete", ignoreCase = true)){
            if (!player.hasPermission("attributum.delete")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (config.getDouble("inactive-days") <= 0.00) { chatOptions.sendMessage(config.getString("delete-0")); return false }
            storage.deleteInactivePlayers(); chatOptions.sendMessage(config.getString("delete")); return true
        }

        // /attributum alter stat player number
        if (args[0].equals("alter")){
            if (!player.hasPermission("attributum.alter")) { chatOptions.sendMessage(config.getString("permission")); return false }
            if (args.size == 1) { chatOptions.sendMessage(config.getString("help")); return true }
            var attribute = "@~Attribute~@"; var target = player.name; var number = 1
            if (args.size == 2 && storage.checkPlayerExists(args[1])){ target = args[1] }
            if (args.size == 3 && storage.checkPlayerExists(args[2])){ target = args[2] }
            if (args.size == 4 && storage.checkPlayerExists(args[3])){ target = args[3] }
            if (args.size == 2 && config.isInteger(args[1])){ number = args[1].toInt() }
            if (args.size == 3 && config.isInteger(args[2])){ number = args[2].toInt() }
            if (args.size == 4 && config.isInteger(args[3])){ number = args[3].toInt() }
            config.getStringList("attributes").forEach {attributeC-> if (args.contains(attributeC)){ attribute = attributeC } }

            if (attribute == "@~Attribute~@") { chatOptions.sendMessage(config.getString("attribute-invalid")); return false }

            storage.setAttribute(target, attribute, storage.getAttribute(target, attribute) + number)
            if (target != player.name) {
                chatOptions.sendTargetMessage(target, config.getString("attributum-alter-other"))
                chatOptions.attemptMessage(target, config.getString("attributum-alter"))
                return true
            } else { chatOptions.sendMessage(config.getString("attributum-alter")); return true }
        }

        chatOptions.sendMessage(config.getString("help"))
        return true
    }
}