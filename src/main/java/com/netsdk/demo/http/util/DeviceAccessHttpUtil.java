package com.netsdk.demo.http.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备HTTP访问工具类：
 * <ul>
 *   <li>发送时自动填充设备相关请求头</li>
 *   <li>接收时解析并校验请求头，防止重放、参数篡改、时间戳过期等安全风险</li>
 *   <li>提供AES加密/解密工具，保护敏感信息</li>
 *   <li>支持配置化密钥和时间戳有效期，便于运维和安全加固</li>
 * </ul>
 * <p>
 * 主要头部：
 *   Dh-Device-Ip、Dh-Device-User、Dh-Device-Password、Dh-Device-Timestamp、Dh-Device-Port
 * <p>
 * 推荐通过Spring注入本工具类，避免静态方法带来的配置注入问题。
 */
@Component
public class DeviceAccessHttpUtil { 
    /**
     * AES加密密钥，长度16位。可通过配置覆盖，避免硬编码泄露。
     */
    private static String AES_KEY = "NetSDK1234567890"; // 16位密钥
    @Value("${crypto.aes-key:NetSDK1234567890}")
    public void setAesKey(String aesKey) {
        AES_KEY = aesKey;
    }

    /**
     * 设备请求时间戳有效期（毫秒），默认5分钟。可通过配置覆盖。
     * 注意：内部会转换为微秒进行比较，以支持微秒级时间戳精度。
     */
    private static long TIMESTAMP_EXPIRE_MS;
    @Value("${device.timestamp.expire-ms:300000}")
    public void setTimestampExpireMs(long timestampExpireMs) {
        TIMESTAMP_EXPIRE_MS = timestampExpireMs;
    }

    private static final String ALGORITHM = "AES";

    // 设备 HTTP 头部 key 常量，便于全局复用和维护
    public static final String HEADER_DEVICE_IP = "Dh-Device-Ip";
    public static final String HEADER_DEVICE_USER = "Dh-Device-User";
    public static final String HEADER_DEVICE_PASSWORD = "Dh-Device-Password";
    public static final String HEADER_DEVICE_TIMESTAMP = "Dh-Device-Timestamp";
    public static final String HEADER_DEVICE_PORT = "Dh-Device-Port";

    /**
     * 防重放 nonce 集合及最大容量，防止同一加密串被多次使用。
     */
    private static final int NONCE_SET_MAX_SIZE = 10000;
    private static final java.util.Set<String> usedNonceSet = java.util.Collections.synchronizedSet(new java.util.LinkedHashSet<>());

