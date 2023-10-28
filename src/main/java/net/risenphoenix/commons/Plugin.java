/*
 * Copyright © 2014 Jacob Keep (Jnk1296). All rights reserved.
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

package net.risenphoenix.commons;

import net.risenphoenix.commons.commands.*;
import net.risenphoenix.commons.configuration.ConfigurationManagerOld;
import net.risenphoenix.commons.localization.LocalizationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;

public class Plugin extends JavaPlugin {

    private LocalizationManager LM;
    private CommandManager CM;
    private ConfigurationManagerOld ConfigM;

    private String pluginName;
    private ChatColor pluginColor = ChatColor.GOLD;
    private ChatColor messageColor = ChatColor.WHITE;

	/* Required Methods for Bukkit JavaPlugin. */

    @Override
    public final void onEnable() {
        this.pluginName = "[" + this.getDescription().getName() + "] ";

        // Must make direct call to Plugin configuration as Config-Manager is
        // not initialized at this point in the execution.
        this.LM = new LocalizationManager(this, this.getConfig()
                .getString("language"));
        this.ConfigM = new ConfigurationManagerOld(this);
        this.CM = new CommandManager(this);
        this.onStartup();
    }

    @Override
    public final void onDisable() {
        this.onShutdown();
    }

    @Override
    public final boolean onCommand(CommandSender sender,
                                   org.bukkit.command.Command root,
                                   String commandLabel, String[] args) {

        /* THIS CODE IS REQUIRED FOR THE COMMAND PARSER TO WORK CORRECTLY */
        // Append Command Root to the list of arguments
        List<String> argsPre = new LinkedList<String>(Arrays.asList(args));
        argsPre.add(0, root.getName());

        // Convert back to Array
        String[] argsFinal = new String[argsPre.size()];
        argsFinal = argsPre.toArray(argsFinal);
        /* THIS CODE IS REQUIRED FOR THE COMMAND PARSER TO WORK CORRECTLY */

        // Parse
        ParseResult pResult = this.CM.parseCommand(argsFinal);

        // If the Parser returned a Command
        if (pResult.getResult() == ResultType.SUCCESS) {
            /*
             * A new command instance must be created when a command is called,
             * so as to prevent static commands from being served to players.
             * This prevents any form of data swappage between two different
             * players executing the same command at the same time.
             */

            // New Command Instance.
            Command cmd = null;

            // The dirty dynamic hack :D
            try {
                // Fetch Class Type
                Class<?> clazz = Class.forName(
                        pResult.getCommand().getClass().getName());

                // Create the Constructor for the Command class. This works
                // only because all commands extend Command, which requires
                // these three arguments to be constructed.
                Constructor<?> ctor = clazz.getConstructor(
                        Plugin.class, String[].class, CommandType.class);

                // Create the specified Command Instance
                cmd = (Command) ctor.newInstance(this,
                        pResult.getCommand().getCallArgs(),
                        pResult.getCommand().getType());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // If Player is calling this command, check Permissions and execute
            if (cmd != null) {
                if (sender instanceof Player) {
                    if (!cmd.onCall(sender, args)) {
                        this.sendPlayerMessage(sender,
                                this.LM.getLocalString("PERM_ERR"));
                    }

                    // If Console is calling Command, check if Command is executable
                } else {
                    if (!cmd.canConsoleExecute()) {
                        this.sendConsoleMessage(Level.INFO,
                                this.LM.getLocalString("NO_CONSOLE"));
                    } else {
                        cmd.onCall(sender, args);
                    }
                }
            } else {
                sendPlayerMessage(sender, getLocalizationManager()
                                .getLocalString("CMD_NULL_ERR"));
            }

            // If the Parser returned a Command, but the Argument count was bad
        } else if (pResult.getResult() == ResultType.BAD_NUM_ARGS) {
            this.sendPlayerMessage(sender,
                    this.LM.getLocalString("NUM_ARGS_ERR"));

            // If the Parser did not return a Command
        } else if (pResult.getResult() == ResultType.FAIL) {
            this.sendPlayerMessage(sender, this.LM.getLocalString("NO_CMD"));
        }

        return true;
    }

	/* Library Specific Methods */

    public void onStartup() {
        throw new UnsupportedOperationException(
                this.LM.getLocalString("NO_IMPLEMENT"));
    }

    public void onShutdown() {
        throw new UnsupportedOperationException(
                this.LM.getLocalString("NO_IMPLEMENT"));
    }

    public final Map<String, String> getVersionInfo() {
        Map<String, String> info = new HashMap<String, String>();

        info.put("NAME", "RP-Commons");
        info.put("VERSION", "v1.05");
        info.put("AUTHOR", "Jacob Keep");
        info.put("BUILD", "93");
        info.put("JVM_COMPAT", "1.7");

        return info;
    }

    public final LocalizationManager getLocalizationManager() {
        return this.LM;
    }

    // Can be over-ridden in order to avoid Bukkit contaminating YML files
    public ConfigurationManagerOld getConfigurationManager() {
        return this.ConfigM;
    }

    public final CommandManager getCommandManager() {
        return this.CM;
    }

    public final void setPluginName(ChatColor color, String name) {
        this.pluginName = "[" + name + "] ";
        this.pluginColor = color;
    }

    public final void setMessageColor(ChatColor color) {
        this.messageColor = color;
    }

    public final void sendPlayerMessage(CommandSender sender, String message) {
        sendPlayerMessage(sender, message, true);
    }

    public final void sendPlayerMessage(CommandSender sender, String message,
                                        boolean useName) {
        sender.sendMessage(((useName) ? this.pluginColor + pluginName : "") +
                this.messageColor + message);
    }

    public final void sendConsoleMessage(Level level, String message) {
        Bukkit.getLogger().log(level, pluginName + message);
    }

    public final String formatPlayerMessage(String msg) {
        return (this.pluginColor + pluginName + this.messageColor + msg);
    }
}
