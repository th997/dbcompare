package com.github.th997.dbcompare;

import com.github.th997.dbcompare.bean.TableColumn;
import com.github.th997.dbcompare.bean.TableIndex;
import com.github.th997.dbcompare.exception.JdbcUrlParseException;
import com.github.th997.dbcompare.exception.NoSuchImplementException;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

public class DbCompare {
    public static void main(String[] args) throws Exception {
        // execute -i ddl.sql -u user:password@jdbc:mysql://localhost:3306,user:password@jdbc:mysql://localhost:3307
        // compare -o compare.sql -ss test ts test  -u user:password@jdbc:mysql://localhost:3306,user:password@jdbc:mysql://localhost:3307
        Options options = new Options();
        options.addOption("h", "help", false, "print usage");
        options.addOption("t", "type", true, "operate type: schema|data|exec, schema compare|data compare|execute sql");
        options.addOption("i", "input", true, "input file path or sql");
        options.addOption("o", "output", true, "output file path: default console");
        options.addOption("u", "urls", true, "jdbc urls like: user:password@jdbc:mysql://localhost:3306,...");
        options.addOption("ss", "schema", true, "compare source schema");
        options.addOption("st", "tables", true, "compare source tables, such as tb1,tb2");
        options.addOption("ts", "target-schema", true, "compare target schema");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("h")) {
            printHelp(options);
            return;
        }
        try {
            run(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            printHelp(options);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar dbcompare.jar", options);
        System.exit(0);
    }

    private static List<JdbcConfig> parseUrls(String urls, Integer minLength) {
        List<JdbcConfig> list = new ArrayList<>();
        for (String url : urls.split(",")) {
            try {
                JdbcConfig config = new JdbcConfig(url);
                list.add(config);
            } catch (Exception e) {
                throw new JdbcUrlParseException(e.getMessage());
            }
        }
        if (minLength != null && list.size() < minLength) {
            throw new JdbcUrlParseException("url size must > " + minLength);
        }
        return list;
    }

    private static void run(CommandLine cmd) throws Exception {
        String type = cmd.getOptionValue("t", "schema");
        List<JdbcConfig> configs = parseUrls(cmd.getOptionValue("u"), "exec".equals(type) ? 1 : 2);
        if ("exec".equals(type)) {
            String input = cmd.getOptionValue("i");
            if (input == null) {
                throw new IllegalArgumentException("input is required");
            }
            String sqls = input;
            File inputFile = new File(input);
            if (inputFile.exists()) {
                sqls = new String(Files.readAllBytes(inputFile.toPath()));
            }
            for (JdbcConfig config : configs) {
                try (Connection conn = DbCompareFactory.getConn(config.getJdbcUrl(), config.getUsername(), config.getPassword())) {
                    for (String sql : sqls.split(";")) {
                        try (Statement st = conn.createStatement()) {
                            st.execute(sql);
                        }
                    }
                }
            }
            return;
        }

        String output = cmd.getOptionValue("o");
        String sourceSchema = cmd.getOptionValue("ss");
        String sourceTables = cmd.getOptionValue("st");
        String targetSchema = cmd.getOptionValue("ts", sourceSchema);
        Set<String> sourceTableSet = new HashSet<>();
        if (sourceTables != null) {
            for (String table : sourceTables.split(",")) {
                sourceTableSet.add(table.trim());
            }
        }

        JdbcConfig source = configs.get(0);
        JdbcConfig target = configs.get(1);
        if (!source.getJdbcType().equals(target.getJdbcType())) {
            throw new NoSuchImplementException(String.format("%s to %s", source.getJdbcType(), target.getJdbcType()));
        }
        TableQuery sourceQuery = DbCompareFactory.getTableQuery(source.getJdbcType());
        TableQuery targetQuery = DbCompareFactory.getTableQuery(target.getJdbcType());
        SqlGenerator sqlGenerator = DbCompareFactory.getSqlGenerator(target.getJdbcType());

        StringBuffer ret = new StringBuffer();
        try (Connection srcConn = DbCompareFactory.getConn(source.getJdbcUrl(), source.getUsername(), source.getPassword());//
             Connection targetConn = DbCompareFactory.getConn(target.getJdbcUrl(), target.getUsername(), target.getPassword())) {
            Map<String, List<TableColumn>> sourceMap = sourceQuery.querySchemaColumn(srcConn, sourceSchema);
            Map<String, List<TableColumn>> targetMap = targetQuery.querySchemaColumn(targetConn, targetSchema);
            Map<String, List<TableIndex>> sourceIndexMap = sourceQuery.querySchemaIndex(srcConn, sourceSchema);
            Map<String, List<TableIndex>> targetIndexMap = targetQuery.querySchemaIndex(targetConn, targetSchema);
            sourceMap.forEach((table, columns) -> {
                if (sourceTableSet.isEmpty() || sourceTableSet.contains(table)) {
                    String sql = sqlGenerator.generateTableSql(targetSchema, table, columns, targetMap.get(table));
                    if (sql != null) {
                        ret.append(sql + ";\n\n");
                    }
                    String indexSql = sqlGenerator.generateIndexSql(targetSchema, table, sourceIndexMap.get(table), targetIndexMap.get(table));
                    if (indexSql != null) {
                        ret.append(indexSql + ";\n\n");
                    }
                }
            });
        }
        if (output != null) {
            File outputFile = new File(output);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(ret.toString().getBytes());
                fos.flush();
            }
        } else {
            System.out.println(ret);
        }
    }
}