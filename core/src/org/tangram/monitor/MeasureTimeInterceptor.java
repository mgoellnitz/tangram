package org.tangram.monitor;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MeasureTimeInterceptor extends HandlerInterceptorAdapter {

    private Set<String> freeUrls;

    @Autowired
    private Statistics statistics;


    public Set<String> getFreeUrls() {
        return freeUrls;
    }


    public void setFreeUrls(Set<String> freeUrls) {
        this.freeUrls = freeUrls;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("start.time", System.currentTimeMillis());
        return super.preHandle(request, response, handler);
    } // preHandle()


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        String thisURL = request.getRequestURI();
        if ( !getFreeUrls().contains(thisURL)) {
            Long startTime = (Long)request.getAttribute("start.time");
            statistics.avg("page render time", System.currentTimeMillis()-startTime);
        } // if
        super.afterCompletion(request, response, handler, ex);
    } // afterCompletion()

} // MeasureTimeInterceptor

