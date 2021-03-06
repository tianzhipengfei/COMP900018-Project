package com.example.group_w01_07_3.util;

// (Modified) From: https://blog.csdn.net/theUncle/article/details/100156976?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param#3.DES%28Data%20Encryption%20Standard%29

/**
 * CaesarCipherUtil is used to encrypt or decrypt string
 */
public class CaesarCipherUtil {

    /**
     * CaesarCipherUtil encrypt
     *
     * @param original the string to be encrypted
     * @param key      encryption key
     * @return encrypted string
     */
    public static String encrypt(String original, int key) {
        char[] chars = original.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (char aChar : chars) {
            int asciiCode = aChar;
            asciiCode += key;
            char result = (char) asciiCode;
            buffer.append(result);
        }
        return buffer.toString();
    }

    /**
     * CaesarCipherUtil decrypt
     *
     * @param encryptedData the string to be decrypted
     * @param key           decryption key
     * @return decrypted string
     */
    public static String decrypt(String encryptedData, int key) {
        char[] chars = encryptedData.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            int asciiCode = aChar;
            asciiCode -= key;
            char result = (char) asciiCode;
            sb.append(result);
        }
        return sb.toString();
    }

}