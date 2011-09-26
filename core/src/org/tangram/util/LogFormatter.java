package org.tangram.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    Date dat = new Date();

    private final static String format = "{0,date} {0,time}";

    private MessageFormat formatter;

    private Object args[] = new Object[1];


    /**
     * Format the given LogRecord.
     * 
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        // Minimize memory allocations here.
        dat.setTime(record.getMillis());
        args[0] = dat;
        StringBuffer text = new StringBuffer();
        if (formatter==null) {
            formatter = new MessageFormat(format);
        } // if
        formatter.format(args, text, null);
        sb.append(text);
        sb.append(" ");
        sb.append(record.getLevel().getLocalizedName().toUpperCase());
        sb.append(": ");
        if (record.getSourceClassName()!=null) {
            sb.append(record.getSourceClassName());
        } else {
            sb.append(record.getLoggerName());
        } // if
        String methodName = record.getSourceMethodName();
        if (methodName!=null) {
            sb.append(".");
            sb.append(methodName);
        } // if
        String message = formatMessage(record);
        if (message.startsWith(methodName)) {
            message = message.substring(methodName.length());
        } // if
        if (message.startsWith("(")) {
        } else {
            sb.append("() ");
        } // if
        sb.append(message);
        sb.append("\n");
        if (record.getThrown()!=null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        } // if
        return sb.toString();
    } // format()

} // LogFormatter
