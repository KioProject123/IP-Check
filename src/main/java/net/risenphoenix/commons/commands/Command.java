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

package net.risenphoenix.commons.commands;

import net.risenphoenix.commons.Plugin;
import net.risenphoenix.commons.localization.LocalizationManager;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.permissions.Permission;

import java.util.logging.Level;

/**
 * Base class for Plugin Commands. This class contains common shared methods
 * which all plugin commands inherit.</br>
 * <p/>
 * </br>This class is responsible for performing checks for player permissions,
 * storing and providing command specific variables such as Name, Syntax and
 * Help Text, as well as providing access to Global Resources such as the
 * Localization Manager.
 *
 * @author Jacob Keep (Jnk1296)
 */

public class Command extends ValidatingPrompt {
    private final Plugin plugin;
    private final CommandType type;

    private String name;
    private String syntax;
    private String help;

    private boolean isConsoleExecutable = true;

    private String[] callArgs;
    private int requiredArgs = 0;

    private Permission[] commandPerms = null;

    private LocalizationManager LM;
    private ConversationFactory conFactory = null;

    /**
     * <p>
     * <strong>Constructor:</strong> Responsible for the main initialization of
     * the Command instance. Registers the required arguments for the command to
     * be identified, as well as the number of arguments needed to successfully
     * call the command. The Plugin instance is required to link to the
     * LocalizationManager.</p>
     * <p/>
     * <br><strong>callArgs</strong> contains the sic arguments which would be
     * typed in-game to call the command, but not those which are variables.
     * <strong>Ex: </strong><em>/ipc exempt-list ip</em> <strong>-></strong>
     * { "ipc", "exempt-list", "ip" }.
     * from the arguments array.
     *
     * @param plugin
     * @param callArgs
     * @param type
     */

    public Command(final Plugin plugin, String[] callArgs, CommandType type) {
        this.plugin = plugin;
        this.callArgs = callArgs;
        this.type = type;
        this.LM = this.plugin.getLocalizationManager();
    }

    /**
     * Called by the CommandManager during execution. This method is the public
     * executor for all Commands, and should be called whenever you wish to
     * execute a Command in your code.</br>
     * <p/>
     * </br>The method returns a boolean value depending on whether the
     * CommandSender Object passed can call the command or not.</br>
     * <p/>
     * </br><b>Operation is as follows: <ul>
     * <p/>
     * <li></b>When called, the method
     * checks the CommandSender object passed for OP Status. If the CS has OP
     * Status, all further checks are skipped and <b>onExecute()</b> is called,
     * causing the method to return <b>TRUE</b>.</li>
     * <p/>
     * <li>If the CS object does not have OP Status, the method then checks to
     * see if the Command Instance has any permissions specified in
     * <b>commandPerms</b>.</li>
     * <p/>
     * <li>If permissions are found, they are looped through and checked against
     * the CS Object one by one. Should the CS Object fail to have any of the
     * specified permissions, the method returns <b>FALSE</b>.</li></ul>
     */

    public final boolean onCall(CommandSender sender, String[] args) {
        if (!canExecute(sender)) return false;
        onExecute(sender, args);
        return true;
    }

    /**
     * Sets whether this Command can be called by Console or not.</br>
     * <p/>
     * </br>Depending on the status of this flag, you can limit this Command to
     * being a Player-only Command, meaning that Console cannot execute this
     * Command.</br>
     * <p/>
     * </br><strong>TRUE</strong> = Console can execute, <strong>FALSE</strong>
     * = Console cannot execute.
     *
     * @param consoleCanExecute
     */
    public final void setConsoleExecutable(boolean consoleCanExecute) {
        this.isConsoleExecutable = consoleCanExecute;
    }

    /**
     * The main execution method for this Command.</br>
     * <p/>
     * </br>The only overrideable method of this Command, this method contains
     * the code that your Command will execute when onCall() is called. The
     * method can be set to return any number of Types, but doing so will break
     * compliance with the API. Also, such Type specification will not work, as
     * the result of this method (void) is returned to the onCall method, which
     * also returns void.</br>
     * <p/>
     * </br>If you wish for this Command to return a value other than <strong>
     * void</strong>, you must set the return type of both onExecute() and
     * onCall() so that they both return the same Type.
     * <p>
     * <strong>NOTE: </strong>when performing actions involving the commands, be
     * aware that the root command is omitted from the arguments array <strong>
     * BEFORE</strong> the arguments reach this method.
     * </p>
     */
    public void onExecute(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException(
                this.getLocalString("NO_IMPLEMENT"));
    }

    /**
     * Returns the Name of this Command.
     *
     * @return String
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Sets the Display Name for this Command.</br>
     * <p/>
     * </br>The name can be any string.
     *
     * @param cmdName
     */
    public final void setName(String cmdName) {
        this.name = cmdName;
    }

