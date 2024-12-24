package com.github.th997.dbcompare;

import com.github.th997.dbcompare.converter.PostgresToStarRocksConverter;
import com.github.th997.dbcompare.exception.NoSuchImplementException;
import com.github.th997.dbcompare.generator.MysqlGenerator;
import com.github.th997.dbcompare.generator.StarRocksGenerator;
import com.github.th997.dbcompare.query.MysqlTableQuery;
import com.github.th997.dbcompare.query.PostgresTableQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DbCompareFactory {

    private final static Map<String, TableQuery> TABLE_QUERY_MAP = new HashMap<>();
    private final static Map<String, SqlGenerator> SQL_GENERATOR_MAP = new HashMap<>();
    private final static Map<String, TypeConverter> TYPE_CONVERTER_MAP = new HashMap<>();

    static {
        TABLE_QUERY_MAP.put("mysql", new MysqlTableQuery());
        TABLE_QUERY_MAP.put("starrocks", new MysqlTableQuery());
        TABLE_QUERY_MAP.put("postgresql", new PostgresTableQuery());
        //
        SQL_GENERATOR_MAP.put("mysql", new MysqlGenerator());
        SQL_GENERATOR_MAP.put("starrocks", new StarRocksGenerator());
        //
        TYPE_CONVERTER_MAP.put("postgresql-to-starrocks", new PostgresToStarRocksConverter());
    }

    public static TableQuery getTableQuery(String jdbcType) {
        TableQuery ret = TABLE_QUERY_MAP.get(jdbcType);
        if (ret == null) {
            throw new NoSuchImplementException(jdbcType + ":TableQuery");
        }
        return ret;
    }

    public static SqlGenerator getSqlGenerator(String jdbcType) {
        SqlGenerator ret = SQL_GENERATOR_MAP.get(jdbcType);
        if (ret == null) {
            throw new NoSuchImplementException(jdbcType + ":SqlGenerator");
        }
        return ret;
    }

    public static TypeConverter getTypeConverter(String jdbcType) {
        TypeConverter ret = TYPE_CONVERTER_MAP.get(jdbcType);
        if (ret == null) {
            throw new NoSuchImplementException(jdbcType + ":TypeConverter");
        }
        return ret;
    }

    public static String getJdbcType(String jdbcUrl) {
        String[] split = jdbcUrl.split(":");
        return split[1];
    }

    public static Connection getConn(String jdbcUrl, String userName, String passWord) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcUrl, userName, passWord);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("No suitable driver found")) {
                String jdbcType = getJdbcType(jdbcUrl);
                if (jdbcType.equals("postgresql")) {
                    Class.forName("org.postgresql.Driver");
                } else if (jdbcType.equals("mysql")) {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                    } catch (ClassNotFoundException e1) {
                        Class.forName("com.mysql.jdbc.Driver");
                    }
                }
                connection = DriverManager.getConnection(jdbcUrl, userName, passWord);
            }
        }
        return connection;
    }
}
