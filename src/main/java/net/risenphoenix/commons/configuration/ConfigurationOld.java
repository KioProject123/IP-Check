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

package net.risenphoenix.commons.configuration;

import net.risenphoenix.commons.Plugin;
import net.risenphoenix.commons.localization.LocalizationManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class ConfigurationOld {

    private final Plugin plugin;
    private LocalizationManager lm;

    private FileConfiguration config;
    private File file;

    public ConfigurationOld(final Plugin plugin, String configName) {
        this.plugin = plugin;
        this.lm = plugin.getLocalizationManager();
        this.file = new File(getPlugin().getDataFolder(), configName + ".yml");
    }

    public final FileConfiguration getConfig() {
        if (this.config == null) reloadConfig();
        return this.config;
    }

    public final void saveConfig() {
        try {
            if (config == null) {
                getPlugin().sendConsoleMessage(Level.SEVERE, lm
                        .getLocalString("FILE_CFG_NULL"));
                return;
            } else if (file == null) {
                getPlugin().sendConsoleMessage(Level.SEVERE, lm
                        .getLocalString("CFG_SRC_NULL"));
                return;
            }

            try {
                getConfig().save(this.file);
            } catch (IOException e) {
                getPlugin().sendConsoleMessage(Level.SEVERE, lm
                        .getLocalString("CFG_SAVE_NULL"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void reloadConfig() {
        try {
            if (file == null) {
                getPlugin().sendConsoleMessage(Level.SEVERE, lm
                        .getLocalString("CFG_SRC_NULL"));
                return;
            }

            this.config = YamlConfiguration.loadConfiguration(this.file);

            // Allows accessing of YML files that are stored within folders
            String resourcePath = file.getPath().replace(getPlugin()
                    .getDataFolder().getPath() + "/", "");

            InputStream fileStream = getPlugin().getResource(resourcePath);

            if (fileStream != null) {
                YamlConfiguration configStream = YamlConfiguration
                        .loadConfiguration(file);
                config.setDefaults(configStream);
            } else {
                getPlugin().sendConsoleMessage(Level.SEVERE, lm
                        .getLocalString("CFG_STREAM_NULL"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void saveDefaultConfiguration() {
        if (file == null) {
            getPlugin().sendConsoleMessage(Level.SEVERE, lm
                    .getLocalString("CFG_SRC_NULL"));
        }

        if (!file.exists()) getPlugin().saveResource(
                this.file.getName(), false);

    }

    public final Plugin getPlugin() {
        return this.plugin;
    }
}
