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

package net.risenphoenix.commons.localization;

import net.risenphoenix.commons.Plugin;
import net.risenphoenix.commons.stores.LocalizationStore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LocalizationManager {

    private Map<String, String> translation;
    private Map<String, String> defaultTranslation;
    private String selectedLanguage;
    private FileConfiguration loadedLanguage;

    public LocalizationManager(final Plugin plugin, String langID) {
        File f = new File(plugin.getDataFolder() + File.separator + langID +
                ".yml");

        if (f.exists()) {
            this.selectedLanguage = langID;
        } else {
            this.selectedLanguage = "en";
            if (!langID.equalsIgnoreCase("en")) plugin.sendConsoleMessage(
                    Level.WARNING, "Translation Index " + langID + ".yml " +
                    "could not be found. Falling back to Default Translation " +
                    "(English).");
        }

        loadTranslation(f);
    }

    private final void loadTranslation(File path) {
        if (!this.selectedLanguage.equals("en") || !selectedLanguage.isEmpty()) {
            loadedLanguage = YamlConfiguration.loadConfiguration(path);
            initializeTranslationIndex();
        }

        // Initialize Default Translation both as primary and as fallback.
        loadDefaultTranslation();
    }

    private final void initializeTranslationIndex() {
        this.translation = new HashMap<String, String>();

        for (Map.Entry<String, Object> entry : loadedLanguage.getValues(true).
                entrySet()) {
            this.translation.put(entry.getKey(), entry.getValue().toString());
        }
    }

    private final void loadDefaultTranslation() {
        this.defaultTranslation = new HashMap<String, String>();

		/* Default Translations Required for Framework to Operate Correctly */

        // Command Instance/Manager Messages
        this.defaultTranslation.put("NO_IMPLEMENT", "Base class does not " +
                "implement.");
        this.defaultTranslation.put("CMD_REG_ERR", "Failed to register " +
                "command. Perhaps it is already registered? Command-ID: ");
        this.defaultTranslation.put("NUM_ARGS_ERR", "An incorrect number of " +
                "arguments was specified.");
        this.defaultTranslation.put("ILL_ARGS_ERR", "An illegal argument was " +
                "passed into the command.");
        this.defaultTranslation.put("PERMS_ERR", "You do not have permission " +
                "to execute this command.");
        this.defaultTranslation.put("NO_CONSOLE", "This command cannot be " +
                "executed from Console.");
        this.defaultTranslation.put("NO_CMD", "An invalid command was " +
                "specified.");
        this.defaultTranslation.put("CMD_NULL_ERR", "An error occurred while " +
                "generating a Command Instance. The command has been aborted.");
        this.defaultTranslation.put("BAD_PARSE_SET", "The parse instructions " +
                "for this Parser have not been determined. Please override " +
                "method Parser.parseCommand() in your parsing class.");

        // configuration Manager Messages
        this.defaultTranslation.put("BAD_CFG_SET", "Failed to register " +
                "Configuration Option. Perhaps it is already specified? " +
                "Cfg-ID: ");
        this.defaultTranslation.put("CFG_INIT_ERR", "The Configuration could " +
                "not be refreshed because it has not yet been initialized.");
        this.defaultTranslation.put("FILE_CFG_NULL", "Failed to save " +
                "configuration; FileConfiguration was null.");
        this.defaultTranslation.put("CFG_SRC_NULL", "Failed to save or reload" +
                "configuration; Configuration Source File was null.");
        this.defaultTranslation.put("CFG_SAVE_ERR", "An IOException " +
                "occurred while attempting to save the Configuration " +
                "(check your input values).");
        this.defaultTranslation.put("CFG_STREAM_NULL", "Failed to reload " +
                "configuration; InputStream was null (is your Source " +
                "File null?).");

        // Database Messages
        this.defaultTranslation.put("DB_CNCT_ERR", "An error occurred while " +
                "attempting to connect to the database.");
        this.defaultTranslation.put("BAD_DB_DRVR", "The database driver " +
                "requested could not be found. Requested driver: ");
        this.defaultTranslation.put("DB_OPEN_SUC", "Established connection " +
                "to database.");
        this.defaultTranslation.put("DB_CLOSE_SUC", "The connection to the " +
                "database was closed successfully.");
        this.defaultTranslation.put("DB_CLOSE_ERR", "An error occurred while " +
                "attempting to close the connection to the database. Error: ");
        this.defaultTranslation.put("DB_QUERY_ERR", "An error occurred while " +
                "attempting to query the database. Error: ");
        this.defaultTranslation.put("DB_QUERY_RETRY", "Retrying Query...");
        this.defaultTranslation.put("DB_PREP_STMT_ERR", "An error occurred " +
                "while attempting to generate a prepared statement for the " +
                "database.");
        this.defaultTranslation.put("DB_DEBUG_ACTIVE", "Database Debugging " +
                "is active. All SQL queries will be logged as they are " +
                "received.");
        this.defaultTranslation.put("BAD_SQL_INPUT", "A parameter passed to " +
                "the StatementObject is invalid! Valid parameters are those " +
                "of type String and type Integer.");
    }

    public final String getLocalString(String key) {
        String value;

        if (translation != null) {
            value = translation.get(key);

            // Attempt to fall back to library/plugin specified default store
            if (value == null || value.equals("null")) {
                value = defaultTranslation.get(key);
            }
        } else {
            value = defaultTranslation.get(key);
        }

        if (value == null || value.equals("null") || value.isEmpty()) {
            return "Invalid Translation-Key: " + key;
        } else {
            return value;
        }
    }

    public final void addDefaultValue(String key, String value) {
        this.defaultTranslation.put(key, value);
    }

    public final void appendLocalizationStore(LocalizationStore values) {
        Map<String,String> finalMap = new HashMap<String, String>();
        finalMap.putAll(defaultTranslation);
        finalMap.putAll(values.getValues());

        this.defaultTranslation = finalMap;
    }

}
