/**
 *
 * Copyright 2013-2016 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.ftp;

import java.util.HashMap;
import java.util.Map;
import org.mockftpserver.core.command.AbstractTrackingCommandHandler;
import org.mockftpserver.core.command.CommandHandler;
import org.mockftpserver.core.command.CommandNames;
import org.mockftpserver.core.command.ConnectCommandHandler;
import org.mockftpserver.core.command.ReplyTextBundleUtil;
import org.mockftpserver.core.command.UnsupportedCommandHandler;
import org.mockftpserver.core.server.AbstractFtpServer;
import org.mockftpserver.stub.command.AborCommandHandler;
import org.mockftpserver.stub.command.AcctCommandHandler;
import org.mockftpserver.stub.command.AlloCommandHandler;
import org.mockftpserver.stub.command.AppeCommandHandler;
import org.mockftpserver.stub.command.CdupCommandHandler;
import org.mockftpserver.stub.command.DeleCommandHandler;
import org.mockftpserver.stub.command.EprtCommandHandler;
import org.mockftpserver.stub.command.HelpCommandHandler;
import org.mockftpserver.stub.command.MkdCommandHandler;
import org.mockftpserver.stub.command.ModeCommandHandler;
import org.mockftpserver.stub.command.NlstCommandHandler;
import org.mockftpserver.stub.command.NoopCommandHandler;
import org.mockftpserver.stub.command.PasvCommandHandler;
import org.mockftpserver.stub.command.PortCommandHandler;
import org.mockftpserver.stub.command.QuitCommandHandler;
import org.mockftpserver.stub.command.RmdCommandHandler;
import org.mockftpserver.stub.command.StatCommandHandler;
import org.mockftpserver.stub.command.StouCommandHandler;
import org.mockftpserver.stub.command.StruCommandHandler;
import org.mockftpserver.stub.command.SystCommandHandler;
import org.mockftpserver.stub.command.TypeCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.content.CodeResourceCache;
import org.tangram.mutable.MutableBeanFactory;


/**
 *
 * Ftp Server stub to access tangram repository elements.
 *
 */
public class TangramFtpServer extends AbstractFtpServer {

    private static final Logger LOG = LoggerFactory.getLogger(TangramFtpServer.class);

    private final Map<String, AbstractTrackingCommandHandler> commands = new HashMap<>();


    /**
     * Create a new instance. Initialize the default command handlers and reply text ResourceBundle.
     *
     * @param beanFactory bean factory instance to to content from and store in.
     * @param codeResourceCache code access facade for the bean factory.
     */
    public TangramFtpServer(MutableBeanFactory<?> beanFactory, CodeResourceCache codeResourceCache) {
        PwdFtpCommandHandler pwdCommandHandler = new PwdFtpCommandHandler();

        // Initialize the default CommandHandler mappings
        commands.put(CommandNames.ABOR, new AborCommandHandler());
        commands.put(CommandNames.ACCT, new AcctCommandHandler());
        commands.put(CommandNames.ALLO, new AlloCommandHandler());
        commands.put(CommandNames.APPE, new AppeCommandHandler());
        commands.put(CommandNames.PWD, pwdCommandHandler);            // same as XPWD
        commands.put(CommandNames.CONNECT, new ConnectCommandHandler());
        commands.put(CommandNames.CWD, new CwdFtpCommandHandler());
        commands.put(CommandNames.CDUP, new CdupCommandHandler());
        commands.put(CommandNames.DELE, new DeleCommandHandler());
        commands.put(CommandNames.EPRT, new EprtCommandHandler());
        commands.put(CommandNames.HELP, new HelpCommandHandler());
        commands.put(CommandNames.LIST, new ListFtpCommandHandler(codeResourceCache));
        commands.put(CommandNames.MKD, new MkdCommandHandler());
        commands.put(CommandNames.MODE, new ModeCommandHandler());
        commands.put(CommandNames.NOOP, new NoopCommandHandler());
        commands.put(CommandNames.NLST, new NlstCommandHandler());
        commands.put(CommandNames.PASS, new PassFtpCommandHandler(codeResourceCache));
        commands.put(CommandNames.PASV, new PasvCommandHandler());
        commands.put(CommandNames.PORT, new PortCommandHandler());
        commands.put(CommandNames.RETR, new RetrFtpCommandHandler(codeResourceCache));
        commands.put(CommandNames.QUIT, new QuitCommandHandler());
        commands.put(CommandNames.RMD, new RmdCommandHandler());
        commands.put(CommandNames.RNFR, new RnfrFtpCommandHandler(codeResourceCache));
        commands.put(CommandNames.RNTO, new RntoFtpCommandHandler(beanFactory));
        commands.put(CommandNames.STAT, new StatCommandHandler());
        commands.put(CommandNames.STOR, new StorFtpCommandHandler(beanFactory, codeResourceCache));
        commands.put(CommandNames.STOU, new StouCommandHandler());
        commands.put(CommandNames.STRU, new StruCommandHandler());
        SystCommandHandler syst = new SystCommandHandler();
        syst.setSystemName("unix");
        commands.put(CommandNames.SYST, syst);
        commands.put(CommandNames.TYPE, new TypeCommandHandler());
        commands.put(CommandNames.USER, new UserFtpCommandHandler());
        commands.put(CommandNames.UNSUPPORTED, new UnsupportedCommandHandler());
        commands.put(CommandNames.XPWD, pwdCommandHandler); // same as PWD

        setCommandHandlers(commands);
    } // TangramFtpServer()


    @Override
    protected void initializeCommandHandler(CommandHandler commandHandler) {
        ReplyTextBundleUtil.setReplyTextBundleIfAppropriate(commandHandler, getReplyTextBundle());
    } // initializeCommandHandler()


    public Map<String, AbstractTrackingCommandHandler> getCommands() {
        return commands;
    } // getCommands()


    @Override
    protected void finalize() throws Throwable {
        for (String key : commands.keySet()) {
            LOG.info(key+": "+commands.get(key).numberOfInvocations());
        } // for
        super.finalize();
    } // finalize()

} // TangramFtpServer
