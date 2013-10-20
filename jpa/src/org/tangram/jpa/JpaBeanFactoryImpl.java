/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tangram.jpa;

import org.tangram.content.Content;

/**
 *
 */
public class JpaBeanFactoryImpl extends AbstractJpaBeanFactory {

    @Override
    protected Object getPrimaryKey(String internalId, Class<? extends Content> kindClass) {
        return internalId;
    } // getPrimaryKey()
    
} // JpaBeanFactoryImpl
