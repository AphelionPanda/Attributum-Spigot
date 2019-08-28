package online.christopherstocks.attributum.external

import online.christopherstocks.attributum.libs.Config
import org.bukkit.plugin.Plugin
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class Updater {

    fun check(): Boolean {
        val lV = BufferedReader(InputStreamReader(URL("https://api.spigotmc.org/legacy/update.php?resource=45142").openConnection().getInputStream())).readLine().split(".")
        val cV = Config().getInstance().description.version.replace("b", "").split(".")
        if ((cV.contains("b") && lV.contains("b")) || (!cV.contains("b") && !lV.contains("b")) ||
                ((!cV.contains("b") && lV.contains("b")) && Config().getConfig().getBoolean("beta-version")) || (lV.contains("b") && Config().getConfig().getBoolean("beta-version"))){
            if ((lV[0] >= cV[0] || (lV[0] >= cV[0] && lV[1] >= cV[1]) || (lV[0] >= cV[0] && lV[1] >= cV[1] && lV[2] >= cV[2]) || (lV[0] >= cV[0] && lV[1] >= cV[1] && lV[2] >= cV[2] && lV[3] >= cV[3])) && lV != cV){
                return true
            }
        }
        return false
    }

}