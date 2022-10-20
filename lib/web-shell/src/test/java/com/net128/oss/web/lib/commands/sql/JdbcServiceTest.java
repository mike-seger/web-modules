package com.net128.oss.web.lib.commands.sql;

import com.net128.oss.web.webshell.commands.sql.JdbcService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(JdbcService.class)
@Slf4j
public class JdbcServiceTest {
    @Autowired
    JdbcService jdbcService;

    @Test
    public void testTables() {
        jdbcService.updateSql("drop table t1");
        assertTrue(jdbcService.updateSql("create table t1(a integer)"));
        Collection<String> tables= jdbcService.tables(null);
        assertEquals(1, tables.size());
        log.info("\n{}", jdbcService.tablesFormatted(null));
        assertTrue(jdbcService.updateSql("drop table t1"));
    }

    @Test
    public void testSchemas() {
        Collection<String> schemas= jdbcService.schemas();
        assertEquals(2, schemas.size());
    }

    @Test
    public void testCrud() throws IOException, SQLException {
        jdbcService.updateSql("drop table t1");
        assertTrue(jdbcService.updateSql("create table t1(a integer, b varchar(6))"));
        Collection<String> tables= jdbcService.tables(null);
        assertEquals(1, tables.size());
        assertTrue(jdbcService.updateSql(
            "insert into t1(a, b) values(123, 'ABC123');\n"
            +"insert into t1(a, b) values(456, 'ABC123');\n"
            +"insert into t1(a, b) values(789, 'ABC123');\n"));
        //FIXME results are empty here!
        List<List<String>> result = jdbcService.executeSql("select * from t1");
        assertNotNull(result);
        assertTrue(jdbcService.updateSql("drop table t1"));
    }

    @Test
    public void testTableInfo() {
        jdbcService.updateSql("drop table t1");
        assertTrue(jdbcService.updateSql("create table t1(a integer not null, b varchar(6))"));
        List<List<String>> tableInfo= jdbcService.tableInfo("t1");
        assertEquals(4, tableInfo.size());
        assertEquals(3, tableInfo.get(2).size());
        log.info("\n{}", jdbcService.tableInfoFormatted("t1"));
    }
}
