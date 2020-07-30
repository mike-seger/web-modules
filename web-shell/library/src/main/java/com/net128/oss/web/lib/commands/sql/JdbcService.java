package com.net128.oss.web.lib.commands.sql;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.net128.oss.web.lib.util.TabUtils;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptStatementFailedException;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.net128.oss.web.lib.commands.sql.JdbcService.OutputFormat.*;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

@Service
@Slf4j
@SuppressWarnings("unused")
public class JdbcService {
    public JdbcService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    final private DataSource dataSource;

    public enum OutputFormat {fixedwb, json, tsv, csv, fixedw}
    public OutputFormat defaultOutputFormat = fixedwb;

    // Some databases are case-sensitive when querying metadata.
    // These maps map the lower case identifiers to their native case.
    final private Map<String, String> tableMap = new HashMap<>();
    private Map<String, String> schemaMap;

    public String executeSqlFormatted(final String sqlString) {
        return executeSqlFormatted(sqlString, defaultOutputFormat);
    }

    public String executeSqlFormatted(final String sqlString, OutputFormat outputFormat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (outputFormat == null) {
            outputFormat = fixedwb;
        }
        int n = executeSqlFormatted(sqlString, baos,
            fixedw.equals(outputFormat) ? tsv :
            fixedwb.equals(outputFormat) ? tsv : outputFormat
        );
        String result = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        if (n>=0 && (fixedw.equals(outputFormat) || fixedwb.equals(outputFormat))) {
            result = String.format("%s\n%s.",
                TabUtils.formatFixedWidthColumnsWithBorders(result,
                    !outputFormat.equals(fixedw)), resultSizeInfo("row", n));
        }
        return result;
    }

    public int executeSqlFormatted(String sql, OutputStream os, OutputFormat outputFormat) {
        sql = sql.trim().replaceAll(";$", "");
        try (Connection connection = dataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            if (json.equals(outputFormat)) {
                StreamingJsonResultSetExtractor se=new StreamingJsonResultSetExtractor(os);
                se.extractData(rs);
                return se.nResults;
            } else {
                boolean tabDelimited = tsv.equals(outputFormat);
                StreamingCsvResultSetExtractor se=new StreamingCsvResultSetExtractor(os, tabDelimited);
                se.extractData(rs);
                return se.nResults;
            }
        } catch (Exception e) {
            outputError(os, String.format("Error: %s while executing: %s ...",
                e.getMessage(), sql.substring(0, Math.min(100, sql.length()))));
            return -1;
        }
    }

