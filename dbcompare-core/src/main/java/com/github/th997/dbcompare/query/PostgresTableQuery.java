package com.github.th997.dbcompare.query;

import com.github.th997.dbcompare.TableQuery;

public class PostgresTableQuery implements TableQuery {

    private static final String SQL_TABLE_QUERY = "select table_schema,table_name,obj_description(concat('\"',table_schema,'\".\"',table_name,'\"')::regclass) table_comment  from information_schema.tables t where table_schema=?";
    private static final String SQL_COLUMN_QUERY = "select t.table_name, column_name, data_type column_type, udt_name data_type, character_maximum_length str_len, numeric_precision, numeric_scale, column_default, is_nullable = 'YES' null_able, (case when column_default like 'nextval(%%' then true else false end) as \"auto_increment\", (select count(*) > 0 from information_schema.table_constraints tc join information_schema.constraint_column_usage as ccu on tc.constraint_name = ccu.constraint_name where tc.table_schema = t.table_schema and tc.table_name = t.table_name and tc.constraint_type = 'PRIMARY KEY' and ccu.column_name = t.column_name) as pri, pg_catalog.col_description(c.oid, t.ordinal_position) column_comment from information_schema.columns t join pg_class c on t.table_name = c.relname join pg_namespace n on c.relnamespace = n.oid and t.table_schema = n.nspname where t.table_schema = ? %s order by ordinal_position";
    private static final String SQL_INDEX_QUERY = "select c.relname table_name, n.nspname as schema_name, c.relname as table_name, i.relname as index_name, a.attname as column_name, not x.indisunique as non_unique, x.indisprimary as pri, pg_get_indexdef(i.oid) as index_def from pg_index x join pg_class c on c.oid = x.indrelid join pg_class i on i.oid = x.indexrelid join pg_attribute a on a.attrelid = c.oid and a.attnum = any(x.indkey) left join pg_namespace n on n.oid = c.relnamespace where c.relkind in('r','m','p') and i.relkind in('i','i') and n.nspname=? %s order by i.relname, array_position(x.indkey, a.attnum)";

    public String getTableInfoSql() {
        return SQL_TABLE_QUERY;
    }

    @Override
    public String getTableColumnSql() {
        return String.format(SQL_COLUMN_QUERY, "and t.table_name=?");
    }

    @Override
    public String getSchemaColumnSql() {
        return String.format(SQL_COLUMN_QUERY, "");

    }

    @Override
    public String getTableIndexSql() {
        return String.format(SQL_INDEX_QUERY, "and c.relname=?");
    }

    @Override
    public String getSchemaIndexSql() {
        return String.format(SQL_INDEX_QUERY, "");
    }
}
