package com.github.th997.dbcompare;

import com.github.th997.dbcompare.bean.TableColumn;
import com.github.th997.dbcompare.bean.TableIndex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface TableQuery {

    String getTableColumnSql();

    String getSchemaColumnSql();

    String getTableIndexSql();

    String getSchemaIndexSql();

    default List<TableColumn> queryTableColumn(Connection connection, String schemaName, String tableName) throws SQLException {
        List<TableColumn> tableColumnList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(getTableColumnSql())) {
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tableColumnList.add(getTableColumnResult(rs));
                }
            }
        }
        return tableColumnList;
    }

    default Map<String, List<TableColumn>> querySchemaColumn(Connection connection, String schemaName) throws SQLException {
        List<TableColumn> tableColumnList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(getSchemaColumnSql())) {
            statement.setString(1, schemaName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tableColumnList.add(getTableColumnResult(rs));
                }
            }
        }
        return tableColumnList.stream().collect(Collectors.groupingBy(TableColumn::getTableName));
    }

    default TableColumn getTableColumnResult(ResultSet rs) throws SQLException {
        TableColumn column = new TableColumn();
        column.setTableName(rs.getString("table_name"));
        column.setColumnName(rs.getString("column_name"));
        column.setColumnType(rs.getString("column_type"));
        column.setDataType(rs.getString("data_type"));
        column.setColumnComment(rs.getString("column_comment"));
        column.setStrLen(rs.getLong("str_len"));
        column.setNumericPrecision(rs.getInt("numeric_precision"));
        column.setNumericScale(rs.getInt("numeric_scale"));
        column.setColumnDefault(rs.getString("column_default"));
        column.setNullAble(rs.getBoolean("null_able"));
        column.setPri(rs.getBoolean("pri"));
        column.setAutoIncrement(rs.getBoolean("auto_increment"));
        return column;
    }

    default List<TableIndex> queryTableIndex(Connection connection, String schemaName, String tableName) throws SQLException {
        List<TableIndex> tableIndexList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(getTableIndexSql())) {
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tableIndexList.add(getTableIndexResult(rs));
                }
            }
        }
        return tableIndexList;
    }

    default Map<String, List<TableIndex>> querySchemaIndex(Connection connection, String schemaName) throws SQLException {
        List<TableIndex> tableIndexList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(getSchemaIndexSql())) {
            statement.setString(1, schemaName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tableIndexList.add(getTableIndexResult(rs));
                }
            }
        }
        return tableIndexList.stream().collect(Collectors.groupingBy(TableIndex::getTableName));
    }

    default TableIndex getTableIndexResult(ResultSet rs) throws SQLException {
        TableIndex index = new TableIndex();
        index.setTableName(rs.getString("table_name"));
        index.setIndexName(rs.getString("index_name"));
        index.setColumnName(rs.getString("column_name"));
        index.setPri(rs.getBoolean("pri"));
        index.setNonUnique(rs.getBoolean("non_unique"));
        return index;
    }


}
