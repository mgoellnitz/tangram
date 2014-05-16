/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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
package org.tangram.view;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static final DateFormat HTTP_HEADER_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");


    {
        HTTP_HEADER_DATE_FORMAT.setTimeZone(GMT);
    }

    private static String uriPrefix = null;


    /**
     * Take the primary browser language from the request as input for JST fmt tag library.
     *
     * Only consideres the first language in the list taken from the clients reuqest.
     *
     * @param request request to read accept-language header from and set request attribute to.
     */
    public static void setPrimaryBrowserLanguageForJstl(HttpServletRequest request) {
        String acceptLanguageHeader = request.getHeader("Accept-Language");
        String[] acceptLanguages = acceptLanguageHeader.split(",");
        if (acceptLanguages.length>0) {
            String[] acceptLanguage = acceptLanguages[0].split(";");
            if (acceptLanguage.length>0) {
                String language[] = acceptLanguage[0].split("-");
                if (language.length>0) {
                    final String localeCode = language[0];
                    if (log.isInfoEnabled()) {
                        log.info("setPrimaryBrowserLanguageForJstl() setting request language "+localeCode);
                    } // if
                    Config.set(request, Config.FMT_LOCALE, new Locale(localeCode));
                } // if
            } // if
        } // if
    } // setPrimaryBrowserLanguageForJstl()


    /**
     * transform a title into a URL conform UTF-8 encoded string
     *
     * @param title
     * @return the URL form of the title
     * @throws UnsupportedEncodingException
     */
    public static String urlize(String title) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(title)) {
            return "-";
        } // if
        String result = title.toLowerCase();
        result = result.replace(" - ", "-");
        result = result.replace("ä", "ae");
        result = result.replace("ö", "oe");
        result = result.replace("ü", "ue");
        result = result.replace("ß", "ss");
        char[] specials = {',', ' ', ':', ';', '"', '?', '!', '*'};
        for (char c : specials) {
            result = result.replace(c, '-');
        } // for
        return URLEncoder.encode(result, "UTF-8");
    } // urlize()


    public static String getUriPrefix(HttpServletRequest request) {
        if (uriPrefix==null) {
            String contextPath = request.getContextPath();
            uriPrefix = (contextPath.length()==1) ? "" : contextPath;
        } // if
        return uriPrefix;
    } // getUriPrefix()


    /**
     * just to protect this stuff from being instantiated
     */
    private Utils() {
    } // Utils()

} // Utils
