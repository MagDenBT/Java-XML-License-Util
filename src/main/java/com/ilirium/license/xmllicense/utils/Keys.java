package com.ilirium.license.xmllicense.utils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 *
 * @author DoDo <dopoljak@gmail.com>
 */
public class Keys
{

    /**
     * Convert encoded to PublicKey
     */
    public static PublicKey convertToPublicKey(byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(pubKeySpec);
    }
}
