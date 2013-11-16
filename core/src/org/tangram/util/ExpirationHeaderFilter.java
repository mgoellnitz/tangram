package org.tangram.util;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Set some static but configurable expiration times in HTTP headers.
 *
 * Quite simple but in many situation usable servlet filter to set some reasonable, cache-friendly and thus
 * user friendly default expiration headers, which might of course subsequently be overridden in the response.
 * Since we can't set header after we come back in the chain and content type is only known after that call,
 * we use extensions here and map them to times.
 */
public class ExpirationHeaderFilter implements Filter {

    private static final Log log = LogFactory.getLog(ExpirationHeaderFilter.class);

    private Map<String, Long> extensionTimes = new HashMap<String, Long>();

    private DateFormat formatter;

    private String startTimeString;
    private String startTimeHeader;


    public ExpirationHeaderFilter() {
        formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        final long startTime = System.currentTimeMillis();
        startTimeString = "\""+startTime+"-";
        startTimeHeader = formatter.format(new Date(startTime));
    } // ExpirationHeaderFilter()


    public void destroy() {
        extensionTimes = new HashMap<String, Long>();
    } // destroy()


    private Long getTimeObject(String contentType) {
        if (contentType==null) {
            return extensionTimes.get("DEFAULT");
        } // if
        Long object = extensionTimes.get(contentType.split(";")[0]);
        if (object==null) {
            return extensionTimes.get("DEFAULT");
        } else {
            return object;
        } // if
    } // getTimeObject()


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("X-Tangram", "ExpirationFilter");
        String uri = ((HttpServletRequest) req).getRequestURI();
        int idx = uri.lastIndexOf('.');
        if (idx>0) {
            String extension = uri.substring(idx+1);
            Long timeObject = getTimeObject(extension);
            if (log.isInfoEnabled()) {
                log.info("doFilter("+uri+") extension="+extension+" timeObject="+timeObject);
            } // if
            if (timeObject!=null) {
                long time = timeObject;
                if (time>0) {
                    long expirationValue = System.currentTimeMillis() +time;
                    if (log.isDebugEnabled()) {
                        log.debug("doFilter() expirationValue="+expirationValue);
                    } // if
                    String expires = formatter.format(new Date(expirationValue));
                    if (log.isInfoEnabled()) {
                        log.info("doFilter() expires="+expires);
                    } // if
                    response.addHeader("Last-Modified", startTimeHeader);
                    response.addHeader("Etag", startTimeString+uri.hashCode()+"\"");
                    response.addHeader("Expires", expires);
                } // if
            } // if
        } // if
        chain.doFilter(req, resp);
    } // doFilter()


    public void init(FilterConfig config) throws ServletException {
        String expiry = config.getInitParameter("expirations");
        if (expiry!=null) {
            if (log.isInfoEnabled()) {
                log.info("init() expiry: "+expiry);
            } // if
            String[] expirations = expiry.split(",");
            for (String exp : expirations) {
                if (log.isInfoEnabled()) {
                    log.info("init() exp: "+exp);
                } // if
                String[] kvp = exp.split("=");
                String mimeType = kvp[0];
                String timeString = kvp[1];
                long time = Long.parseLong(timeString)*1000;
                if (log.isInfoEnabled()) {
                    log.info("init() time for "+mimeType+" is "+time);
                } // if
                extensionTimes.put(mimeType, time);
            } // for
        } // if
    } // init()

} // ExpirationHeaderFilter
