package com.github.th997.dbcompare.generator;

import com.github.th997.dbcompare.SqlGenerator;
import com.github.th997.dbcompare.bean.TableColumn;
import com.github.th997.dbcompare.bean.TableIndex;
import com.github.th997.dbcompare.bean.TableInfo;

import java.util.List;

public class PostgresGenerator implements SqlGenerator {
    @Override
    public String generateColumnSql(TableColumn c) {
        return "";
    }

    @Override
    public String generateTableSql(TableInfo table, List<TableColumn> columnList) {
        return "";
    }

    @Override
    public String generateTableSql(TableInfo table, List<TableColumn> src, List<TableColumn> dst) {
        return "";
    }

    @Override
    public String generateIndexSql(TableInfo table, List<TableIndex> src, List<TableIndex> dst) {
        return "";
    }

}
