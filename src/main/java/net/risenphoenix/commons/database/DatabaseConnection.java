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

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.risenphoenix.commons.Plugin;

import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public class DatabaseConnection {

    private final Plugin plugin;
    public Connection c = null;

    private String driver;
    private String connectionString;

    private ComboPooledDataSource pooledDataSource = null;

    // SQLite Connection Initializer
    public DatabaseConnection(final Plugin plugin) {
        this.plugin = plugin;

        driver = "org.sqlite.JDBC";
        connectionString = "jdbc:sqlite:" +
                new File(this.plugin.getDataFolder() + File.separator +
                        "store.db").getAbsolutePath();
        this.openConnection();
    }

    public DatabaseConnection(final Plugin plugin, final String dbName) {
        this.plugin = plugin;

        driver = "org.sqlite.JDBC";
        connectionString = "jdbc:sqlite:" +
                new File(this.plugin.getDataFolder() + File.separator +
                        dbName + ".db").getAbsolutePath();
        this.openConnection();
    }

    // MySQL Connection Initializer
    public DatabaseConnection(final Plugin plugin, String hostname, int port,
                              String database, String username, String pwd) {
        this.plugin = plugin;

        driver = "com.mysql.jdbc.Driver";
        connectionString = "jdbc:mysql://" + hostname + ":" + port + "/" +
                database + "?user=" + username + "&password=" + pwd;
        this.openConnection();
    }

    // MySQL (Pooled) Connection Initializer
    public DatabaseConnection(final Plugin plugin, String hostname, int port,
                              String database, String username, String pwd, int
                              poolSize) {
        this.plugin = plugin;

        driver = "com.mysql.jdbc.Driver";
        connectionString = "jdbc:mysql://" + hostname + ":" + port + "/" +
                database;

        pooledDataSource = new ComboPooledDataSource();

        try {
            pooledDataSource.setDriverClass(driver);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

        pooledDataSource.setJdbcUrl(connectionString);
        pooledDataSource.setUser(username);
        pooledDataSource.setPassword(pwd);
        pooledDataSource.setMaxPoolSize(poolSize);
    }



    public Connection openConnection() {
        try {
            Class.forName(this.driver);
            this.c = DriverManager.getConnection(this.connectionString);

            // Output Message
            this.plugin.sendConsoleMessage(Level.INFO,
                    this.plugin.getLocalizationManager().
                            getLocalString("DB_OPEN_SUC"));

            return c;
        } catch (SQLException e) {
            this.plugin.sendConsoleMessage(Level.SEVERE,
                    this.plugin.getLocalizationManager().
                            getLocalString("DB_CNCT_ERR") + e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
            this.plugin.sendConsoleMessage(Level.SEVERE,
                    this.plugin.getLocalizationManager().
                            getLocalString("BAD_DB_DRVR") + this.driver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.c;
    }

    public void closeConnection() {
        try {
            if (this.c != null) c.close();
            this.plugin.sendConsoleMessage(Level.INFO,
                    this.plugin.getLocalizationManager().
                            getLocalString("DB_CLOSE_SUC"));
        } catch (SQLException e) {
            this.plugin.sendConsoleMessage(Level.SEVERE,
                    this.plugin.getLocalizationManager().
                            getLocalString("DB_CLOSE_ERR") + e.getLocalizedMessage());
        }

        this.c = null;
    }

    public Connection getConnection() {
        if (pooledDataSource != null) {
            try {
                return pooledDataSource.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        return this.c;
    }

    public boolean isConnected() {
        try {
            return (c == null || c.isClosed()) ? false : true;
        } catch (SQLException e) {
            this.plugin.sendConsoleMessage(Level.SEVERE,
                    e.getLocalizedMessage());
            return false;
        }
    }

    public Result query(final PreparedStatement stmt) {
        return query(stmt, true);
    }

    public Result query(final PreparedStatement stmt, boolean retry) {
        try {
            if (!isConnected()) openConnection();

            if (stmt.execute())
                return new Result(stmt, stmt.getResultSet());
        } catch (final SQLException e) {
            this.plugin.sendConsoleMessage(Level.SEVERE,
                    this.plugin.getLocalizationManager().
                            getLocalString("DB_QUERY_ERR") + e.getMessage());

            if (retry && e.getMessage().contains("_BUSY")) {
                this.plugin.sendConsoleMessage(Level.SEVERE,
                        this.plugin.getLocalizationManager().
                                getLocalString("DB_QUERY_RETRY") + e.getMessage());

                this.plugin.getServer().getScheduler()
                        .scheduleSyncDelayedTask(this.plugin,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        query(stmt, false);
                                    }
                                }, 20);
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.getStackTrace();
            }
        }

        return null;
    }

    // Result Object
    public class Result {
        private ResultSet resultSet;
        private Statement statement;

        public Result(Statement stmt, ResultSet rset) {
            this.statement = stmt;
            this.resultSet = rset;
        }

        public ResultSet getResultSet() {
            return this.resultSet;
        }

        public String getStatement() {
            return this.statement.toString();
        }

        public void close() {
            try {
                this.statement.close();
                this.resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
