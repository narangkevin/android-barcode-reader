package com.example.customheader;

import org.bouncycastle.util.encoders.Hex;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class StmAppPlayerAuthen {

    private String iv = "S7S43799DFB8B3D9";
    private String SecretKey = "DMP-SC-341125129";
    private IvParameterSpec ivParameterSpec;
    private SecretKeySpec keySpec;
    private Cipher cipher;
    final private String TAG = "StmAppPlayerAuthen";

    public StmAppPlayerAuthen() {
        ivParameterSpec = new IvParameterSpec(iv.getBytes());
        keySpec = new SecretKeySpec(SecretKey.getBytes(), "AES");
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String text) throws Exception {
        if (text == null || text.length() == 0)
            throw new Exception(TAG + " Empty string");

        byte[] encrypted = null;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            encrypted = cipher.doFinal(padString(text).getBytes());
        } catch (Exception e) {
            throw new Exception(TAG + " [Encrypt] " + e.getMessage());
        }

        return  new String(Hex.encode(encrypted));
    }

    public String decrypt(String code) throws Exception {
        if (code == null || code.length() == 0)
            throw new Exception(TAG + " Empty string");

        byte[] decrypted = null;

        try {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            decrypted = cipher.doFinal(Hex.decode(code));
        } catch (Exception e) {
            throw new Exception(TAG + " [Decrypt] " + e.getMessage());
        }

        return new String(decrypted);
    }

    private static String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }

        return source;
    }
}
