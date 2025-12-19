package org.isp63.prog1.util;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
  // para hashear(codificar) la contraseña
  public static String hashPassword(String plainPassword) {
    return encoder.encode(plainPassword);
  }
  // para verificar la contraseña
  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return encoder.matches(plainPassword, hashedPassword);
  }
}
