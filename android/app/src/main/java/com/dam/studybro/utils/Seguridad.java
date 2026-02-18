package com.dam.studybro.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Clase de utilidad para seguridad (Nivel 4)
 * Responsable: Antonio
 */
public class Seguridad {

    /**
     * Encripta una contraseña usando SHA-256
     * @param contrasena La contraseña en texto plano
     * @return El hash de la contraseña en hexadecimal
     */
    public static String encriptarPassword(String contrasena) {
        try {
            // Creamos el "motor" de encriptación SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Convertimos la contraseña a bytes y la procesamos
            byte[] hashBytes = digest.digest(contrasena.getBytes());
            
            // Convertimos los bytes a String Hexadecimal (lo leíble)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
