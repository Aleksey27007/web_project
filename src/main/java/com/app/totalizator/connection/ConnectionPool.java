package com.app.totalizator.connection;

import com.mysql.cj.jdbc.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;


public class ConnectionPool {
    private static final Logger logger = LogManager.getLogger();
    private static final ReentrantLock instanceLock = new ReentrantLock();
    private static final Path pathProperties = Paths.get("D:\\IntJava\\totalizator\\totalizator\\src\\main\\resources\\db.properties");
    private static final int connectionCapacity = 8;
    private static ConnectionPool instance;
    private final BlockingQueue<Connection> free = new LinkedBlockingQueue<>(connectionCapacity);
    private final BlockingQueue<Connection> used = new LinkedBlockingQueue<>(connectionCapacity);

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            logger.warn("Driver has not register.");
            e.printStackTrace();
        }
    }

    private ConnectionPool() {

        Properties properties = new Properties();

        logger.info("Properties created");

        try(InputStream input = new FileInputStream(pathProperties.toFile())) {
            logger.info("Try load input properties");
            properties.load(input);
        } catch (IOException e) {
            logger.warn("Properties not found.");
            e.printStackTrace();
        }

        for (int i = 0; i < connectionCapacity; i++) {
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(properties.getProperty("db.url"),
                        properties.getProperty("db.user"), properties.getProperty("db.password"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (connection != null) {
                free.add(connection);
            }
        }
    }

    public static ConnectionPool getInstance() {
        if (instance == null) {
            instanceLock.lock();
            try {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            } finally {
                instanceLock.unlock();
            }
        }
        return instance;
    }

    public Connection getConnection(){
        Connection connection;
        try {
            connection = free.take();
            used.put(connection);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void releaseConnection(Connection connection){
        try {
            used.remove(connection);
            free.put(connection);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
