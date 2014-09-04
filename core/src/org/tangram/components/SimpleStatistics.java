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
import org.tangram.monitor.Statistics;


/**
 * Simple event counter and average caculation implementation.
 */
@Named("statistics")
@Singleton
public class SimpleStatistics implements Statistics {

    private final Map<String, Long> counter = new HashMap<>();

    private final Date startTime;


    public SimpleStatistics() {
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

} // SimpleStatistics
