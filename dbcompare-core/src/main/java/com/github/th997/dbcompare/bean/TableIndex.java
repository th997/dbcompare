package com.github.th997.dbcompare.bean;

public class TableIndex {
    private String tableName;
    private String indexName;
    private String columnName;
    private boolean nonUnique;
    private boolean pri;

    public boolean isPri() {
        return pri;
    }

    public void setPri(boolean pri) {
        this.pri = pri;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isNonUnique() {
        return nonUnique;
    }

    public void setNonUnique(boolean nonUnique) {
        this.nonUnique = nonUnique;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "TableIndex{" + "tableName='" + tableName + '\'' + ", indexName='" + indexName + '\'' + ", columnName='" + columnName + '\'' + ", nonUnique=" + nonUnique + ", pri=" + pri + '}';
    }
}
