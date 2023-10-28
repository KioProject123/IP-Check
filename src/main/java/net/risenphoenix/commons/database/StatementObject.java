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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class StatementObject {

    private final Plugin plugin;
    private String SQL;
    private Object[] values = null;

    public StatementObject(final Plugin plugin, String SQL) {
        this.plugin = plugin;
        this.SQL = SQL;
    }

    public StatementObject(final Plugin plugin, String SQL, Object[] values) {
        this.plugin = plugin;
        this.SQL = SQL;
        this.values = values;
    }

    public PreparedStatement getStatement(Connection c) {
        try {
            PreparedStatement prepStmt = c.prepareStatement(this.SQL);

            if (this.values != null) {
                for (int i = 1; i <= this.values.length; i++) {

                    if (this.values[i-1] instanceof Integer) {
                        prepStmt.setInt(i, (Integer) this.values[i-1]);
                        continue;
                    }

                    if (this.values[i-1] instanceof String) {
                        prepStmt.setString(i, this.values[i-1].toString());
                        continue;
                    }

                    this.plugin.sendConsoleMessage(Level.SEVERE, this.plugin
                            .getLocalizationManager()
                            .getLocalString("BAD_SQL_INPUT"));
                }
            }

            return prepStmt;
        } catch (Exception e) {
            this.plugin.sendConsoleMessage(Level.SEVERE, this.plugin
                    .getLocalizationManager()
                    .getLocalString("DB_PREP_STMT_ERR"));
            e.printStackTrace();
        }

        return null;
    }
}