    /**
     * 构造设备访问请求头，自动加密密码并填充所有必需字段。
     * 使用方式：获取返回结果，遍历map kv，直接填充到请求头。
     * @param ip 设备IP
     * @param user 用户名
     * @param password 明文密码
     * @param timestamp 时间戳（建议用System.nanoTime()获取微秒级时间戳）
     * @param port 端口号
     * @return 设备请求头Map
     * @throws Exception 加密异常
     */
    public static Map<String, String> buildDeviceHeaders(String ip, String user, String password, String timestamp, String port) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_DEVICE_IP, ip);
        headers.put(HEADER_DEVICE_USER, user);
        headers.put(HEADER_DEVICE_TIMESTAMP, timestamp);
        headers.put(HEADER_DEVICE_PORT, port);
        // 密码加密（password|timestamp）
        String encPwd = encryptWithTimestamp(password, timestamp);
        headers.put(HEADER_DEVICE_PASSWORD, encPwd);
        return headers;
    }

    /**
     * 解析并校验设备请求头，返回解析结果DeviceAccessInfo。
     * 校验项：参数完整性、密码解密、时间戳一致性、防重放、端口校验。
     * 校验失败抛异常。
     * @param request HttpServletRequest
     * @return DeviceAccessInfo（ip、user、password、timestamp、port）
     * @throws IllegalArgumentException 校验或解密异常
     */
    public static DeviceAccessInfo parseAndValidateDeviceHeaders(javax.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader(HEADER_DEVICE_IP);
        String encPwd = request.getHeader(HEADER_DEVICE_PASSWORD);
        String user = request.getHeader(HEADER_DEVICE_USER);
        String ts = request.getHeader(HEADER_DEVICE_TIMESTAMP);
        String portStr = request.getHeader(HEADER_DEVICE_PORT);
        if (user == null || user.isEmpty()) user = "admin";
        return parseAndValidateDeviceHeaders(ip, encPwd, user, ts, portStr);
    }

    /**
     * 统一参数校验和解密逻辑，失败抛异常，成功返回DeviceAccessInfo。
     * <ul>
     *   <li>参数完整性校验</li>
     *   <li>重放攻击防护（同一加密串只允许用一次）</li>
     *   <li>密码解密与格式校验</li>
     *   <li>时间戳一致性与有效期校验</li>
     *   <li>端口号格式校验</li>
     * </ul>
     * @param ip 设备IP
     * @param encPwd 加密密码
     * @param user 用户名
     * @param ts 时间戳
     * @param portStr 端口字符串
     * @return DeviceAccessInfo 校验通过的设备信息
     * @throws IllegalArgumentException 校验失败
     */
    public static DeviceAccessInfo parseAndValidateDeviceHeaders(String ip, String encPwd, String user, String ts, String portStr) {
        if (ip == null || encPwd == null || ts == null) {
            throw new IllegalArgumentException("缺少设备登录信息: ip, encPwd, ts");
        }
        String nonce = encPwd;
        synchronized (usedNonceSet) {
            if (usedNonceSet.contains(nonce)) {
                throw new IllegalArgumentException("请求重放: nonce=" + nonce);
            }
            usedNonceSet.add(nonce);
            if (usedNonceSet.size() > NONCE_SET_MAX_SIZE) {
                String first = usedNonceSet.iterator().next();
                usedNonceSet.remove(first);
            }
        }
        String[] pwdTsNonce;
        try {
            pwdTsNonce = decryptWithTimestamp(encPwd);
        } catch (Exception e) {
            throw new IllegalArgumentException("密码解密异常: " + e.getMessage(), e);
        }
        if (pwdTsNonce.length != 2) {
            throw new IllegalArgumentException("密码解密格式错误: encPwd=" + encPwd);
        }
        String pwd = pwdTsNonce[0];
        String tsInPwd = pwdTsNonce[1];
        if (!ts.equals(tsInPwd)) {
            throw new IllegalArgumentException("时间戳校验失败: ts=" + ts + ", tsInPwd=" + tsInPwd);
        }
        long now = System.nanoTime();
        long tsLong;
        try { tsLong = Long.parseLong(ts); } catch (Exception e) {
            throw new IllegalArgumentException("时间戳格式错误: ts=" + ts);
        }
        if (Math.abs(now - tsLong) > TIMESTAMP_EXPIRE_MS * 1000) { // 转换为微秒进行比较
            throw new IllegalArgumentException("登录请求已过期: now=" + now + ", tsLong=" + tsLong + ", expireMs=" + TIMESTAMP_EXPIRE_MS);
        }
        int port = 37777;
        if (portStr != null && !portStr.isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Device-Port格式错误: portStr=" + portStr);
            }
        }
        return new DeviceAccessInfo(ip, user, pwd, ts, port);
    }

    /**
     * AES加密（ECB模式，PKCS5Padding），用于加密敏感数据。
     * @param data 明文
     * @return base64编码的密文
     * @throws Exception 加密异常
     */
    private static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES解密（ECB模式，PKCS5Padding），用于解密敏感数据。
     * @param encryptedData base64编码的密文
     * @return 明文
     * @throws Exception 解密异常
     */
    private static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, "UTF-8");
    }

    /**
     * 生成加密字符串：password|timestamp
     * @param password 明文密码
     * @param timestamp 时间戳
     * @return 加密后字符串
     * @throws Exception 加密异常
     */
    private static String encryptWithTimestamp(String password, String timestamp) throws Exception {
        return encrypt(password + "|" + timestamp);
    }

    /**
     * 解密并返回[password, timestamp]
     * @param encryptedData 加密后字符串
     * @return [password, timestamp]
     * @throws Exception 解密异常
     */
    private static String[] decryptWithTimestamp(String encryptedData) throws Exception {
        String plain = decrypt(encryptedData);
        return plain.split("\\|", 2);
    }

    
    /**
     * 设备访问信息结构体，封装所有校验通过的关键信息。
     * 用于业务层安全登录、日志审计等场景。
     */
    public static class DeviceAccessInfo {
        /** 设备IP */
        public final String ip;
        /** 用户名 */
        public final String user;
        /** 明文密码（已解密） */
        public final String password;
        /** 时间戳 */
        public final String timestamp;
        /** 端口号 */
        public final int port;
        public DeviceAccessInfo(String ip, String user, String password, String timestamp, int port) {
            this.ip = ip;
            this.user = user;
            this.password = password;
            this.timestamp = timestamp;
            this.port = port;
        }
    }

    /**
     * 本地测试主方法，演示加密解密流程。
     */
    public static void main(String[] args) throws Exception {
        String password = "admin";
        String timestamp = String.valueOf(System.nanoTime());
        String encrypted = encryptWithTimestamp(password, timestamp);
        System.out.println("加密后: " + encrypted);
        String[] decrypted = decryptWithTimestamp(encrypted);
        System.out.println("解密后 password: " + decrypted[0]);
        System.out.println("解密后 timestamp: " + decrypted[1]);
    }
}