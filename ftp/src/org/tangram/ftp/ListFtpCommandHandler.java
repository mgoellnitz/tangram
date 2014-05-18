/**
 *
 * Copyright 2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.ftp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.command.ListCommandHandler;
import org.tangram.components.CodeResourceCache;
import org.tangram.content.CodeHelper;
import org.tangram.content.CodeResource;


/**
 *
 * List ftp command.
 *
 * Generates a listing for the given parameters.
 *
 */
public class ListFtpCommandHandler extends ListCommandHandler {

    private static final Log log = LogFactory.getLog(ListFtpCommandHandler.class);

    private CodeResourceCache codeResourceCache;

    private final static String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May",
        "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final static String FILE_PREFIX = "-rw-r--r--   1 tangram tangram ";

    private final static String DIR_PREFIX = "drwxr-xr-x   1 tangram tangram ";


    public ListFtpCommandHandler(CodeResourceCache codeResourceCache) {
        this.codeResourceCache = codeResourceCache;
    } // ListFtpCommandHandler()


    private String getUnixDate(long millis) {
        if (millis<0) {
            return "------------";
        } // if

        StringBuilder sb = new StringBuilder(16);
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);

        // month
        sb.append(MONTHS[cal.get(Calendar.MONTH)]);
        sb.append(' ');

        // day
        int day = cal.get(Calendar.DATE);
        if (day<10) {
            sb.append(' ');
        } // if
        sb.append(day);
        sb.append(' ');

        long sixMonth = 15811200000L; // 183L * 24L * 60L * 60L * 1000L;
        long nowTime = System.currentTimeMillis();
        if (Math.abs(nowTime-millis)>sixMonth) {
            // year
            int year = cal.get(Calendar.YEAR);
            sb.append(' ');
            sb.append(year);
        } else {
            // hour
            int hh = cal.get(Calendar.HOUR_OF_DAY);
            if (hh<10) {
                sb.append('0');
            } // if
            sb.append(hh);
            sb.append(':');

            // minute
            int mm = cal.get(Calendar.MINUTE);
            if (mm<10) {
                sb.append('0');
            }
            sb.append(mm);
        } // if
        return sb.toString();
    } // getUnixDate()


    @Override
    protected void processData(Command command, Session session, InvocationRecord invocationRecord) {
        String options = command.getParameter(0);
        String dir = SessionHelper.getCwd(session);
        if (log.isInfoEnabled()) {
            log.info("handleCommand() listing directory with options "+options+" in directory "+dir);
        } // if

        // TODO: make codes stored with real modification time
        String now = getUnixDate(codeResourceCache.getLastUpdate());

        StringBuilder listing = new StringBuilder();
        Set<String> types = codeResourceCache.getTypes();
        if (dir.length()==1) {
            if (log.isInfoEnabled()) {
                log.info("handleCommand() root listing of all type directories");
            } // if
            for (String type : types) {
                String name = CodeHelper.getFolder(type);
                String item = DIR_PREFIX+"1 "+now+" "+name+"\n";
                listing.append(item);
            } // for
        } else {
            dir = SessionHelper.getDirectoy(session);
            if (log.isInfoEnabled()) {
                log.info("handleCommand() listing for directory "+dir);
            } // if
            String type = CodeHelper.getMimetype(dir);
            if (types.contains(type)) {
                String extension = CodeHelper.getExtension(type);
                Map<String, CodeResource> resources = codeResourceCache.getTypeCache(type);
                for (CodeResource code : resources.values()) {
                    String item = FILE_PREFIX+code.getSize()+" "+now+" "+code.getAnnotation()+extension+"\n";
                    listing.append(item);
                } // for
            } // if
        } // if
        session.sendData(listing.toString().getBytes(), listing.length());
    } // proocessData()

} // ListFtpCommandHandler
