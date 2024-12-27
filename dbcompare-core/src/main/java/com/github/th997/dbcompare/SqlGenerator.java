package com.github.th997.dbcompare;

import com.github.th997.dbcompare.bean.TableColumn;
import com.github.th997.dbcompare.bean.TableIndex;
import com.github.th997.dbcompare.bean.TableInfo;

import java.util.List;

public interface SqlGenerator {

    String generateColumnSql(TableColumn c);

    String generateTableSql(TableInfo table, List<TableColumn> columnList);

    String generateTableSql(TableInfo table, List<TableColumn> src, List<TableColumn> dst);

    String generateIndexSql(TableInfo table, List<TableIndex> src, List<TableIndex> dst);
}
