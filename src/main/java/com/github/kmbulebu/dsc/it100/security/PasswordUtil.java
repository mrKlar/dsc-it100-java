package com.github.kmbulebu.dsc.it100.security;

import java.util.Base64;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Crypt32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinCrypt;

public class PasswordUtil {

    private static final int CRYPTPROTECT_UI_FORBIDDEN = 0x1;

    /**
     * Encrypts a plaintext password using Windows DPAPI. The encryption is tied to the current user context.
     * @param plaintext The password to encrypt.
     * @return A Base64 encoded string of the encrypted data.
     */
    public static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return "";
        }

        byte[] data = plaintext.getBytes();
        WinCrypt.DATA_BLOB in = new WinCrypt.DATA_BLOB();
        in.cbData = data.length;
        in.pbData = new Memory(data.length);
        in.pbData.write(0, data, 0, data.length);

        WinCrypt.DATA_BLOB out = new WinCrypt.DATA_BLOB();

        boolean success = Crypt32.INSTANCE.CryptProtectData(in, "password", null, null, null, CRYPTPROTECT_UI_FORBIDDEN, out);
        if (!success) {
            throw new RuntimeException("Failed to encrypt data. Windows Error code: " + Kernel32.INSTANCE.GetLastError());
        }

        byte[] encryptedData = out.pbData.getByteArray(0, out.cbData);
        Kernel32.INSTANCE.LocalFree(out.pbData);

        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Decrypts a password using Windows DPAPI. Can only be decrypted by the same user who encrypted it.
     * @param encryptedBase64 The Base64 encoded encrypted string.
     * @return The plaintext password.
     */
    public static String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isEmpty()) {
            return "";
        }

        byte[] encryptedData = Base64.getDecoder().decode(encryptedBase64);
        WinCrypt.DATA_BLOB in = new WinCrypt.DATA_BLOB();
        in.cbData = encryptedData.length;
        in.pbData = new Memory(encryptedData.length);
        in.pbData.write(0, encryptedData, 0, encryptedData.length);

        WinCrypt.DATA_BLOB out = new WinCrypt.DATA_BLOB();

        boolean success = Crypt32.INSTANCE.CryptUnprotectData(in, null, null, null, null, CRYPTPROTECT_UI_FORBIDDEN, out);
        if (!success) {
            throw new RuntimeException("Failed to decrypt data. Windows Error code: " + Kernel32.INSTANCE.GetLastError());
        }

        byte[] decryptedData = out.pbData.getByteArray(0, out.cbData);
        Kernel32.INSTANCE.LocalFree(out.pbData);

        return new String(decryptedData);
    }

    /**
     * Main method to allow command-line encryption of passwords.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ... com.github.kmbulebu.dsc.it100.security.PasswordUtil <password_to_encrypt>");
            System.err.println("\nPlease provide a password to encrypt as a command line argument.");
            System.exit(1);
        }

        if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            System.err.println("This utility uses Windows DPAPI and can only be run on Windows.");
            System.exit(1);
        }

        try {
            String plaintextPassword = args[0];
            String encryptedPassword = encrypt(plaintextPassword);
            System.out.println("Encrypted password (for the current user):");
            System.out.println(encryptedPassword);
        } catch (Exception e) {
            System.err.println("An error occurred during encryption:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
