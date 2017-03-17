package io.vandam.dbdeploy;

import io.vandam.dbdeploy.configuration.Configuration;
import io.vandam.dbdeploy.configuration.DatabaseConfig;
import io.vandam.dbdeploy.databasestructure.Database;
import io.vandam.dbdeploy.databasestructure.Table;
import io.vandam.dbdeploy.sql.IProcess;
import io.vandam.dbdeploy.sql.ProcessFactory;
import io.vandam.dbdeploy.sql.db2_400.ImportStaticData;
import io.vandam.dbdeploy.sql.static_data.StaticData;
import io.vandam.dbdeploy.utility.DirectoryListing;
import io.vandam.dbdeploy.utility.FileUtility;

import java.util.List;

class Import {
	/**
	 * Import source.
	 *
	 * @param source
	 *            the source
	 * @throws Exception
	 */
	static void importDatabase(final Configuration configuration, final String source) throws Exception {
		if (null == source) {
			System.err.println("No source defined to import from");
			return;
		}

		final DatabaseConfig config = configuration.getConfig(source);
		if (null == config) {
			System.err.println("No configuration defined for \"" + source + '"');
			return;
		}

		System.out.println("Importing from " + source + " using " + config.getDriverType() + " from \""
				+ config.getHostname() + '/' + config.getSchemaName() + '"');

		final IProcess dbProcess = ProcessFactory.getProcess(config.getDriverType());
		if (null == dbProcess) {
			return;
		}

		System.out.println("Importing Structure");

		final Database dbs = dbProcess.importDatabase(config);

		if (null == dbs) {
			System.err.println("Error importing " + source + " from \"" + config.getHostname() + '/'
					+ config.getSchemaName() + '"');
			return;
		}

		System.out.println("Writing to \"" + source + ".xml\"");
		dbs.toXml("conf/" + source + ".xml");

		final String targetDirectory = "SOURCE".equals(source) ? "sql_source" : ("sql_" + source);
		FileUtility.createDirectory(targetDirectory);

		System.out.println("Importing Stored Procedures");
		FileUtility.createDirectory(targetDirectory + "/stored_procedures/");
		dbProcess.importStoredProcedures(config, targetDirectory + "/stored_procedures/");

		System.out.println("Importing Triggers");
		FileUtility.createDirectory(targetDirectory + "/triggers/");
		dbProcess.importTriggers(config, targetDirectory + "/triggers/");

		if (!"SOURCE".equals(source)) {
			System.out.println("Importing static data");
			FileUtility.createDirectory(targetDirectory + "/static_data/");
			final List<String> fileList = DirectoryListing.getFiles("sql_source/static_data/", true);
			for (String fileName : fileList) {
				StaticData staticData = StaticData.fromXml(fileName);

				importStaticData(configuration, source, staticData.getTableName());
			}
		}
	}

	static void importStaticData(final Configuration configuration, final String source, final String tableName)
			throws Exception {
		if (null == source) {
			System.err.println("No source defined to import from");
			return;
		}

		final DatabaseConfig config = configuration.getConfig(source);
		if (null == config) {
			System.err.println("No configuration defined for \"" + source + '"');
			return;
		}

		System.out.println("Importing " + tableName + " from " + source + " using " + config.getDriverType()
				+ " from \"" + config.getHostname() + '/' + config.getSchemaName() + '"');

		final ImportStaticData importStaticData = new ImportStaticData(config);
		final String targetDirectory = "sql_" + ("SOURCE".equals(source) ? "source" : source) + "/static_data/";

		final Database database = Database.fromXml("conf/SOURCE.xml");

		Table tableConfig = null;
		for (final Table table : database.getTables()) {
			if (tableName.equals(table.getName())) {
				tableConfig = table;
			}
		}

		if (null == tableConfig) {
			System.err.println("No definition found for table " + tableName);
			return;
		}

		importStaticData.getStaticData(tableConfig, targetDirectory);
	}
}
