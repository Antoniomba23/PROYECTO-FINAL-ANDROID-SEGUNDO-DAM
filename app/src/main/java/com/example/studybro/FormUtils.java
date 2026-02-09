package com.example.studybro;

import com.google.android.material.textfield.TextInputLayout;

import org.mindrot.jbcrypt.BCrypt;

public class FormUtils {



    public static boolean isTilEmpty(TextInputLayout textInputLayout) {
        return String.valueOf(textInputLayout.getEditText().getText()).isEmpty();
    }

    public static String generateHashedPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static String getTilText(TextInputLayout textInputLayout){

        return String.valueOf((textInputLayout.getEditText().getText()));
    }

    public static boolean checkPassword(String candidate, String hashed) {
        return BCrypt.checkpw(candidate, hashed);
    }
}




