package org.isp63.prog1.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface AdmConnexion {

  default Connection obtenerConexion() {
    try {
      // Redirige directamente al singleton AdminConexion (HikariCP)
      return AdminConexion.INSTANCE.obtenerConexion();
    } catch (SQLException e) {
      System.err.println("No se pudo obtener una conexion del pool");
      throw new RuntimeException(e);
    }
  }
}