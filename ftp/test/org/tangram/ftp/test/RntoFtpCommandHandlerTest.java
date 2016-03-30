/*
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.ftp.test;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.ftp.RntoFtpCommandHandler;
import org.tangram.ftp.SessionHelper;
import org.tangram.mock.content.MockMutableCode;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test aspects of the ftp command handler for renaming.
 */
public class RntoFtpCommandHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(RntoFtpCommandHandlerTest.class);


    @Test
    public void testRntoFtpCommandHandler() throws Exception {
        String id = "CodeResource:10";
        FtpTestHelper helper = new FtpTestHelper(CommandNames.RNTO);
        MockMutableCode code = helper.getBeanFactory().getBean(MockMutableCode.class, id);
        Assert.assertEquals(code.getAnnotation(), "org.tangram.link.LinkHandler", "unexpected initial value for annotation.");
        RntoFtpCommandHandler rntoFtpCommandHandler = (RntoFtpCommandHandler) helper.getHandler();
        List<String> params = new ArrayList<>();
        params.add("org.tangram.link.GroovyLinkHandler.groovy");
        Command command = helper.getCommand(params);
        Socket socket = helper.getSocket();
        MockSession session = new MockSession(socket, helper.getCommands());
        LOG.debug("testRntoFtpCommandHandler() run");
        Thread t = new Thread(session);
        t.start();
        Thread.sleep(100);
        LOG.debug("testRntoFtpCommandHandler() list");
        session.setAttribute(SessionHelper.CURRENT_DIR, "/groovy");
        session.setAttribute(SessionHelper.RENAME_ID, id);
        rntoFtpCommandHandler.handleCommand(command, session);
        LOG.debug("testRntoFtpCommandHandler() close");
        session.close();
        LOG.debug("testRntoFtpCommandHandler() join");
        t.join(5000);
        code = helper.getBeanFactory().getBean(MockMutableCode.class, id);
        Assert.assertEquals(code.getAnnotation(), "org.tangram.link.GroovyLinkHandler", "unexpected new value for annotation.");
    } // testRntoFtpCommandHandler()

} // RntoFtpCommandHandlerTest
