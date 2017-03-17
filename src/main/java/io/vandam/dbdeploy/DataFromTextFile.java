package io.vandam.dbdeploy;

import io.vandam.dbdeploy.sql.static_data.Column;
import io.vandam.dbdeploy.sql.static_data.Record;
import io.vandam.dbdeploy.sql.static_data.StaticData;
import io.vandam.dbdeploy.utility.FileUtility;

import java.util.Arrays;
import java.util.List;

class DataFromTextFile {
    private static final String TAB = "\t";

    static void importDataFromTxtFile(final String tableName, final String filename) throws Exception {
        if (!FileUtility.fileExists(filename)) {
            System.out.println("Cannot find file " + filename);
            return;
        }

        final List<String> fileContent = FileUtility.readFileLines(filename);

        if (fileContent.isEmpty()) {
            System.out.println("No data read from " + filename);
            return;
        }

        final List<String> columns = Arrays.asList(fileContent.get(0).split(TAB));
        System.out.println("Found headers: " + columns);

        final StaticData staticData = new StaticData();
        staticData.setTableName(tableName);

        for (int i = 1; i < fileContent.size(); i++) {
            System.out.println("Reading record #" + i);
            final List<String> values = Arrays.asList(fileContent.get(i).split(TAB));

            final Record record = new Record();

            for (int j = 0; j < columns.size(); j++) {
                record.getColumns().add(new Column(columns.get(j), values.get(j).replace("\"", "").trim()));
            }

            staticData.getRecords().add(record);
        }

        System.out.println("Writing to file " + tableName + ".xml");
        staticData.toXml("sql_source/static_data/" + tableName + ".xml");

    }
}
