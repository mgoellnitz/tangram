/**
 *
 * Copyright 2013-2015 Martin Goellnitz
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Set some static but configurable expiration times in HTTP headers.
 *
 * Quite simple but in many situations usable servlet filter to set some reasonable, cache-friendly and thus
 * user friendly default expiration headers, which might of course subsequently be overridden in the response.
 * Since we can't set header after we come back in the chain and content type is only known after that call,
 * we use extensions here and map them to times.
 *
 * The filter takes an init parameter "expiration" which is a comma separated list of "extension=seconds" pairs
 * mapping extensions to caching times in seconds. Exensons don't include the dot in this case. All times have
 * to be issued in seconds.
 *
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;expiryFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;org.tangram.util.ExpirationHeaderFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *      &lt;param-name&gt;expirations&lt;/param-name&gt;
 *      &lt;param-value&gt;css=604800,js=604800,gif=604800,ico=604800,html=0,DEFAULT=86400&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *  &lt;/filter&gt;
 *  &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;expiryFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *  &lt;/filter-mapping&gt;
 *
 */
public class ExpirationHeaderFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ExpirationHeaderFilter.class);

    private Map<String, Long> extensionTimes = new HashMap<>();

    private final DateFormat formatter;

    private final String startTimeString;

    private final String startTimeHeader;


    public ExpirationHeaderFilter() {
        formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        final long startTime = System.currentTimeMillis();
        startTimeString = "\""+startTime+"-";
        startTimeHeader = formatter.format(new Date(startTime));
    } // ExpirationHeaderFilter()


    @Override
    public void destroy() {
        extensionTimes = new HashMap<>();
    } // destroy()


    public void addExpirationTime(String extension, long time) {
        extensionTimes.put(extension, time);
    } // addExpirationTime()


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


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("X-Tangram", "ExpirationFilter");
        String uri = ((HttpServletRequest) req).getRequestURI();
        int idx = uri.lastIndexOf('.');
        if (idx>0) {
            String extension = uri.substring(idx+1);
            Long timeObject = getTimeObject(extension);
            LOG.debug("doFilter({}) extension={} timeObject={}", uri, extension, timeObject);
            if (timeObject!=null) {
                long time = timeObject;
                if (time>0) {
                    long expirationValue = System.currentTimeMillis()+(time*1000);
                    LOG.debug("doFilter() expirationValue=", expirationValue);
                    String expires = formatter.format(new Date(expirationValue));
                    LOG.debug("doFilter() expires={}", expires);
                    response.addHeader("Cache-Control", "max-age="+time);
                    response.addHeader("Etag", startTimeString+uri.hashCode()+"\"");
                    response.addHeader("Expires", expires);
                    response.addHeader("Last-Modified", startTimeHeader);
                } // if
            } // if
        } // if
        chain.doFilter(req, resp);
    } // doFilter()


    @Override
    public void init(FilterConfig config) throws ServletException {
        String expiry = config.getInitParameter("expirations");
        if (expiry!=null) {
            LOG.info("init() expirations: {}", expiry);
            for (String exp : expiry.split(",")) {
                LOG.debug("init() exp: {}", exp);
                exp = exp.trim();
                String[] kvp = exp.split("=");
                String extension = kvp[0];
                String timeString = kvp[1];
                long time = Long.parseLong(timeString);
                LOG.info("init() time for {} is {}", extension, time);
                addExpirationTime(extension, time);
            } // for
        } // if
    } // init()

} // ExpirationHeaderFilter
