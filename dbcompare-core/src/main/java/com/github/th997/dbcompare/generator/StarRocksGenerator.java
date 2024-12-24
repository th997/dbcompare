package com.github.th997.dbcompare.generator;

import com.github.th997.dbcompare.bean.TableColumn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StarRocksGenerator extends MysqlGenerator {

    private Integer buckets = 1;
    private Integer replicationNum = 1;

    public StarRocksGenerator() {
    }

    public StarRocksGenerator(Integer buckets, Integer replicationNum) {
        this.buckets = buckets;
        this.replicationNum = replicationNum;
    }

    @Override
    public String generateColumnSql(TableColumn c) {
        StringBuffer sql = new StringBuffer("`" + c.getColumnName() + "` ");
        sql.append(c.getColumnType());
        sql.append(c.isNullAble() ? " null" : " not null");
        if (c.getColumnDefault() != null) {
            String columnDefault = c.getColumnDefault();
            if (!c.getDataType().contains("date") && !c.getDataType().contains("time") && !columnDefault.startsWith("'")) {
                columnDefault = "'" + columnDefault + "'";
            }
            sql.append(" default " + columnDefault);
        }
        if (c.getColumnComment() != null && !c.getColumnComment().trim().isEmpty()) {
            sql.append(String.format(" comment '%s'", c.getColumnComment().replaceAll("'", "''")));
        }
        return sql.toString();
    }

    @Override
    public String generateTableSql(String scheme, String table, List<TableColumn> columnList) {
        StringBuffer sql = new StringBuffer(String.format("create table `%s`.`%s` (\n", scheme, table));
        List<String> priKeys = new ArrayList<>();
        columnList.sort(Comparator.comparing(TableColumn::isPri).reversed());
        for (TableColumn c : columnList) {
            sql.append(generateColumnSql(c) + ",\n");
            if (c.isPri()) {
                priKeys.add("`" + c.getColumnName() + "`");
            }
        }
        sql.delete(sql.length() - 2, sql.length());
        sql.append("\n)\n");
        if (!priKeys.isEmpty()) {
            sql.append(String.format("primary key (%s)\n", String.join(",", priKeys)));
            sql.append(String.format("distributed by hash (%s) buckets %s\n", priKeys.get(0), buckets));
            sql.append(String.format("order by (%s)\n", priKeys.get(0)));
        }
        sql.append(String.format("properties(\"replication_num\"=\"%s\")", replicationNum));
        return sql.toString();
    }
}