    /**
     * Returns the Syntax of this Command, minus the root.
     *
     * @return String
     */
    public final String getSyntax() {
        return this.syntax;
    }

    /**
     * Sets the syntax for this Command.</br>
     * <p/>
     * </br>When set, the string passed should contain the literal (sic) syntax
     * to be used when calling your command, <strong>minus the root command
     * </strong>. The syntax string is generally used for displaying help
     * information and should be relatively easy to understand.</br>
     * <p/>
     * </br>Ex: <em>ban {@literal <}player{@literal >} [message]</em>
     *
     * @param cmdSyntax
     */
    public final void setSyntax(String cmdSyntax) {
        this.syntax = cmdSyntax;
    }

    /**
     * Returns the Help Description text for this Command.
     *
     * @return String
     */
    public final String getHelp() {
        return this.help;
    }

    /**
     * Sets the Help Description text for this command.</br>
     * <p/>
     * </br>This text is displayed in the Help Command and is used to describe
     * what this Command does, as well as any other information the end user may
     * need to know.
     *
     * @param helpDesc
     */
    public final void setHelp(String helpDesc) {
        this.help = helpDesc;
    }

    /**
     * Returns a Permission[] containing all the permissions this Command
     * requires to be executed.
     *
     * @return Permission[]
     */
    public final Permission[] getPermissions() {
        return this.commandPerms;
    }

    /**
     * Sets the permissions required for this Command to be executed.</br>
     * <p/>
     * </br>The permission should be passed as an array like such:<ul>
     * <li><em>new Permission[]{ new Permission("permission.one"), new
     * Permission ("permission.two"), etc... };</em></li></ul>
     *
     * @param perms
     */
    public final void setPermissions(Permission[] perms) {
        this.commandPerms = perms;
    }

    /**
     * Returns a boolean denoting whether this command can be executed by the
     * Console or not.
     *
     * @return boolean
     */
    public final boolean canConsoleExecute() {
        return this.isConsoleExecutable;
    }

    /**
     * Returns a String[] containing all the arguments required to be typed by
     * the end user for this Command to be identified by the Command Manager.
     *
     * @return String[]
     */
    public final String[] getCallArgs() {
        return this.callArgs;
    }

    /**
     * Returns an integer of the number of arguments required for this Command
     * to be identified by the Command Manager.
     *
     * @return int
     */
    @Deprecated
    public final int getArgumentsRequired() {
        return this.requiredArgs;
    }

    /**
     * This method checks the CommandSender Object passed against the
     * permissions this Command requires in order to be executed. If the CS
     * Object has all the required permissions, this method will return <strong>
     * TRUE</strong>, else it will return <strong>FALSE</strong>.
     *
     * @param sender
     * @return boolean
     */
    public final boolean canExecute(CommandSender sender) {
        if (this.commandPerms == null || sender.isOp()) return true;

        for (int i = 0; i < this.commandPerms.length; i++) {
            if (!sender.hasPermission(this.commandPerms[i])) return false;
        }

        return true;
    }

    /**
     * This method is used to obtain localized messages from the Localization
     * Manager. In order to get a Localized Message, a String <strong><em>key
     * </em></strong> must be passed which corresponds to the message in the
     * Localization Manager.
     *
     * @param key
     * @return String
     */
    public final String getLocalString(String key) {
        return this.LM.getLocalString(key);
    }

    /**
     * @return
     */
    public final Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * @return
     */
    public final CommandType getType() {
        return this.type;
    }

    /**
     * @param sender
     * @param message
     */
    public final void sendPlayerMessage(CommandSender sender, String message) {
        this.sendPlayerMessage(sender, message, true);
    }

    /**
     * @param sender
     * @param message
     * @param useName
     */
    public final void sendPlayerMessage(CommandSender sender, String message,
                                        boolean useName) {
        this.plugin.sendPlayerMessage(sender, message, useName);
    }

    /**
     * @param level
     * @param message
     */
    public final void sendConsoleMessage(Level level, String message) {
        this.plugin.sendConsoleMessage(level, message);
    }

    /**
     *
     * @param factory
     */
    public final void setConversationFactory(ConversationFactory factory) {
        this.conFactory = factory;
    }

    /**
     *
     * @return
     */
    public final ConversationFactory getConversationFactory() {
        return this.conFactory;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        throw new UnsupportedOperationException(
                this.getLocalString("NO_IMPLEMENT"));
    }

    @Override
    public Prompt acceptValidatedInput(ConversationContext context, String in) {
        throw new UnsupportedOperationException(
                this.getLocalString("NO_IMPLEMENT"));
    }

    @Override
    public boolean isInputValid (ConversationContext context, String in) {
        throw new UnsupportedOperationException(
                this.getLocalString("NO_IMPLEMENT"));
    }
}
