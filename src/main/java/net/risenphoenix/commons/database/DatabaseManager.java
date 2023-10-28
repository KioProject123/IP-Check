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

package net.risenphoenix.commons.database;

import net.risenphoenix.commons.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final Plugin plugin;
    private DatabaseConnection connection;
    private DatabaseType type;
    private boolean debug = false;

    /* Connection Properties */

    // MySQL
    private String hostname = null;
    private int port = -1;
    private String database = null;
    private String username = null;
    private String password = null;
    private int poolSize = 1;

    // SQLite
    private String dbName = null;

    // SQLite Constructor
    public DatabaseManager(final Plugin plugin) {
        this.plugin = plugin;
        this.connection = new DatabaseConnection(plugin);
        this.type = DatabaseType.SQLITE;
    }

    // SQLite Constructor w/ DB Name
    public DatabaseManager(final Plugin plugin, final String dbName) {
        this.plugin = plugin;

        // Value Assignment
        this.dbName = dbName;

        // Connection Creation
        this.connection = new DatabaseConnection(plugin, dbName);
        this.type = DatabaseType.SQLITE;
    }

    // MySQL Constructor
    public DatabaseManager(final Plugin plugin, String hostname, int port,
                           String database, String username, String pwd,
                           int poolSize) {
        this.plugin = plugin;

        // Value Assignment
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = pwd;
        this.poolSize = poolSize;

        // Connection Creation (Pooled)
        if (poolSize > 1) {
            this.connection = new DatabaseConnection(plugin, hostname, port,
                    database, username, pwd, poolSize);

        // Connection Creation (Non-pooled)
        } else {
            this.connection = new DatabaseConnection(plugin, hostname, port,
                    database, username, pwd);
        }
        this.type = DatabaseType.MYSQL;
    }

    public void enableDebug(boolean shouldDebug) {
        this.debug = shouldDebug;
        this.plugin.sendConsoleMessage(Level.INFO, this.plugin
                .getLocalizationManager().getLocalString("DB_DEBUG_ACTIVE"));
    }

    // Statement Execute Master Method
    public final boolean executeStatement(StatementObject stmt) {
        try {
            confirmConnection(); // Confirm connection has not timed out.
            PreparedStatement statement =
                    stmt.getStatement(this.connection.getConnection());

            if (this.debug)
            this.plugin.sendConsoleMessage(Level.INFO, statement.toString());

            stmt.getStatement(this.connection.getConnection()).executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Query Execute Master Method
    public final Object executeQuery(StatementObject stmt, QueryFilter filter) {
        ResultSet res = null;
        Object obj = null;

        try {
            confirmConnection(); // Confirm connection has not timed out.
            res = stmt.getStatement(this.connection.getConnection())
                    .executeQuery();
            obj = filter.onExecute(res);
            res.close();
        } catch (SQLException e) {
            this.plugin.sendConsoleMessage(Level.SEVERE,
                    e.getMessage());
        } finally {
            try {
                if (res != null) res.close();
            } catch (Exception e) {
                this.plugin.sendConsoleMessage(Level.SEVERE,
                        e.getMessage());
                res = null;
            }
        }

        return obj;
    }

    private void confirmConnection() {
        if (connection == null) {
            if (getDatabaseType().equals(DatabaseType.MYSQL)) {
                connection = new DatabaseConnection(getPlugin(), hostname, port,
                        database, username, password);
            } else {
                if (dbName == null) {
                    connection = new DatabaseConnection(getPlugin());
                } else {
                    connection = new DatabaseConnection(getPlugin(), dbName);
                }
            }
        }
    }

    public final Plugin getPlugin() {
        return this.plugin;
    }

    public final DatabaseConnection getDatabaseConnection() {
        return this.connection;
    }

    public final DatabaseType getDatabaseType() {
        return this.type;
    }

    public enum DatabaseType {
        MYSQL,
        SQLITE
    }

}
