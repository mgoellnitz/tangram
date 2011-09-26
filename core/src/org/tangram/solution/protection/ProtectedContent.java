package org.tangram.solution.protection;

import java.util.List;

import org.tangram.content.Content;

public interface ProtectedContent extends Content {

    List<? extends Content> getProtectionPath();
    
} // ProtectedContent()
