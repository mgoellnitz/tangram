/**
 *
 * Copyright 2011-2013 Martin Goellnitz
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
package org.tangram.components;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.monitor.Statistics;
import org.tangram.view.TargetDescriptor;


/**
 * All in one implementation of the statistics interface together with its own controller to trigger the view.
 * This implementaton still is a simple spring framework @Controller so that we don't run into circular dependencies
 * when collecting statistics for the lower parts of the system and later try to present them with the higher
 * level object oriented templating presentd by those parts.s
 */
@LinkHandler
@Named
@Singleton
public class StatisticsHandler implements Statistics {

    public static final String STATS_URI = "/stats";

    private Map<String, Long> counter = new HashMap<String, Long>();

    private Date startTime;


    public StatisticsHandler() {
        startTime = new Date();
    } // StatisticsHandler()


    public Map<String, Long> getCounter() {
        return counter;
    } // getCounter()


    public Date getStartTime() {
        return startTime;
    } // getStartTime()


    @Override
    public void increase(String eventIdentifier) {
        long value = counter.containsKey(eventIdentifier) ? counter.get(eventIdentifier)+1 : 1;
        counter.put(eventIdentifier, value);
    } // increase(()


    @Override
    public void avg(String eventIdentifier, long value) {
        String countKey = eventIdentifier+" count";
        increase(countKey);
        long median = (counter.containsKey(eventIdentifier) ? counter.get(eventIdentifier) : 0);
        long count = counter.get(countKey);
        counter.put(eventIdentifier, (median*(count-1)+value)/count);
    } // avg()


    @LinkAction("/stats")
    public TargetDescriptor statistics(HttpServletRequest request, HttpServletResponse response) {
        return new TargetDescriptor(this, null, null);
    } // statistics()

} // StatisticsHandler
