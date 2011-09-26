package org.tangram.gae;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.tangram.jdo.JdoContent;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE, customStrategy = "complete-table")
public abstract class GaeContent extends JdoContent {

    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @PrimaryKey
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    private String id;

} // GaeContent
