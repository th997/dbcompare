package com.github.th997.dbcompare.generator;

import com.github.th997.dbcompare.SqlGenerator;
import com.github.th997.dbcompare.bean.TableColumn;
import com.github.th997.dbcompare.bean.TableIndex;
import com.github.th997.dbcompare.bean.TableInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MysqlGenerator implements SqlGenerator {

    @Override
    public String generateColumnSql(TableColumn c) {
        StringBuffer sql = new StringBuffer("`" + c.getColumnName() + "` ");
        sql.append(c.getColumnType());
        sql.append(c.isNullAble() ? " null" : " not null");
        if (c.getColumnDefault() != null) {
            String columnDefault = c.getColumnDefault();
            if (!c.getDataType().contains("date") && !c.getDataType().contains("time") && !columnDefault.endsWith("'")) {
                columnDefault = "'" + columnDefault + "'";
            }
            sql.append(" default " + columnDefault);
        }
        if (c.getColumnComment() != null && !c.getColumnComment().trim().isEmpty()) {
            sql.append(String.format(" comment '%s'", c.getColumnComment().replaceAll("'", "''")));
        }
        if (c.isAutoIncrement()) {
            sql.append(" auto_increment");
        }
        return sql.toString();
    }

    @Override
    public String generateTableSql(TableInfo table, List<TableColumn> columnList) {
        StringBuffer sql = new StringBuffer(String.format("create table `%s`.`%s` (\n", table.getSchemaName(), table.getTableName()));
        List<String> priKeys = new ArrayList<>();
        for (TableColumn c : columnList) {
            sql.append(generateColumnSql(c) + ",\n");
            if (c.isPri()) {
                priKeys.add("`" + c.getColumnName() + "`");
            }
        }
        if (!priKeys.isEmpty()) {
            sql.append(String.format("primary key (%s)", String.join(",", priKeys)));
        } else {
            sql.delete(sql.length() - 2, sql.length());
        }
        sql.append("\n)");
        if (table.getTableComment() != null) {
            sql.append(String.format(" comment='%s'", table.getTableComment().replaceAll("'", "''").replaceAll("\\n", " ")));
        }
        return sql.toString();
    }

    @Override
    public String generateTableSql(TableInfo table, List<TableColumn> src, List<TableColumn> dst) {
        if (dst == null || dst.isEmpty()) {
            return generateTableSql(table, src);
        }
        String initSql = String.format("alter table `%s`.`%s`\n", table.getSchemaName(), table.getTableName());
        StringBuffer sql = new StringBuffer(initSql);
        Map<String, TableColumn> srcMap = src.stream().collect(Collectors.toMap(TableColumn::getColumnName, Function.identity()));
        Map<String, TableColumn> dstMap = dst.stream().collect(Collectors.toMap(TableColumn::getColumnName, Function.identity()));
        Set<String> allColumns = new HashSet<>();
        allColumns.addAll(srcMap.keySet());
        allColumns.addAll(dstMap.keySet());
        boolean isModifyLast = false;
        for (String columnName : allColumns) {
            TableColumn srcColumn = srcMap.get(columnName);
            TableColumn dstColumn = dstMap.get(columnName);
            if (dstColumn == null) {
                String srcSql = this.generateColumnSql(srcColumn);
                sql.append("add column " + srcSql + ",\n");
                isModifyLast = false;
            } else if (srcColumn == null) {
                sql.append(String.format("drop column %s,\n", columnName));
                isModifyLast = false;
            } else {
                String srcSql = this.generateColumnSql(srcColumn);
                String dstSql = this.generateColumnSql(dstColumn);
                if (!srcSql.equals(dstSql)) {
                    sql.append("modify column " + srcSql + ", -- " + dstSql + "\n");
                    isModifyLast = true;
                }
            }
        }
        if (sql.length() == initSql.length()) {
            return null;
        }
        if (isModifyLast) {
            sql.deleteCharAt(sql.lastIndexOf(",", sql.lastIndexOf(" -- ")));
        } else {
            sql.deleteCharAt(sql.lastIndexOf(","));
        }
        return sql.toString();
    }

    @Override
    public String generateIndexSql(TableInfo table, List<TableIndex> src, List<TableIndex> dst) {
        Map<String, List<TableIndex>> srcMap = this.groupByColumn(src);
        Map<String, List<TableIndex>> dstMap = this.groupByColumn(dst);
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(srcMap.keySet());
        allKeys.addAll(dstMap.keySet());
        StringBuffer sql = new StringBuffer();
        String addIndex = "add %s index %s(%s),\n";
        for (String key : allKeys) {
            List<TableIndex> srcIndex = srcMap.get(key);
            List<TableIndex> dstIndex = dstMap.get(key);
            if (srcIndex == null) {
                sql.append("drop index " + dstIndex.get(0).getIndexName() + ",\n");
            } else if (dstIndex == null) {
                String columns = srcIndex.stream().map(TableIndex::getColumnName).collect(Collectors.joining(","));
                sql.append(String.format(addIndex, !srcIndex.get(0).isNonUnique() ? "unique" : "", srcIndex.get(0).getIndexName(), columns));
            }
        }
        if (sql.length() == 0) {
            return null;
        }
        return String.format("alter table `%s`.`%s` %s", table.getSchemaName(), table.getTableName(), sql.deleteCharAt(sql.lastIndexOf(",")).toString().trim());
    }

    public Map<String, List<TableIndex>> groupByColumn(List<TableIndex> indexList) {
        if (indexList == null || indexList.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Map<String, List<TableIndex>> map = indexList.stream().collect(Collectors.groupingBy(TableIndex::getIndexName));
        Map<String, List<TableIndex>> ret = new HashMap<>();
        for (List<TableIndex> index : map.values()) {
            if (index.get(0).isPri()) {
                continue;
            }
            String columns = index.stream().map(TableIndex::getColumnName).collect(Collectors.joining(","));
            columns = columns + "," + index.get(0).isNonUnique();
            ret.put(columns, index);
        }
        return ret;
    }
}
