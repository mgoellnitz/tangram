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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mockftpserver.core.command.AbstractTrackingCommandHandler;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.CommandNames;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.components.test.GenericCodeResourceCacheTest;
import org.tangram.content.CodeResourceCache;
import org.tangram.ftp.ListFtpCommandHandler;
import org.tangram.ftp.SessionHelper;
import org.tangram.ftp.TangramFtpServer;
import org.tangram.mock.MockMutableBeanFactory;
import org.testng.annotations.Test;


/**
 * Test aspects of the ftp command handler for listing folders.
 */
public class ListFtpCommandHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ListFtpCommandHandlerTest.class);


    @Test
    public void testListFtpCommandHandler() throws Exception {
        MockMutableBeanFactory beanFactory = new MockMutableBeanFactory();
        beanFactory.init();
        GenericCodeResourceCacheTest codeCacheTest = new GenericCodeResourceCacheTest();
        codeCacheTest.init();
        CodeResourceCache codeCache = codeCacheTest.getInstance();
        TangramFtpServer ftpServer = new TangramFtpServer(beanFactory, codeCache);
        Map<String, AbstractTrackingCommandHandler> commands = ftpServer.getCommands();
        ListFtpCommandHandler listFtpCommandHandler = (ListFtpCommandHandler) commands.get(CommandNames.LIST);
        List<String> params = new ArrayList<>();
        Command command = new Command(CommandNames.LIST, params);
        Socket socket = Mockito.mock(Socket.class);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream("list\r\n".getBytes("UTF-8"));
        InetAddress me = InetAddress.getByName("localhost");
        Mockito.when(socket.getInetAddress()).thenReturn(me);
        Mockito.when(socket.getLocalAddress()).thenReturn(me);
        Mockito.when(socket.getOutputStream()).thenReturn(output);
        Mockito.when(socket.getInputStream()).thenReturn(input);
        MockSession session = new MockSession(socket, commands);
        LOG.debug("testListFtpCommandHandler() run");
        Thread t = new Thread(session);
        t.start();
        Thread.sleep(100);
        LOG.debug("testListFtpCommandHandler() list");
        listFtpCommandHandler.handleCommand(command, session);
        session.setAttribute(SessionHelper.CURRENT_DIR, "/groovy");
        listFtpCommandHandler.handleCommand(command, session);
        LOG.debug("testListFtpCommandHandler() close");
        session.close();
        LOG.debug("testListFtpCommandHandler() join");
        t.join(5000);
    } // testListFtpCommandHandler()

} // ListFtpCommandHandlerTest
