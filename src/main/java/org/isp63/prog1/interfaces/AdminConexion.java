package org.isp63.prog1.interfaces;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum AdminConexion {
  INSTANCE;

  private final Logger log = Logger.getLogger(AdminConexion.class.getName());
  private HikariDataSource dataSource;

  AdminConexion() {
    this.dataSource = inicializarPool();
  }

  private HikariDataSource inicializarPool() {
    try {
      log.info("[DB-LOG] Iniciando configuración del pool HikariCP para Reparaciones...");

      // Variables de entorno inyectadas por Docker o Propiedades del Sistema para Tests
      String envUrl  = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : System.getProperty("db.url");
      String envUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : System.getProperty("db.user");
      String envPass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : System.getProperty("db.password");

      HikariConfig config = new HikariConfig();
      config.setPoolName("ReparacionesPool");
      config.setDriverClassName("com.mysql.cj.jdbc.Driver");
      config.setConnectionTestQuery("SELECT 1");

      if (envUrl != null && envUser != null && envPass != null) {
        log.info("[DB-LOG] Modo: DOCKER / PRODUCCIÓN (variables de entorno)");
        config.setJdbcUrl(envUrl);
        config.setUsername(envUser);
        config.setPassword(envPass);
        config.setMaximumPoolSize(15);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000);
        config.setIdleTimeout(300_000);
        config.setMaxLifetime(600_000);
      } else {
        log.warning("[DB-LOG] Modo: DESARROLLO LOCAL (database.properties)");
        Properties props = new Properties();
        try (InputStream is = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("database.properties")) {
          if (is == null) throw new IOException("database.properties no encontrado en classpath");
          props.load(is);
        }
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user", "root"));
        config.setPassword(props.getProperty("db.pass"));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "15")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimumIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout", "10000")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idleTimeout", "300000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("hikari.maxLifetime", "600000")));
        log.info("[DB-LOG] URL: " + config.getJdbcUrl());
      }

      HikariDataSource ds = new HikariDataSource(config);
      log.info("[DB-LOG] Pool listo. Tamaño máximo: " + config.getMaximumPoolSize());
      return ds;

    } catch (IOException e) {
      log.log(Level.SEVERE, "[DB-LOG] Error leyendo database.properties", e);
      throw new ExceptionInInitializerError(e);
    } catch (Exception e) {
      log.log(Level.SEVERE, "[DB-LOG] Error crítico inicializando HikariCP", e);
      throw new ExceptionInInitializerError(e);
    }
  }

  public Connection obtenerConexion() throws SQLException {
    Connection conn = dataSource.getConnection();
    // Opcional: comentar esta línea en producción para no saturar los logs
    // log.info("[DB-LOG] Conexión obtenida. Activas: " + dataSource.getHikariPoolMXBean().getActiveConnections());
    return conn;
  }

  public void cerrarPool() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      log.info("[DB-LOG] Pool cerrado correctamente.");
    }
  }
  public void recargarPoolParaTests() {
    cerrarPool();
    this.dataSource = inicializarPool();
  }
}
