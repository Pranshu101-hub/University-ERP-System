package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

//manages conn pools for both dbs
public class MySqlConnectionManager {
    private static MySqlConnectionManager instance; // single instance
    private final HikariDataSource authDataSource;
    private final HikariDataSource erpDataSource;


    //private constructor that takes the configs from Main.java
    private MySqlConnectionManager(DatabaseConfig authConfig, DatabaseConfig erpConfig){
        authDataSource = createDataSource(authConfig);
        erpDataSource = createDataSource(erpConfig);
    }

    //creates a HikariCP data source (conn pool)
    private HikariDataSource createDataSource(DatabaseConfig config){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(10);

        return new HikariDataSource(hikariConfig);
    }

    public static synchronized void init(DatabaseConfig authConfig, DatabaseConfig erpConfig){
        if (instance ==null){
            // this creates the one and only instance
            instance = new MySqlConnectionManager(authConfig, erpConfig);
        }
    }

    public static synchronized MySqlConnectionManager getInstance(){
        if (instance ==null){
            throw new IllegalStateException("MySqlConnectionManager has not been initialized. Call init() first.");
        } //error if init was not called first
        // This now returns the real instance
        return instance;
    }

    //gets connection to Auth DB
    public Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }


    //gets connection to ERP DB
    public Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }

     //close both conn pools
    public void closeDataSources(){
        if (authDataSource != null) authDataSource.close();
        if (erpDataSource != null) erpDataSource.close();
    }
}