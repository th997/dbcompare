package com.github.th997.dbcompare;

import com.github.th997.dbcompare.bean.TableColumn;
import com.github.th997.dbcompare.bean.TableIndex;

import java.util.List;

public interface SqlGenerator {

    String generateColumnSql(TableColumn c);

    String generateTableSql(String scheme, String table, List<TableColumn> columnList);

    String generateTableSql(String scheme, String table, List<TableColumn> src, List<TableColumn> dst);

    String generateIndexSql(String scheme, String table, List<TableIndex> src, List<TableIndex> dst);
}
