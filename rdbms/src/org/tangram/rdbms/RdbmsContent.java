package org.tangram.rdbms;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.tangram.jdo.JdoContent;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE, customStrategy="complete-table")
public abstract class RdbmsContent extends JdoContent {

} // RdbmsContent
