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

package net.risenphoenix.commons.commands.parsers;

import net.risenphoenix.commons.commands.Command;
import net.risenphoenix.commons.commands.CommandManager;
import net.risenphoenix.commons.commands.ComparisonResult;

public class StaticParser extends Parser {

    public StaticParser(CommandManager mngr, Command cmd, String[] args) {
        super(mngr, cmd, args);
    }

    @Override
    public ComparisonResult parseCommand() {
        int callSize = this.cmd.getCallArgs().length;
        int inputSize = this.input.length;

        String[] COMMAND_ARGS;
        String[] INPUT_ARGS;

        if (callSize > inputSize) {
            COMMAND_ARGS = new String[callSize];
            INPUT_ARGS = new String[callSize];
        } else {
            COMMAND_ARGS = new String[inputSize];
            INPUT_ARGS = new String[inputSize];
        }

        for (int i = 0; i < this.cmd.getCallArgs().length; i++) {
            COMMAND_ARGS[i] = this.cmd.getCallArgs()[i];
        }
        for (int i = 0; i < this.input.length; i++) {
            INPUT_ARGS[i] = this.input[i];
        }

        if (callSize > inputSize) {
            for (int i = inputSize; i < INPUT_ARGS.length; i++) {
                INPUT_ARGS[i] = "null";
            }
        } else {
            for (int i = callSize; i < COMMAND_ARGS.length; i++) {
                COMMAND_ARGS[i] = "null";
            }
        }

        // Check for Matching Arguments
        for (int i = 0; i < COMMAND_ARGS.length; i++) {
            // Debug Output
            if (this.cmdManager.debugMode()) {
                System.out.println("Command Expected: " + COMMAND_ARGS[i]);
                System.out.println("Received: " + INPUT_ARGS[i]);
            }

            if (COMMAND_ARGS[i].equals("null")) {
                return ComparisonResult.ARG_ERR;
            }

            if (!INPUT_ARGS[i].equalsIgnoreCase(COMMAND_ARGS[i])) {
                return ComparisonResult.BAD;
            }
        }

        return ComparisonResult.GOOD;
    }

}
