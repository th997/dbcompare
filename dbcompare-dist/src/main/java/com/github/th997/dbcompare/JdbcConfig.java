package com.github.th997.dbcompare;

public class JdbcConfig {
    /**
     * @param url jdbc url like: user:password@jdbc:mysql://localhost:3306
     */
    public JdbcConfig(String url) {
        String[] urlSplit = url.split("@");
        String[] split1 = urlSplit[0].split(":");
        String[] split2 = urlSplit[1].split(":");
        this.username = split1[0];
        this.password = split1[1];
        this.jdbcUrl = urlSplit[1];
        this.jdbcType = split2[1];
    }

    private String jdbcUrl;
    private String username;
    private String password;
    private String jdbcType;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }
}
