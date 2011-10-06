/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.monitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;

@Controller
public class StatisticsController implements Statistics {

    public static final String STATS_URI = "/stats";

    private Map<String, Long> counter = new HashMap<String, Long>();

    private Date startTime;


    public StatisticsController() {
        startTime = new Date();
    } // StatisticsController()


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


    @RequestMapping(value = "/stats")
    public ModelAndView statistics(HttpServletRequest request, HttpServletResponse response) {
        // poor man's version of createModelAndView to be injection independent of ModelAndViewFactory which in turn might need statistics
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(Constants.THIS, this);
        model.put("request", request);
        model.put("response", response);
        return new ModelAndView(Constants.DEFAULT_VIEW, model);
    } // statistics()

} // StatisticsController
