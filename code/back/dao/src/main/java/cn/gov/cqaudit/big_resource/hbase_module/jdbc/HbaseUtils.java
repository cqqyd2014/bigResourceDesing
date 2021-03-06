package cn.gov.cqaudit.big_resource.hbase_module.jdbc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.dao.DataAccessException;

import org.springframework.util.StringUtils;

public class HbaseUtils {

	/**
	 * Converts the given (Hbase) exception to an appropriate exception from <tt>org.springframework.dao</tt> hierarchy.
	 * 
	 * @param ex Hbase exception that occurred
	 * @return the corresponding DataAccessException instance
	 */
	public static DataAccessException convertHbaseException(Exception ex) {
		return new HbaseSystemException(ex);
	}

	/**
	 * Retrieves an Hbase table instance identified by its name.
	 * 
	 * @param configuration Hbase configuration object
	 * @param tableName table name
	 * @return table instance
	 */
	public static Table getHTable(String tableName) {
		return getHTable(tableName, getCharset(null), null);
	}

	/**
	 * Retrieves an Hbase table instance identified by its name and charset using the given table factory.
	 * 
	 * @param tableName table name
	 * @param configuration Hbase configuration object
	 * @param charset name charset (may be null)
	 * @param tableFactory table factory (may be null)
	 * @return table instance
	 */
	public static Table getHTable(String tableName, Charset charset, Connection tableFactory) {
		if (HbaseSynchronizationManager.hasResource(tableName)) {
			return (HTable) HbaseSynchronizationManager.getResource(tableName);
		}

		Table t = null;
		try {
			if (tableFactory != null) {
				//t = tableFactory.createHTableInterface(configuration, tableName.getBytes(charset));
				t = tableFactory.getTable(TableName.valueOf(tableName));
			}
			else {
				//t = new HTable(configuration, tableName.getBytes(charset));
				System.out.println("HBase Connection is null");
			}

			return t;

		} catch (Exception ex) {
			throw convertHbaseException(ex);
		}
	}

	static Charset getCharset(String encoding) {
		return (StringUtils.hasText(encoding) ? Charset.forName(encoding) : Charset.forName("UTF-8"));
	}

	/**
	 * Releases (or closes) the given table, created via the given configuration if it is not managed externally (or bound to the thread).
	 * 
	 * @param tableName table name
	 * @param table table
	 */
	public static void releaseTable(String tableName, Table table) {
		releaseTable(tableName, table, null);
	}
	public static void releaseMutator(String tableName,BufferedMutator mutator) {
		releaseMutator(tableName, mutator, null);
		
	}
	public static void releaseMutator(String tableName, BufferedMutator mutator, Connection tableFactory) {
		try {
			doReleaseMutator(tableName, mutator, tableFactory);
		} catch (IOException ex) {
			throw HbaseUtils.convertHbaseException(ex);
		}
	}
	private static void doReleaseMutator(String tableName, BufferedMutator mutator, Connection tableFactory)
			throws IOException {
		if (mutator == null) {
			return;
		}

		// close only if its unbound 
		if (!isBoundToThread(tableName)) {
			if (tableFactory != null) {
				//tableFactory.releaseHTableInterface(table);
				mutator.close();
			}
			else {
				//table.close();
			}
		}
	}

	/**
	 * Releases (or closes) the given table, created via the given configuration if it is not managed externally (or bound to the thread).
	 * 
	 * @param tableName table name
	 * @param table table
	 * @param tableFactory table factory
	 */
	public static void releaseTable(String tableName, Table table, Connection tableFactory) {
		try {
			doReleaseTable(tableName, table, tableFactory);
		} catch (IOException ex) {
			throw HbaseUtils.convertHbaseException(ex);
		}
	}

	private static void doReleaseTable(String tableName, Table table, Connection tableFactory)
			throws IOException {
		if (table == null) {
			return;
		}

		// close only if its unbound 
		if (!isBoundToThread(tableName)) {
			if (tableFactory != null) {
				//tableFactory.releaseHTableInterface(table);
				table.close();
			}
			else {
				//table.close();
			}
		}
	}

	private static boolean isBoundToThread(String tableName) {
		return HbaseSynchronizationManager.hasResource(tableName);
	}
}