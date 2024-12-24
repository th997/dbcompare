package com.github.th997.dbcompare.converter;

import com.github.th997.dbcompare.bean.TableColumn;
import com.github.th997.dbcompare.TypeConverter;

import java.util.Arrays;
import java.util.List;

public class PostgresToStarRocksConverter implements TypeConverter {
    @Override
    public TableColumn convert(TableColumn c) {
        TableColumn ret = new TableColumn();
        ret.setColumnType(this.convertType(c));
        ret.setColumnDefault(this.convertDefault(c));
        ret.setDataType(c.getDataType());
        ret.setAutoIncrement(c.isAutoIncrement());
        ret.setColumnComment(c.getColumnComment());
        ret.setColumnName(c.getColumnName());
        ret.setNullAble(c.isNullAble());
        ret.setNumericPrecision(c.getNumericPrecision());
        ret.setNumericScale(c.getNumericScale());
        ret.setPri(c.isPri());
        ret.setStrLen(c.getStrLen());
        return ret;
    }

    private String convertDefault(TableColumn c) {
        if (c.getColumnDefault() == null || c.isAutoIncrement()) {
            return null;
        }
        String columnDefault = c.getColumnDefault();
        List<String> chs = Arrays.asList("::character varying", "::bpchar", "::text", "::timestamp without time zone", "::integer");
        for (String ch : chs) {
            if (c.getColumnDefault().endsWith(ch)) {
                columnDefault = columnDefault.substring(0, columnDefault.length() - ch.length());
                break;
            }
        }
        if (!c.getDataType().contains("date") && !c.getDataType().contains("time") && !columnDefault.startsWith("'")) {
            columnDefault = "'" + columnDefault + "'";
        }
        return columnDefault;
    }

    private String convertType(TableColumn c) {
        String type = c.getDataType();
        switch (type) {
            // string
            case "varchar":
            case "char":
            case "bpchar":
            case "time":
                return "varchar(65533)";
            case "text":
                return "varchar(1048576)";
            // number
            case "int2":
                return "smallint(6)";
            case "int4":
                return "int(11)";
            case "int8":
                return "bigint(20)";
            case "float4":
                return "float";
            case "float8":
                return "double";
            case "numeric":
                return "decimal(38, 18)";
            // date
            case "timestamp":
                return "datetime";
            case "date":
                return "date";
            // bool
            case "bool":
                return "boolean";
            // byte
            case "bytea":
                return "varbinary(1048576)";
            // json
            case "json":
                return "json";
        }
        return type;
    }
}