    private void outputError(OutputStream os, String message) {
        try {
            os.write(ansi().fg(RED).a(message.replace("]","")
                .replace("[","")).reset().toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<List<String>> executeSql(String sql) throws SQLException {
        sql = sql.trim().replaceAll(";$", "");
        try (Connection connection = dataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery(sql);
            List<List<String>> result = new ArrayList<>();
            if(rs.next()) {
                ResultSetMetaData rsm=rs.getMetaData();
                int n=rsm.getColumnCount();
                List<String> columnNames=new ArrayList<>();
                for(int i=0;i<n; i++) {
                    columnNames.add(rsm.getColumnName(i));
                }
                result.add(columnNames);
                do {
                    List<String> row=new ArrayList<>();
                    for(int i=0;i<n; i++) {
                        row.add(rs.getString(i));
                    }
                    result.add(row);
                } while (rs.next());
            }
            return result;
        }
    }

    public String updateSqlVerbose(final String sqlString) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return updateSql(sqlString, baos) ? "OK" :
            String.format("Error occurred: %s",
                new String(baos.toByteArray(), StandardCharsets.UTF_8));
    }

    public boolean updateSql(final String sqlString) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return updateSql(sqlString, baos);
    }

    boolean updateSql(String sql, OutputStream os) {
        PrintStream pos = new PrintStream(os, true);
        try (InputStream is = new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8.name()))) {
            Resource resource = new InputStreamResource(is);
            ResourceDatabasePopulator databasePopulator =
                new ResourceDatabasePopulator(false,
                        true, StandardCharsets.UTF_8.name(), resource);
            databasePopulator.execute(dataSource);
            pos.println("Success\n<OK>");
            return true;
        } catch (Exception e) {
            outputError(pos, String.format("Error: %s while executing: %s ...\n",
                e.getMessage(), sql.substring(0, Math.min(100, sql.length()))));
            handleSqlExecutionException(e, sql, pos);
            return false;
        }
    }

    String driverInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTypeInfo()) {
                resultSet.next();
            }
            return TabUtils.formatFixedWidthColumnsWithBorders(new String[][]{
                {"Attribute", "Value"},
                {"URL", metaData.getURL()},
                {"User", metaData.getUserName()},
                {"Schema", connection.getSchema()},
                {"Product", metaData.getDatabaseProductName()},
                {"Version", noBreak(metaData.getDatabaseProductVersion())}
            }, true);
        } catch (Exception e) {
            throw rte(e);
        }
    }

    public String schemasFormatted() {
        Collection<String> schemas = schemas();
        List<List<String>> tableContent = new ArrayList<>();
        tableContent.add(Collections.singletonList(noBreak("SCHEMA NAME")));
        tableContent.addAll(schemas.stream().sorted().map(Collections::singletonList).collect(Collectors.toList()));
        String formattedTable = "";
        if (schemas.size() > 0) {
            formattedTable = TabUtils.formatFixedWidthColumnsWithBorders(tableContent, true, 100);
        }
        return (formattedTable + String.join("", "\n",
                resultSizeInfo("schema", schemas.size()), ".")).trim();
    }

    public Collection<String> schemas() {
        try (Connection connection = dataSource.getConnection()) {
            return schemasInternal(connection);
        } catch (Exception e) {
            throw rte(e);
        }
    }

    public Collection<String> schemasInternal(Connection connection) {
        try {
            if (schemaMap != null) {
                return schemaMap.values();
            }
            List<String> result = new ArrayList<>();
            try (ResultSet rs = connection.getMetaData().getSchemas()) {
                while (rs.next()) {
                    result.add(rs.getString(1).trim());
                }
            }
            schemaMap = mapLowerCase(result, "");
            return result;
        } catch (Exception e) {
            throw rte(e);
        }
    }

    private Map<String, String> mapLowerCase(Collection<String> strings, final String prefix) {
        return strings.stream().collect(
                Collectors.toMap(s -> (prefix + s).toLowerCase(), s -> s));
    }

    private String resolveSchema(Connection connection, String schema) {
        if (schema == null) return null;
        schemasInternal(connection);
        return schemaMap.get(schema.toLowerCase());
    }

    public String tablesFormatted(String schema) {
        Collection<String> tables = tables(schema);
        List<List<String>> tableContent = new ArrayList<>();
        tableContent.add(Collections.singletonList(noBreak("TABLE NAME")));
        tableContent.addAll(tables.stream().sorted().map(Collections::singletonList).collect(Collectors.toList()));
        String formattedTable = "";
        if (tables.size() > 0) {
            formattedTable = TabUtils.formatFixedWidthColumnsWithBorders(tableContent, true, 100);
        }
        return (formattedTable + String.join("", "\n",
            resultSizeInfo("table", tables.size()) + " in ", schema != null ? "in schema " + schema : "default schema", ".")).trim();
    }

    public Collection<String> tables(String schema) {
        try (Connection connection = dataSource.getConnection()) {
            return tablesInternal(connection, schema);
        } catch (Exception e) {
            throw rte(e);
        }
    }

    public Collection<String> tablesInternal(Connection connection, final String schema) {
        try {
            final String selectedSchema = resolveSchema(connection,
                schema == null ? connection.getSchema() : schema);
            List<String> result = new ArrayList<>();
            try (ResultSet rs = connection.getMetaData().getTables(
                    null, selectedSchema, null, null)) {
                while (rs.next()) {
                    result.add(rs.getString("TABLE_NAME"));
                }
            }
            Collections.sort(result);
            tableMap.putAll(mapLowerCase(result, schema + "."));
            return result;
        } catch (Exception e) {
            throw rte(e);
        }
    }

    public String tableInfoFormatted(String tableName) {
        return tableInfoFormatted(null, tableName);
    }

    public String tableInfoFormatted(String schemaName, String tableName) {
        return TabUtils.formatFixedWidthColumnsWithBorders(
                tableInfo(schemaName, tableName), true, 100);
    }

    public List<List<String>> tableInfo(String tableName) {
        return tableInfo(null, tableName);
    }

    public List<List<String>> tableInfo(String schemaName, String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            schemaName = resolveSchema(connection, schemaName);
            tableName = resolveTable(connection, schemaName, tableName);
            List<List<String>> columnDefinitions = new ArrayList<>();
            columnDefinitions.add(Arrays.asList(null, null, tableName));
            columnDefinitions.add(Arrays.asList("COLUMN", noBreak("DATA TYPE      "), noBreak("NOT NULL")));
            try (ResultSet columns = connection.getMetaData().getColumns(
                    null, schemaName, tableName, null)) {
                while (columns.next()) {
                    columnDefinitions.add(Arrays.asList(
                            columns.getString("COLUMN_NAME"),
                            columns.getString("TYPE_NAME")
                                    + " (" + columns.getInt("COLUMN_SIZE") + ")",
                            columns.getString("IS_NULLABLE").replace("YES", "").replace("NO", "Y")
                    ));
                }
            }
            return columnDefinitions;
        } catch (Exception e) {
            throw rte(e);
        }
    }

    private String resolveTable(Connection connection, String schema, String table) {
        String result = tableMap.get((schema + "." + table).toLowerCase());
        if (result != null) return result;
        tablesInternal(connection, schema);
        return tableMap.get((schema + "." + table).toLowerCase());
    }

    private RuntimeException rte(Exception e) {
        return new RuntimeException(String.format("Error: %s", e.getMessage()), e);
    }

    private void handleSqlExecutionException(Exception e, String sql, PrintStream pos) {
        String message = "Failed to execute: " + sql;
        if (e.getClass().getPackage().getName().startsWith("org.spring") && !(e instanceof ScriptStatementFailedException)) {
            log.error(message, e);
            outputError(pos, String.format("Error executing:\n\t\t%s - %s", sql.trim(), e.getMessage()));
        } else if (log.isDebugEnabled() || (!(e instanceof SQLSyntaxErrorException) &&
                !e.getClass().getName().contains("OracleDatabaseException"))) {
            log.error("{}", sql, e);
        } else {
            log.error("{}: {}", message, e.getMessage());
        }
        if (e instanceof SQLException || e instanceof ScriptStatementFailedException) {
            outputError(pos, e.getMessage()
                    .replaceAll("[\t\n]", " ")
                    .replaceAll("[ ]+", " ").trim());
        }
    }

    private static class StreamingCsvResultSetExtractor {
        private final OutputStream os;
        private final boolean tabDelimited;
        private int nResults=-1;

        StreamingCsvResultSetExtractor(OutputStream os, boolean tabDelimited) {
            this.os = os;
            this.tabDelimited = tabDelimited;
        }

        void extractData(final ResultSet rs) throws SQLException, IOException {
            char separator = tabDelimited ? '\t' : CSVWriter.DEFAULT_SEPARATOR;
            try (OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                try (CSVWriter writer = new CSVWriter(osw, separator,
                        CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END) {
                    @Override
                    protected void processCharacter(Appendable appendable, char nextChar) throws IOException {
                        if (nextChar == '\t') {
                            appendable.append('\\').append('t');
                        } else if (nextChar == '\n') {
                            appendable.append('\\').append('n');
                        } else {
                            super.processCharacter(appendable, nextChar);
                        }
                    }}) {
                    nResults = writer.writeAll(rs, true)-1;
                }
            }
        }
    }

    private static class StreamingJsonResultSetExtractor implements ResultSetExtractor<Void> {
        private final OutputStream os;
        private int nResults;

        StreamingJsonResultSetExtractor(OutputStream os) {
            this.os = os;
        }

        @Override
        public Void extractData(@Nonnull ResultSet rs) throws SQLException {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            try (JsonGenerator jg =
                    objectMapper.getFactory().createGenerator(os, JsonEncoding.UTF8)) {
                writeResultSetToJson(rs, jg);
            } catch (IOException e) {
                throw new AbortedException(e.getMessage(), e);
            }
            return null;
        }

        private void writeResultSetToJson(final ResultSet rs, final JsonGenerator jg)
                throws SQLException, IOException {
            final ResultSetMetaData rsmd = rs.getMetaData();
            final int columnCount = rsmd.getColumnCount();
            jg.writeStartArray();
            while (rs.next()) {
                nResults++;
                jg.writeStartObject();
                for (int i = 1; i <= columnCount; i++) {
                    jg.writeObjectField(rsmd.getColumnName(i), rs.getObject(i));
                }
                jg.writeEndObject();
                jg.flush();
            }
            jg.writeEndArray();
        }
    }

    private static class AbortedException extends RuntimeException {
        AbortedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private String resultSizeInfo(String name, int size) {
        return size + " " + name + (size != 1 ? "s" : "") + " found";
    }

    private String noBreak(String s) {
        return s.replace(" ", "\u2007");
    }
}