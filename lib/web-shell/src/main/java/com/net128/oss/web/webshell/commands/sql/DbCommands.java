package com.net128.oss.web.webshell.commands.sql;

import com.net128.oss.web.webshell.commands.RawCommand;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static com.net128.oss.web.webshell.commands.sql.JdbcService.OutputFormat;

@Service
public class DbCommands {
    public DbCommands(JdbcService jdbcService) {
        this.jdbcService = jdbcService;
    }

    private final JdbcService jdbcService;

    public enum SqlInfo {driver, tables, table, schemas}

	@Command(name = "db", description = "Show metadata information.", mixinStandardHelpOptions = true)
    public String show(
            @Option(names = {"-s", "--schema",}, description = "Use the specified schema.", arity = "1") String schema,
            @Option(names = {"-t", "--table"}, description = "Use the specified table.", arity = "1") String table,
            @Option(names = {"-o", "--output"}, description = "Set or get the default output format for SELECT. Valid values: [${COMPLETION-CANDIDATES}]", arity = "0..1") OutputFormat outputFormat,
            @Parameters(arity = "0..1", description = "The detail to show. Valid values: [${COMPLETION-CANDIDATES}]") SqlInfo detail
    ) {
        try {
            if (detail != null)
                switch (detail) {
                    case driver:
                        return jdbcService.driverInfo();
                    case schemas:
                        return jdbcService.schemasFormatted();
                    case table:
                        return jdbcService.tableInfoFormatted(schema, table);
                    default:
                    case tables:
                        return jdbcService.tablesFormatted(schema);
                }
            if (outputFormat != null) {
                jdbcService.defaultOutputFormat = outputFormat;
            }
            return "output type: " + jdbcService.defaultOutputFormat;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @RawCommand
    @Command(name = "select",  aliases = {"show", "desc", "describe"},
        description = "Execute an SQL select statement returning a result.", mixinStandardHelpOptions = true)
    public String select(@Parameters(description = "The SQL statements") String [] statement) {
        return jdbcService.executeSqlFormatted(String.join(" ", statement));
    }

    @RawCommand
    @Command(name = "update", aliases = {"delete", "drop", "create", "insert", "alter"},
        description = "Execute an SQL statement returning no result.", mixinStandardHelpOptions = true)
    public String update(@Parameters(description = "The SQL statements") String [] statement) {
        return jdbcService.updateSqlVerbose(String.join(" ", statement));
    }
}
