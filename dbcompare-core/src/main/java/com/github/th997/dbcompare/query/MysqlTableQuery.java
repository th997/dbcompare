package com.github.th997.dbcompare.query;

import com.github.th997.dbcompare.TableQuery;

public class MysqlTableQuery implements TableQuery {

    private static final String SQL_COLUMN_QUERY = "select table_name, column_name,column_type,data_type,column_comment,character_maximum_length str_len,numeric_precision,numeric_scale,column_default,is_nullable = 'YES' null_able,column_key = 'PRI' pri,extra ='auto_increment' as \"auto_increment\" from information_schema.columns where table_schema =? %s order by ordinal_position";

    private static final String SQL_INDEX_QUERY = "select table_name, index_name,column_name,non_unique,index_name='PRIMARY' pri from information_schema.statistics where table_schema = ? %s order by seq_in_index";

    @Override
    public String getTableColumnSql() {
        return String.format(SQL_COLUMN_QUERY, "and table_name=?");
    }

    @Override
    public String getSchemaColumnSql() {
        return String.format(SQL_COLUMN_QUERY, "");
    }

    @Override
    public String getTableIndexSql() {
        return String.format(SQL_INDEX_QUERY, "and table_name=?");

    }

    @Override
    public String getSchemaIndexSql() {
        return String.format(SQL_INDEX_QUERY, "");
    }
}
