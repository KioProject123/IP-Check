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

package net.risenphoenix.ipcheck.database;

import net.risenphoenix.commons.Plugin;
import net.risenphoenix.commons.database.DatabaseManager;
import net.risenphoenix.commons.database.QueryFilter;
import net.risenphoenix.commons.database.StatementObject;
import net.risenphoenix.ipcheck.objects.IPObject;
import net.risenphoenix.ipcheck.objects.UserObject;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class DatabaseController extends DatabaseManager {

    // SQ-Lite Initializer
    public DatabaseController(final Plugin plugin) {
        super(plugin, "ip-check");

        // Enable Debugging to allow us to view the dynamic SQL queries
        //this.enableDebug(true);
        this.dropTables(); // Attempt Table Drop
        this.initializeSQLiteTables();
    }

    // MySQL Initializer
    public DatabaseController(final Plugin plugin, String hostname, int port,
                              String database, String username, String pwd) {
        super(plugin, hostname, port, database, username, pwd, 1);


        // Enable Debugging to allow us to view the dynamic SQL queries
        //this.enableDebug(true);
        this.dropTables(); // Attempt Table Drop
        this.initializeMySQLTables(); // Initialize Tables
    }

    private void dropTables() {
        if (!getPlugin().getConfigurationManager().getBoolean("dbGenerated")) {
            // SQL Strings
            String SQL_0 = "DROP TABLE IF EXISTS ipcheck_log;";
            String SQL_1 = "DROP TABLE IF EXISTS ipcheck_user;";
            String SQL_2 = "DROP TABLE IF EXISTS ipcheck_ip;";

            // Execute SQL
            executeStatement(new StatementObject(getPlugin(), SQL_0));
            executeStatement(new StatementObject(getPlugin(), SQL_1));
            executeStatement(new StatementObject(getPlugin(), SQL_2));

            // Save Configuration Option
            getPlugin().getConfigurationManager()
                    .setConfigurationOption("dbGenerated", true);
        }
    }

    // Initialize Tables for SQ-Lite
    public void initializeSQLiteTables() {
        String TABLE_IPC_LOG = "CREATE TABLE IF NOT EXISTS ipcheck_log ( " +
                "ip TEXT," +
                "username TEXT," +
                "timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY(username,ip));";

        String TABLE_IPC_USER = "CREATE TABLE IF NOT EXISTS ipcheck_user ( " +
                "username TEXT," +
                "uuid TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY(username));";

        String TABLE_IPC_IP = "CREATE TABLE IF NOT EXISTS ipcheck_ip ( " +
                "ip TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY(ip));";

        this.executeStatement(new StatementObject(this.getPlugin(),
                TABLE_IPC_IP));
        this.executeStatement(new StatementObject(this.getPlugin(),
                TABLE_IPC_LOG));
        this.executeStatement(new StatementObject(this.getPlugin(),
                TABLE_IPC_USER));

        executeColumnUpdate();
    }

    // Initialize Tables for MySQL
    public void initializeMySQLTables() {
        String TABLE_IPC_LOG = "CREATE TABLE IF NOT EXISTS ipcheck_log ( " +
                "ip varchar(15) NOT NULL," +
                "username varchar(255) NOT NULL," +
                "timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (ip,username)" +
                ");";

        String TABLE_IPC_USER = "CREATE TABLE IF NOT EXISTS ipcheck_user ( " +
                "username varchar(255) NOT NULL," +
                "uuid varchar(255)," +
                "timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (username)" +
                ");";

        String TABLE_IPC_IP = "CREATE TABLE IF NOT EXISTS ipcheck_ip ( " +
                "ip varchar(15) NOT NULL," +
                "timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (ip)" +
                ");";

        this.executeStatement(new StatementObject(this.getPlugin(),
                TABLE_IPC_IP));
        this.executeStatement(new StatementObject(this.getPlugin(),
                TABLE_IPC_LOG));
        this.executeStatement(new StatementObject(this.getPlugin(),
                TABLE_IPC_USER));

        executeColumnUpdate();
    }

    public final void log(String player, String ip) {
        this.addIP(ip);
        this.addPlayer(player);

        String SQL = "replace into ipcheck_log (ip,username) VALUES (?, ?)";
        this.executeStatement(new StatementObject(this.getPlugin(),
                SQL, new Object[]{ip, player.toLowerCase()}));
    }

    public final void log(UUID uuid, String player, String ip) {
        log(player, ip);

        this.addUUID(uuid);
    }

    public final void addIP(String ip) {
        String SQL = "insert " + ((this.getPlugin().getConfigurationManager()
                .getBoolean("use-mysql")) ? "" : "or ") + "ignore into " +
                "ipcheck_ip (ip) values (?)";

        this.executeStatement(new StatementObject(this.getPlugin(),
                SQL, new Object[]{ip}));
    }

    public final void addPlayer(String player) {
        String SQL = "insert " + ((this.getPlugin().getConfigurationManager()
                .getBoolean("use-mysql")) ? "" : "or ") + "ignore into " +
                "ipcheck_user (username) values (?)";

        this.executeStatement(new StatementObject(this.getPlugin(),
                SQL, new Object[]{player.toLowerCase()}));
    }

    public final void addUUID(UUID uuid) {
        String player = Bukkit.getOfflinePlayer(uuid).getName();

        String SQL = "update ipcheck_user set uuid=? " +
                "where lower(username) = ?";

        this.executeStatement(new StatementObject(this.getPlugin(),
                SQL, new Object[]{uuid.toString(), player.toLowerCase()}));
    }

    public final UUID getUUID(String player) {
        String SQL = "select uuid from ipcheck_user where " +
                "lower(username) = ?";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                UUID uuid = null;

                try {
                    while (res.next()) {
                        if (res.getString("uuid") != null &&
                                res.getString("uuid").length() > 0) {
                            uuid = UUID.fromString(res.getString("uuid"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return uuid;
            }
        };

        return (UUID) this.executeQuery(new StatementObject(this.getPlugin(),
                SQL, new Object[]{player.toLowerCase()}), filter);
    }

    /* Player Methods */

    public final void purgePlayer(String player) {
        String STMT_1 = "delete from ipcheck_user where lower(username) = ?";
        String STMT_2 = "delete from ipcheck_log where lower(username) = ?";

        this.executeStatement(new StatementObject(this.getPlugin(),
                STMT_1, new Object[]{player.toLowerCase()}));

        this.executeStatement(new StatementObject(this.getPlugin(),
                STMT_2, new Object[]{player.toLowerCase()}));
    }

    public final boolean isValidPlayer(String player) {
        String SQL = "select username from ipcheck_user where " +
                "lower(username) = ?";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                try {
                    return (res.next());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return false;
            }
        };

        return (Boolean) this.executeQuery(new StatementObject(this.getPlugin(),
                SQL, new Object[]{player.toLowerCase()}), filter);
    }

    /* IP Methods */

    public final void purgeIP(String ip) {
        String STMT_1 = "delete from ipcheck_ip where ip = ?";
        String STMT_2 = "delete from ipcheck_log where ip = ?";

        this.executeStatement(new StatementObject(this.getPlugin(),
                STMT_1, new Object[]{ip}));

        this.executeStatement(new StatementObject(this.getPlugin(),
                STMT_2, new Object[]{ip}));
    }

    /* Other Methods */

    public final IPObject getIPObject(String ip) {
        String SQL = "select username from ipcheck_log where ip = ?";

        QueryFilter filter = new QueryFilter(new Object[]{ip, this}) {
            @Override
            public Object onExecute(ResultSet res) {
                DatabaseController dbC = (DatabaseController) this.getData()[1];
                ArrayList<String> users = new ArrayList<String>();
                String ip = (String) this.getData()[0];

                try {
                    while (res.next()) {
                        users.add(res.getString("username"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return new IPObject(ip, users);
            }
        };

        return (IPObject) this.executeQuery(new StatementObject(
                this.getPlugin(), SQL, new Object[]{ip}), filter);
    }

    public final UserObject getUserObject(String player) {
        String SQL = "select ip from ipcheck_log where lower(username) = ?";
        UUID uuid = this.getUUID(player);

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                ArrayList<String> ips = new ArrayList<String>();

                try {
                    while (res.next()) {
                        if (!ips.contains(res.getString("ip"))) {
                            ips.add(res.getString("ip"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return ips;
            }
        };

        ArrayList<String> ips = (ArrayList<String>) this.executeQuery(
                new StatementObject(this.getPlugin(), SQL, new Object[]{
                        player.toLowerCase()}), filter);

        return new UserObject(player.toLowerCase(), uuid, ips);
    }

    public final ArrayList<String> getPlayersByUUID(UUID uuid) {
        String SQL = "select username from ipcheck_user where uuid = ?";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                ArrayList<String> users = new ArrayList<String>();

                try {
                    while (res.next()) {
                        if (!users.contains(res.getString("username"))) {
                            users.add(res.getString("username"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return users;
            }
        };

        return (ArrayList<String>) this.executeQuery(new StatementObject(
                this.getPlugin(), SQL, new Object[]{uuid.toString()}), filter);
    }

    public final String getLastKnownIP(String player) {
        String SQL = "select ip from ipcheck_log where lower(username) = ? " +
                "order by timestamp desc limit 1;";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                String returning = "NO_FIND";

                try {
                    if (res.next()) returning = res.getString("ip");
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return returning;
            }
        };

        return (String) this.executeQuery(new StatementObject(this.getPlugin(),
                SQL, new Object[]{player.toLowerCase()}), filter);
    }

    public final boolean isValidIP(String ip) {
        String SQL = "select ip from ipcheck_ip where ip = ?";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                try {
                    return (res.next());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return false;
            }
        };

        if (ip.equals("NO_FIND")) return false;

        return (Boolean) this.executeQuery(new StatementObject(this.getPlugin(),
                SQL, new Object[]{ip}), filter);
    }

    public final String getLogTime(String player) {
        String SQL = "select timestamp from ipcheck_user where " +
                "lower(username) = ?";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                String returning = null;

                try {
                    if (res.next()) returning = res.getString("timestamp");
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return returning;
            }
        };

        return (String) this.executeQuery(new StatementObject(this.getPlugin(),
                SQL, new Object[]{player.toLowerCase()}), filter);
    }

    public final String getLastTime(String player) {
        String SQL = "select timestamp from ipcheck_log where " +
                "lower(username) = ? order by timestamp desc limit 1;";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                String returning = null;

                try {
                    if (res.next()) returning = res.getString("timestamp");
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return returning;
            }
        };

        return (String) this.executeQuery(new StatementObject(this.getPlugin(),
                SQL, new Object[]{player.toLowerCase()}), filter);
    }

    public final String getCurrentTimeStamp() {
        String SQL = "select CURRENT_TIMESTAMP";

        QueryFilter filter = new QueryFilter() {
            @Override
            public Object onExecute(ResultSet res) {
                String date = null;

                try {
                    res.next();
                    date = res.getString("CURRENT_TIMESTAMP");
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return date;
            }
        };

        return (String) this.executeQuery(new StatementObject(this.getPlugin(),
                SQL), filter);
    }

    private void executeColumnUpdate() {
        // Check for Missing Columns (Required for
        // those upgrading from pre-2.0, 2.0.6, etc)
        boolean hasRejoinPlayer = true;
        boolean hasRejoinIP = true;
        boolean hasProtectedPlayer = true;
        boolean hasUUID = true;

        // SQLite
        if (getDatabaseType() == DatabaseType.SQLITE) {
            String SQL_P = "PRAGMA table_info(ipcheck_user);";

            QueryFilter filter = new QueryFilter() {
                @Override
                public Object onExecute(ResultSet res) {
                    boolean[] flags = new boolean[3];
                    // 0 = Rejoin, 1 = Protection
                    try {
                        while (res.next()) {
                            if (res.getString("name").equals("rejoinexempt"))
                                flags[0] = true;
                            if (res.getString("name").equals("protected"))
                                flags[1] = true;
                            if (res.getString("name").equals("uuid"))
                                flags[2] = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return flags;
                }
            };

            // Fetch Boolean Array from Database
            boolean[] res = (boolean[]) this.executeQuery(
                    new StatementObject(this.getPlugin(), SQL_P), filter);

            hasRejoinPlayer = res[0];
            hasProtectedPlayer = res[1];
            hasUUID = res[2];

            String SQL_I = "PRAGMA table_info(ipcheck_ip);";

            QueryFilter filter_two = new QueryFilter() {
                @Override
                public Object onExecute(ResultSet res) {
                    boolean flag = false;
                    // 0 = Rejoin, 1 = Protection
                    try {
                        while (res.next()) {
                            if (res.getString("name").equals("rejoinexempt"))
                                flag = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return flag;
                }
            };

            // Fetch Boolean from Database
            hasRejoinIP = (boolean) this.executeQuery(
                    new StatementObject(this.getPlugin(), SQL_I), filter_two);

            // Execute Results
            if (!hasRejoinPlayer) {
                String SQL_ZERO = "ALTER TABLE ipcheck_user ADD COLUMN " +
                        "rejoinexempt INTEGER DEFAULT 0";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }

            if (!hasProtectedPlayer) {
                String SQL_ZERO = "ALTER TABLE ipcheck_user ADD COLUMN " +
                        "protected INTEGER DEFAULT 0";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }

            if (!hasUUID) {
                String SQL_ZERO = "ALTER TABLE ipcheck_user ADD COLUMN " +
                        "uuid TEXT";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }

            if (!hasRejoinIP) {
                String SQL_ZERO = "ALTER TABLE ipcheck_ip ADD COLUMN " +
                        "rejoinexempt INTEGER DEFAULT 0";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }


        // MySQL
        } else {
            String SQL_P = "SHOW COLUMNS FROM " +
                    getPlugin().getConfigurationManager().getString("dbName") +
                    ".ipcheck_user";

            QueryFilter filter = new QueryFilter() {
                @Override
                public Object onExecute(ResultSet res) {
                    boolean[] flags = new boolean[3];
                    // 0 = Rejoin, 1 = Protection
                    try {
                        while (res.next()) {
                            if (res.getString("Field").equals("rejoinexempt"))
                                flags[0] = true;
                            if (res.getString("Field").equals("protected"))
                                flags[1] = true;
                            if (res.getString("Field").equals("uuid"))
                                flags[2] = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return flags;
                }
            };

            // Fetch Boolean Array from Database
            boolean[] res = (boolean[]) this.executeQuery(
                    new StatementObject(this.getPlugin(), SQL_P), filter);

            hasRejoinPlayer = res[0];
            hasProtectedPlayer = res[1];
            hasUUID = res[2];

            String SQL_I = "SHOW COLUMNS FROM " +
                    getPlugin().getConfigurationManager().getString("dbName") +
                    ".ipcheck_ip";

            QueryFilter filter_two = new QueryFilter() {
                @Override
                public Object onExecute(ResultSet res) {
                    boolean flag = false;
                    // 0 = Rejoin, 1 = Protection
                    try {
                        while (res.next()) {
                            if (res.getString("Field").equals("rejoinexempt"))
                                flag = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return flag;
                }
            };

            // Fetch Boolean from Database
            hasRejoinIP = (boolean) this.executeQuery(
                    new StatementObject(this.getPlugin(), SQL_I), filter_two);

            // Execute Results
            if (!hasRejoinPlayer) {
                String SQL_ZERO = "ALTER TABLE ipcheck_user ADD COLUMN " +
                        "rejoinexempt bit(1) NOT NULL DEFAULT b'0'";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }

            if (!hasProtectedPlayer) {
                String SQL_ZERO = "ALTER TABLE ipcheck_user ADD COLUMN " +
                        "protected bit(1) NOT NULL DEFAULT b'0'";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }

            if (!hasUUID) {
                String SQL_ZERO = "ALTER TABLE ipcheck_user ADD COLUMN " +
                        "uuid varchar(255)";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }

            if (!hasRejoinIP) {
                String SQL_ZERO = "ALTER TABLE ipcheck_ip ADD COLUMN " +
                        "rejoinexempt bit(1) NOT NULL DEFAULT b'0'";

                this.executeStatement(new StatementObject(this.getPlugin(),
                        SQL_ZERO));
            }
        }
    }

}
