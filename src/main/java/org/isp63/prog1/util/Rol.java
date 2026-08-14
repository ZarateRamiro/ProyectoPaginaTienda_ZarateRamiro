package org.isp63.prog1.util;

public final class Rol {

  public static final String ADMIN = "ADMIN";
  public static final String USUARIO = "USUARIO";

  private Rol() {
  }

  public static boolean esAdmin(String rol) {
    return ADMIN.equalsIgnoreCase(rol);
  }

  public static boolean esUsuario(String rol) {
    return USUARIO.equalsIgnoreCase(rol);
  }
}
