package org.tangram.jdo;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.NotPersistent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public abstract class JdoContent implements Content {

	private static final Log log = LogFactory.getLog(JdoContent.class);

	@NotPersistent
	protected PersistenceManager manager;

	@NotPersistent
	protected BeanFactory beanFactory;

	@NotPersistent
	private String id;

	@Override
	public String getId() {
		if (id == null) {
			id = ((JdoBeanFactory) beanFactory).postprocessPlainId(JDOHelper
					.getObjectId(this));
		} // if
		return id;
	} // getId()

	public void setManager(PersistenceManager manager) {
		this.manager = manager;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof JdoContent) ? getId().equals(
				((JdoContent) obj).getId()) : super.equals(obj);
	} // equals()

	protected List<String> getIds(List<? extends Content> contents) {
		List<String> result = new ArrayList<String>();
		for (Object o : contents) {
			result.add(((JdoContent) o).getId());
		} // for
		return result;
	} // getIds()

	protected <T extends Content> T getContent(Class<T> c, String id) {
		if (log.isDebugEnabled()) {
			log.debug("getContent() id=" + id + " beanFactory=" + beanFactory);
		} // if
		return (id == null) ? null : beanFactory.getBean(c, id);
	} // getContent()

	protected <T extends Content> List<T> getContents(Class<T> c,
			List<String> ids) {
		List<T> result = new ArrayList<T>();
		if (ids != null) {
			for (String id : ids) {
				T content = getContent(c, id);
				if (content != null) {
					result.add(content);
				} // if
			} // for
		} // if
		return result;
	} // getContents()

	@Override
	public boolean persist() {
		boolean result = true;
		try {
			manager.makePersistent(this);
			manager.currentTransaction().commit();
			((JdoBeanFactory) beanFactory).clearCacheFor(this.getClass());
		} catch (Exception e) {
			log.error("persist()", e);
			manager.currentTransaction().rollback();
			result = false;
		} // try/catch/finally
		return result;
	} // persist()

	@Override
	public int compareTo(Content c) {
		return getId().compareTo(c.getId());
	} // compareTo()

} // JdoContent
