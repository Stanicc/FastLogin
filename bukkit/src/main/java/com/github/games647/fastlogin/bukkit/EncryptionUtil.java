package com.github.games647.fastlogin.bukkit;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption and decryption minecraft util for connection between servers
 * and paid Minecraft account clients.
 *
 * @see net.minecraft.server.MinecraftEncryption
 */
public class EncryptionUtil {

    public static final int VERIFY_TOKEN_LENGTH = 4;
    public static final String KEY_PAIR_ALGORITHM = "RSA";

    private EncryptionUtil() {
        //utility
    }

    /**
     * Generate a RSA key pair
     *
     * @return The RSA key pair.
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);

            keyPairGenerator.initialize(1_024);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            //Should be existing in every vm
            throw new ExceptionInInitializerError(nosuchalgorithmexception);
        }
    }

    /**
     * Generate a random token. This is used to verify that we are communicating with the same player
     * in a login session.
     *
     * @param random random generator
     * @return an error with 4 bytes long
     */
    public static byte[] generateVerifyToken(Random random) {
        byte[] token = new byte[VERIFY_TOKEN_LENGTH];
        random.nextBytes(token);
        return token;
    }

    /**
     * Generate the server id based on client and server data.
     *
     * @param sessionId    session for the current login attempt
     * @param sharedSecret shared secret between the client and the server
     * @param publicKey    public key of the server
     * @return the server id formatted as a hexadecimal string.
     */
    public static String getServerIdHashString(String sessionId, Key sharedSecret, PublicKey publicKey) {
        try {
            byte[] serverHash = getServerIdHash(sessionId, sharedSecret, publicKey);
            return (new BigInteger(serverHash)).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Decrypts the content and extracts the key spec.
     *
     * @param cipher     decryption cipher initialized with the private key
     * @param sharedKey  the encrypted shared key
     * @return shared secret key
     * @throws GeneralSecurityException if it fails to decrypt the data
     */
    public static SecretKey decryptSharedKey(Cipher cipher, byte[] sharedKey) throws GeneralSecurityException {
        return new SecretKeySpec(decrypt(cipher, sharedKey), "AES");
    }

    /**
     * Decrypted the given data using the cipher.
     *
     * @param cipher decryption cypher initialized with the private key
     * @param data   the encrypted data
     * @return clear text data
     * @throws GeneralSecurityException if it fails to decrypt the data
     */
    public static byte[] decrypt(Cipher cipher, byte[] data) throws GeneralSecurityException {
        return cipher.doFinal(data);
    }

    private static byte[] getServerIdHash(String sessionId, Key sharedSecret, PublicKey publicKey)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        digest.update(sessionId.getBytes(StandardCharsets.ISO_8859_1));
        digest.update(sharedSecret.getEncoded());
        digest.update(publicKey.getEncoded());

        return digest.digest();
    }
}
