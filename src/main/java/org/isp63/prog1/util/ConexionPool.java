package org.isp63.prog1.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionPool {

  // Se quita 'final' para poder reiniciar el pool si se cierra entre tests
  private static HikariDataSource dataSource = crearDataSource();

  private ConexionPool() {
  }

  public static synchronized Connection getConnection() throws SQLException {
    // Si el pool fue cerrado por un test previa o está nulo, lo volvemos a instanciar
    if (dataSource == null || dataSource.isClosed()) {
      dataSource = crearDataSource();
    }
    return dataSource.getConnection();
  }

  /**
   * Cierra el pool de conexiones actual.
   * Útil para liberar conexiones en métodos @AfterEach o @AfterAll de tests.
   */
  public static synchronized void close() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
      dataSource = null;
    }
  }

  private static HikariDataSource crearDataSource() {
    Properties props = cargarPropiedades();

    HikariConfig config = new HikariConfig();
    config.setDriverClassName(leerPropiedad("db.driver", props));
    config.setJdbcUrl(leerPropiedad("db.url", props));
    config.setUsername(leerPropiedad("db.user", props));
    config.setPassword(leerPropiedad("db.pass", props));
    config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "10")));
    config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimumIdle", "2")));
    config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout", "10000")));
    config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idleTimeout", "300000")));
    config.setMaxLifetime(Long.parseLong(props.getProperty("hikari.maxLifetime", "600000")));
    config.setPoolName("TiendaHikariPool");

    return new HikariDataSource(config);
  }

  private static Properties cargarPropiedades() {
    Properties props = new Properties();
    try (InputStream input = ConexionPool.class.getClassLoader().getResourceAsStream("database.properties")) {
      if (input == null) {
        throw new IllegalStateException("No se encontro database.properties en src/main/resources");
      }
      props.load(input);
      return props;
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer database.properties", e);
    }
  }

  private static String leerPropiedad(String clave, Properties props) {
    String valorSistema = System.getProperty(clave);
    if (valorSistema != null && !valorSistema.isBlank()) {
      return valorSistema;
    }
    String valorEnv = System.getenv(clave.replace('.', '_').toUpperCase());
    if (valorEnv != null && !valorEnv.isBlank()) {
      return valorEnv;
    }
    return props.getProperty(clave);
  }

}