package org.isp63.prog1.interfaces;

import org.isp63.prog1.util.ConexionPool;

import java.sql.Connection;
import java.sql.SQLException;

public interface AdmConnexion {

  default Connection obtenerConexion() {
    try {
      return ConexionPool.getConnection();
    } catch (SQLException e) {
      System.err.println("No se pudo obtener una conexion del pool");
      throw new RuntimeException(e);
    }
  }
}
