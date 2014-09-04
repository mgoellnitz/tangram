/**
 *
 * Copyright 2011 Martin Goellnitz
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
package org.tangram.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * JUL logging formatter for a nicer log output.
 */
public class LogFormatter extends Formatter {

    private final Date dat = new Date();

    private final static MessageFormat DATE_FORMAT = new MessageFormat("{0,date} {0,time}");

    private final Object[] args = new Object[1];


    /**
     * Format the given LogRecord.
     *
     * @param record
     * the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(256);
        StringBuffer text = new StringBuffer(32);
        synchronized (this) {
            // Minimize memory allocations here.
            dat.setTime(record.getMillis());
            args[0] = dat;
            DATE_FORMAT.format(args, text, null);
            sb.append(text);
        } // synchronized
        sb.append(" xx ");
        sb.append(record.getLevel().getLocalizedName().toUpperCase());
        sb.append(": ");
        String sourceClassName = record.getSourceClassName();
        if (sourceClassName!=null) {
            int idx = sourceClassName.lastIndexOf('.');
            if (idx>0) {
                sourceClassName = sourceClassName.substring(idx);
            } // if
            sb.append(sourceClassName);
        } else {
            sb.append(record.getLoggerName());
        } // if
        String methodName = record.getSourceMethodName();
        String message = formatMessage(record);
        if (methodName!=null) {
            sb.append('.');
            sb.append(methodName);
            if (message.startsWith(methodName)) {
                message = message.substring(methodName.length());
            } // if
        } // if
        if (!message.startsWith("(")) {
            sb.append("() ");
        } // if
        sb.append(message);
        sb.append('\n');
        if (record.getThrown()!=null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
        } // if
        return sb.toString();
    } // format()

} // LogFormatter
