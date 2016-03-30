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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import org.mockftpserver.core.command.AbstractTrackingCommandHandler;
import org.mockftpserver.core.command.Command;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.components.test.GenericCodeResourceCacheTest;
import org.tangram.content.CodeResourceCache;
import org.tangram.ftp.TangramFtpServer;
import org.tangram.mock.MockMutableBeanFactory;


/**
 * Helper class for ftp test.
 *
 * provides common mock implementations for ftp handler tests.
 */
public class FtpTestHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FtpTestHelper.class);

    private final String command;

    private final byte[] bytes;

    private final Map<String, AbstractTrackingCommandHandler> commands;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();


    /**
     * Create a new helper instance.
     *
     * @param commandName Internal name from Commands class of MockFtpServer
     * @throws UnsupportedEncodingException should only happen if UTF-8 is not available on your system
     * @throws FileNotFoundException thrown when the default xml mock content file cannot be read
     */
    public FtpTestHelper(String commandName) throws UnsupportedEncodingException, FileNotFoundException {
        command = commandName;
        bytes = (commandName.toLowerCase()+"\r\n").getBytes("UTF-8");
        MockMutableBeanFactory beanFactory = new MockMutableBeanFactory();
        CodeResourceCache codeCache = new GenericCodeResourceCacheTest().getInstance();
        TangramFtpServer ftpServer = new TangramFtpServer(beanFactory, codeCache);
        commands = ftpServer.getCommands();
    } // ()


    public Map<String, AbstractTrackingCommandHandler> getCommands() {
        return commands;
    } // getCommands()


    public AbstractTrackingCommandHandler getHandler() throws FileNotFoundException {
        return commands.get(command);
    } // getHandler()


    public Command getCommand(List<String> params) {
        return new Command(command, params);
    } // getCommand()


    public Socket getSocket() throws IOException {
        Socket socket = Mockito.mock(Socket.class);
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        InetAddress me = InetAddress.getByName("localhost");
        Mockito.when(socket.getInetAddress()).thenReturn(me);
        Mockito.when(socket.getLocalAddress()).thenReturn(me);
        Mockito.when(socket.getOutputStream()).thenReturn(output);
        Mockito.when(socket.getInputStream()).thenReturn(input);
        return socket;
    } // getSocket()


    /**
     * Get output collected via the data socket.
     *
     * @return UTF-8 string representation of the output
     * @throws UnsupportedEncodingException onyl thrown if your system doesn't know UTF-8
     */
    public String getOutput() throws UnsupportedEncodingException {
        return new String(output.toByteArray(), "UTF-8");
    } // getOutput()

} // FtpTestHelper()
