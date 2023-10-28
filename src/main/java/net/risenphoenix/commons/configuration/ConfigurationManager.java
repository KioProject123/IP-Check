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
import net.risenphoenix.commons.configuration.ConfigurationOption.ConfigOptionType;
import net.risenphoenix.commons.stores.ConfigurationStore;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings("unchecked")
public class ConfigurationManager {

    private final Plugin plugin;
    private Configuration config;
    public File configFile;

    private String configName = "config";
    private Map<String, Object> configSettings;
    private ArrayList<ConfigurationOption> options = null;

    private String[] configHeader = null;

    /*
    * Manage custom configurations and files
    */
    public ConfigurationManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public ConfigurationManager(Plugin plugin, String configName) {
        this.plugin = plugin;
        this.configName = configName;
    }

    public final void setConfigHeader(String[] header) {
        this.configHeader = header;
    }

    public final void initializeConfiguration(ConfigurationStore store) {
        this.config = getNewConfig(configName + ".yml");
        initializeConfigurationStore(store.getOptions());
    }

    private void initializeConfigurationStore(
            ArrayList<ConfigurationOption> options) {
        this.options = options;
        this.configSettings = new HashMap<String, Object>();

        for (ConfigurationOption configOpt : options) {
            try {
                if (configOpt.getType() == ConfigOptionType.Boolean) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getBoolean(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.BooleanList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getBooleanList(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.ByteList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getByteList(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.CharList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getCharacterList(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.FloatList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getFloatList(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.Integer) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getInt(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.IntegerList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getIntegerList(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.Long) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getLong(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.LongList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getLongList(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.String) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getString(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.StringList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getStringList(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.Double) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getDouble(configOpt.getIdentifier()));

                } else if (configOpt.getType() == ConfigOptionType.DoubleList) {
                    configSettings.put(configOpt.getIdentifier(),
                            this.config.getConfig().
                                    getDoubleList(configOpt.getIdentifier()));
                }
            } catch (Exception e) {
                this.plugin.sendConsoleMessage(Level.WARNING,
                        this.plugin.getLocalizationManager().
                                getLocalString("BAD_CFG_SET")
                                + configOpt.getIdentifier());
            }
        }
    }

    /* DATA ACCESSOR METHODS */
    private Object getConfigurationOption(String identifier) {
        return this.configSettings.get(identifier);
    }

    public final boolean getBoolean(String identifier) {
        return Boolean.parseBoolean(
                getConfigurationOption(identifier).toString());
    }

    public final List<Boolean> getBooleanList(String identifier) {
        return (List<Boolean>) getConfigurationOption(identifier);
    }

    public final List<Byte> getByteList(String identifier) {
        return (List<Byte>) getConfigurationOption(identifier);
    }

    public final List<Character> getCharacterList(String identifier) {
        return (List<Character>) getConfigurationOption(identifier);
    }

    public final List<Float> getFloatList(String identifier) {
        return (List<Float>) getConfigurationOption(identifier);
    }

    public final int getInteger(String identifier) {
        return Integer.parseInt(getConfigurationOption(identifier).toString());
    }

    public final List<Integer> getIntegerList(String identifier) {
        return (List<Integer>) getConfigurationOption(identifier);
    }

    public final long getLong(String identifier) {
        return Long.parseLong(getConfigurationOption(identifier).toString());
    }

    public final List<Long> getLongList(String identifier) {
        return (List<Long>) getConfigurationOption(identifier);
    }

    public final String getString(String identifier) {
        return (String) getConfigurationOption(identifier);
    }

    public final List<String> getStringList(String identifier) {
        return (List<String>) getConfigurationOption(identifier);
    }

    public void setConfigurationOption(String identifier, Object arg0) {
        this.config.getConfig().set(identifier, arg0);
        this.saveConfig();

        if (this.options != null) {
            this.initializeConfigurationStore(this.options);
        } else {
            this.plugin.sendConsoleMessage(Level.SEVERE,
                    this.plugin.getLocalizationManager().
                            getLocalString("CFG_INIT_ERR"));
        }
    }

    /* CONFIGURATION I/O METHODS */

    /*
    * Get new configuration with header
    * @param filePath - Path to file
    * @return - New SimpleConfig
    */
    private Configuration getNewConfig(String filePath) {
        configFile = this.getConfigFile(filePath);

        if(!configFile.exists()) {
            this.prepareFile();

            if(this.configHeader != null && this.configHeader.length != 0) {
                this.setHeader(this.configHeader);
            }
        }

        Configuration config = new Configuration(this, this.configFile,
                this.getCommentsNum());
        return config;
    }

    /*
    * Get configuration file from string
    * @param file - File path
    * @return - New file object
    */
    private File getConfigFile(String file) {
        if(file.isEmpty() || file == null) {
            System.out.println("Line 260 getConfigFile(String file) returned null!");
            return null;
        }

        File configFile;

        if(file.contains("/")) {
            if(file.startsWith("/")) {
                configFile = new File(plugin.getDataFolder() + file.replace("/",
                        File.separator));
            } else {
                configFile = new File(plugin.getDataFolder() + File.separator +
                        file.replace("/", File.separator));
            }

        } else {
            configFile = new File(plugin.getDataFolder(), file);
            System.out.println("Got file: " + configFile.getPath());
        }

        return configFile;

    }

    /*
    * Create new file for config and copy resource into it
    * @param file - Path to file
    * @param resource - Resource to copy
    */
    private void prepareFile() {
        if(configFile.exists()) {
            return;
        }

        configFile.getParentFile().mkdirs();
        plugin.saveResource(this.configFile.getName(), false);
    }

    /*
    * Adds header block to config
    * @param file - Config file
    * @param header - Header lines
    */
    private void setHeader(String[] header) {
        if(!configFile.exists()) {
            return;
        }

        try {
            String currentLine;
            StringBuilder config = new StringBuilder("");
            BufferedReader reader = new BufferedReader(new
                    FileReader(configFile));

            while((currentLine = reader.readLine()) != null) {
                config.append(currentLine + "\n");
            }

            reader.close();
            config.append("# ===============================================" +
                    "============================= #\n");

            for(String line : header) {
                config.append("# " + line + "\n");
            }

            config.append("# ===============================================" +
                    "============================= #\n");

            BufferedWriter writer = new BufferedWriter(new
                    FileWriter(configFile));
            writer.write(this.prepareConfigString(config.toString()));
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    * Read file and make comments SnakeYAML friendly
    * @param filePath - Path to file
    * @return - File as Input Stream
    */
    public InputStream getConfigContent() {
        if(!configFile.exists()) {
            System.out.println("Line 351 getConfigContent() returned null!");
            return null;
        }

        try {
            int commentNum = 0;

            String addLine;
            String currentLine;
            String pluginName = this.getPluginName();

            StringBuilder whole = new StringBuilder("");
            BufferedReader reader = new BufferedReader(new
                    FileReader(configFile));

            while((currentLine = reader.readLine()) != null) {
                if(currentLine.startsWith("#")) {
                    addLine = currentLine.replaceFirst("#", pluginName +
                            "_COMMENT_" + commentNum + ":");
                    whole.append(addLine + "\n");
                    commentNum++;
                } else {
                    whole.append(currentLine + "\n");
                }
            }

            String config = whole.toString();
            InputStream configStream = new ByteArrayInputStream(config
                    .getBytes(Charset.forName("UTF-8")));

            reader.close();
            return configStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    * Get comments from file
    * @param file - File
    * @return - Comments number
    */
    private int getCommentsNum() {
        if(!configFile.exists()) {
            return 0;
        }

        try {
            int comments = 0;
            String currentLine;
            BufferedReader reader = new BufferedReader(new
                    FileReader(configFile));

            while((currentLine = reader.readLine()) != null) {
                if(currentLine.startsWith("#")) {
                    comments++;
                }
            }

            reader.close();
            return comments;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String prepareConfigString(String configString) {
        int lastLine = 0;
        int headerLine = 0;

        String[] lines = configString.split("\n");
        StringBuilder config = new StringBuilder("");

        for(String line : lines) {
            if(line.startsWith(this.getPluginName() + "_COMMENT")) {
                String comment = "#" + line.trim().substring(
                        line.indexOf(":") + 1);

                if(comment.startsWith("# +-")) {

                    /*
                    * If header line = 0 then it is
                    * header start, if it's equal
                    * to 1 it's the end of header
                    */

                    if(headerLine == 0) {
                        config.append(comment + "\n");

                        lastLine = 0;
                        headerLine = 1;

                    } else if(headerLine == 1) {
                        config.append(comment + "\n\n");

                        lastLine = 0;
                        headerLine = 0;
                    }
                } else {
                    /*
                    * Last line = 0 - Comment
                    * Last line = 1 - Normal path
                    */
                    String normalComment;

                    if(comment.startsWith("# ' ")) {
                        normalComment = comment.substring(0, comment.length() -
                                1).replaceFirst("# ' ", "# ");
                    } else {
                        normalComment = comment;
                    }

                    if(lastLine == 0) {
                        config.append(normalComment + "\n");
                    } else if(lastLine == 1) {
                        config.append("\n" + normalComment + "\n");
                    }

                    lastLine = 0;
                }
            } else {
                config.append(line + "\n");
                lastLine = 1;
            }
        }

        return config.toString();
    }

    public void saveConfig() {
        String configuration = this.prepareConfigString(prepareConfigString(
                config.toString()));

        try {
            BufferedWriter writer = new BufferedWriter(new
                    FileWriter(configFile));
            writer.write(configuration);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getPluginName() {
        return plugin.getDescription().getName();
    }
}
