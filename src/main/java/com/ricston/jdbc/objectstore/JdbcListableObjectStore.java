package com.ricston.jdbc.objectstore;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.mule.api.context.MuleContextAware;
import org.mule.api.execution.ExecutionCallback;
import org.mule.api.execution.ExecutionTemplate;
import org.mule.api.store.ListableObjectStore;
import org.mule.api.store.ObjectAlreadyExistsException;
import org.mule.api.store.ObjectDoesNotExistException;
import org.mule.api.store.ObjectStoreException;
import org.mule.api.transaction.TransactionConfig;
import org.mule.config.i18n.CoreMessages;
import org.mule.execution.TransactionalExecutionTemplate;
import org.mule.transport.jdbc.JdbcConnector;
import org.mule.util.store.AbstractMonitoredObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcListableObjectStore<T extends Serializable> extends AbstractMonitoredObjectStore<T> implements MuleContextAware,
		ListableObjectStore<T> {

	private static final Logger LOG = LoggerFactory.getLogger(JdbcListableObjectStore.class);

	protected JdbcConnector jdbcConnector;
	protected TransactionConfig transactionConfig;
	protected String insertQueryKey;
	protected String selectQueryKey;
	protected String deleteQueryKey;
	protected String clearQueryKey;
	protected String allKeysQueryKey;

	protected ArrayHandler arrayHandler;
	protected ColumnListHandler listHandler;

	public JdbcListableObjectStore() {
		this.arrayHandler = new ArrayHandler();
		this.listHandler = new ColumnListHandler();
		this.maxEntries = -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPersistent() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void expire() {
		// DO NOTHING
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Serializable key) throws ObjectStoreException {
		this.notNullKey(key);
		Object[] result = (Object[]) this.query(this.getSelectQuery(), this.arrayHandler, key);
		return result != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T remove(Serializable key) throws ObjectStoreException {
		this.notNullKey(key);
		T value = this.retrieve(key);
		this.update(this.getDeleteQuery(), key);
		return value;
	}

	@Override
	public void clear() throws ObjectStoreException {
		this.update(this.getClearQuery(), new Object[] {});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T retrieve(Serializable key) throws ObjectStoreException {
		Object[] row = (Object[]) this.query(this.getSelectQuery(), this.arrayHandler, key);
		if (row == null) {
			throw new ObjectDoesNotExistException(CoreMessages.objectNotFound(key));
		} else {
			return (T) row[1];
		}
	}

	public void store(Serializable key, T value, String[] parameters) throws ObjectStoreException {
		Object[] arguments = new Object[2 + parameters.length];
		arguments[0] = key;
		arguments[1] = value;

		for (int i = 0; i < parameters.length; i++) {
			String parameter = parameters[i];
			arguments[2 + i] = parameter;
		}

		this.update(this.getInsertQuery(), arguments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(Serializable key, T value) throws ObjectStoreException {
		this.notNullKey(key);
		try {
			this.update(this.getInsertQuery(), key, value);
		} catch (ObjectStoreException e) {
			throw new ObjectAlreadyExistsException(e);
		}
	}

	/**
	 * Validates that the key is not null
	 * 
	 * @param key
	 * @throws ObjectStoreException
	 */
	private void notNullKey(Serializable key) throws ObjectStoreException {
		if (key == null) {
			throw new ObjectStoreException(CoreMessages.objectIsNull("id"));
		}
	}

	/**
	 * Executes the query in sql using the current transaction config
	 * 
	 * @param sql
	 * @param handler
	 * @param arguments
	 * @return
	 * @throws ObjectStoreException
	 */
	private Object query(final String sql, final ResultSetHandler handler, final Object... arguments) throws ObjectStoreException {
		try {
			ExecutionCallback<Object> processingCallback = new ExecutionCallback<Object>() {
				public Object process() throws Exception {
					return jdbcConnector.getQueryRunner().query(sql, handler, arguments);
				}
			};
			return executeInTransactionTemplate(processingCallback);
		} catch (SQLException e) {
			throw new ObjectStoreException(e);
		} catch (Exception e) {
			throw new ObjectStoreException(e);
		}
	}

	/**
	 * Executes an update query using the current transaction config
	 * 
	 * @param sql
	 * @param arguments
	 * @return
	 * @throws ObjectStoreException
	 */
	private Object update(final String sql, final Object... arguments) throws ObjectStoreException {
		try {
			ExecutionCallback<Object> processingCallback = new ExecutionCallback<Object>() {
				public Object process() throws Exception {
					return jdbcConnector.getQueryRunner().update(sql, arguments);
				}
			};
			return executeInTransactionTemplate(processingCallback);
		} catch (SQLException e) {
			throw new ObjectStoreException(e);
		} catch (Exception e) {
			throw new ObjectStoreException(e);
		}
	}

	private Object executeInTransactionTemplate(ExecutionCallback<Object> processingCallback) throws Exception {
		ExecutionTemplate<Object> executionTemplate = TransactionalExecutionTemplate.createTransactionalExecutionTemplate(getMuleContext(),
				this.transactionConfig);
		return executionTemplate.execute(processingCallback);
	}

	public JdbcConnector getJdbcConnector() {
		return jdbcConnector;
	}

	public void setJdbcConnector(JdbcConnector jdbcConnector) {
		this.jdbcConnector = jdbcConnector;
	}

	public TransactionConfig getTransactionConfig() {
		return transactionConfig;
	}

	public void setTransactionConfig(TransactionConfig transactionConfig) {
		this.transactionConfig = transactionConfig;
	}

	public String getInsertQuery() {
		return (String) this.jdbcConnector.getQueries().get(this.insertQueryKey);
	}

	public String getSelectQuery() {
		return (String) this.jdbcConnector.getQueries().get(this.selectQueryKey);
	}

	public String getAllKeysQuery() {
		return (String) this.jdbcConnector.getQueries().get(this.allKeysQueryKey);
	}

	public String getDeleteQuery() {
		return (String) this.jdbcConnector.getQueries().get(this.deleteQueryKey);
	}

	public String getClearQuery() {
		return (String) this.jdbcConnector.getQueries().get(this.clearQueryKey);
	}

	public String getInsertQueryKey() {
		return insertQueryKey;
	}

	public void setInsertQueryKey(String insertQueryKey) {
		this.insertQueryKey = insertQueryKey;
	}

	public String getSelectQueryKey() {
		return selectQueryKey;
	}

	public void setSelectQueryKey(String selectQueryKey) {
		this.selectQueryKey = selectQueryKey;
	}

	public String getDeleteQueryKey() {
		return deleteQueryKey;
	}

	public void setDeleteQueryKey(String deleteQueryKey) {
		this.deleteQueryKey = deleteQueryKey;
	}

	public String getClearQueryKey() {
		return clearQueryKey;
	}

	public void setClearQueryKey(String clearQueryKey) {
		this.clearQueryKey = clearQueryKey;
	}

	public String getAllKeysQueryKey() {
		return allKeysQueryKey;
	}

	public void setAllKeysQueryKey(String allKeysQueryKey) {
		this.allKeysQueryKey = allKeysQueryKey;
	}

	@Override
	public void open() throws ObjectStoreException {
		LOG.debug("BinaryDataJdbcObjectStore does not take care of opening the JDBC connection");
	}

	@Override
	public void close() throws ObjectStoreException {
		LOG.debug("BinaryDataJdbcObjectStore does not take care of closing the JDBC connection");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Serializable> allKeys() throws ObjectStoreException {
		String allKeysQuery = this.getAllKeysQuery();
		List<String> result = (List<String>) this.query(allKeysQuery, this.listHandler);
        return new ArrayList<Serializable>(result);
	}

}
