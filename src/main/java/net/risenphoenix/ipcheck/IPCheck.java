/*
 * Copyright © 2017 Jacob Keep (Jnk1296). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *
 *  * Neither the name of JuNK Software nor the names of its contributors may 
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.risenphoenix.ipcheck;

import net.risenphoenix.commons.Plugin;
import net.risenphoenix.ipcheck.database.DatabaseController;
import net.risenphoenix.ipcheck.stores.CmdStore;
import net.risenphoenix.ipcheck.stores.ConfigStore;
import net.risenphoenix.ipcheck.stores.LocaleStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Collection;
import java.util.logging.Level;

public class IPCheck extends Plugin implements Listener {

    // Instance and Main System Objects
    private static IPCheck instance;
    private DatabaseController dbController;
    private ConfigStore config;

    // Control used mainly in the event of an in-plugin Reload.
    private boolean hasRegistered = false;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        // Fetch IP Address and Player
        Player player = e.getPlayer();
        String address = e.getAddress().getHostAddress();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> dbController.log(player.getUniqueId(), player.getName(), address));
    }

    @Override
    public void onStartup() {
        instance = this;

        // Set Basic Plugin Variables
        this.setPluginName(ChatColor.GOLD, "IP-Check");
        this.setMessageColor(ChatColor.YELLOW);

        // Register the Player Login Listener
        if (!hasRegistered) {
            getServer().getPluginManager().registerEvents(this, this);
            this.hasRegistered = true; // Prevents Re-registration with Bukkit
        }

        // Initialize Configuration
        this.config = new ConfigStore(this);
        this.getConfigurationManager().initializeConfigurationStore(config);

        // Initialize Default Localization
        LocaleStore locStore = new LocaleStore(this);
        this.getLocalizationManager().appendLocalizationStore(locStore);

        // Initialize Database Controller
        if (this.getConfigurationManager().getBoolean("use-mysql")) {
            // MySQL Database Initialization
            dbController = new DatabaseController(this,
                    this.getConfigurationManager().getString("dbHostname"),
                    this.getConfigurationManager().getInteger("dbPort"),
                    this.getConfigurationManager().getString("dbName"),
                    this.getConfigurationManager().getString("dbUsername"),
                    this.getConfigurationManager().getString("dbPassword")
            );
        } else {
            // SQLite Database Initialization
            dbController = new DatabaseController(this);
        }

        // Initialize Commands
        CmdStore cmdStore = new CmdStore(this);
        this.getCommandManager().registerStore(cmdStore);
    }

    @Override
    public void onShutdown() {
        dbController.getDatabaseConnection().closeConnection();
    }

    public static IPCheck getInstance() {
        return instance;
    }

    public DatabaseController getDatabaseController() {
        return this.dbController;
    }

    public String getVersion() {
        return "2.0.7";
    }

    public int getBuildNumber() {
        return 2084;
    }

    public final Player[] getOnlinePlayers() {
        try {
            Collection<? extends Player> result = Bukkit.getOnlinePlayers();
            return result.toArray(new Player[result.size()]);
        } catch (NoSuchMethodError err) {
            sendConsoleMessage(Level.INFO, getLocalizationManager()
                    .getLocalString("VER_COMP_ERR"));
        }

        return new Player[0];
    }
}
