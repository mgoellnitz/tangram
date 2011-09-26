package org.tangram.solution.protection;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tangram.content.Content;

public interface Protection extends ProtectedContent {

    public String getProtectionKey();


    public List<Content> getProtectedContents();


    public String handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception;


    public boolean isContentVisible(HttpServletRequest request) throws Exception;


    public boolean needsAuthorization(HttpServletRequest request);

} // Protection
