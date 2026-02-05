package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 正在配置 VLESS 节点...");
        try {
            String baseDir = "/home/container";
            String xrayDir = baseDir + "/xray";
            
            // 配置参数
            String host = "node.zenix.sg";
            int port = 30194;
            String uuid = "195120f0-5bb6-487d-8a91-17ac122f529c";  // 固定 UUID
            
            System.out.println("📋 VLESS 配置信息:");
            System.out.println("   地址: " + host);
            System.out.println("   端口: " + port);
            System.out.println("   UUID: " + uuid);
            
            // 创建目录
            new File(xrayDir).mkdirs();
            
            // 检查 xray 是否已存在
            File xrayFile = new File(xrayDir + "/xray");
            if (!xrayFile.exists()) {
                System.out.println("\n📦 下载 Xray...");
                downloadAndExtract(xrayDir);
            } else {
                System.out.println("\n✓ Xray 已存在");
            }
            
            // 设置执行权限
            xrayFile.setExecutable(true);
            
            // 生成配置文件
            System.out.println("📝 生成配置文件...");
            generateConfig(xrayDir, port, uuid, host);
            
            // 启动 Xray
            System.out.println("🚀 启动 VLESS 服务...");
            ProcessBuilder xrayPb = new ProcessBuilder(xrayDir + "/xray", "run", "-c", xrayDir + "/config.json");
            xrayPb.directory(new File(xrayDir));
            xrayPb.inheritIO();
            xrayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void downloadAndExtract(String xrayDir) throws Exception {
        String zipPath = xrayDir + "/xray.zip";
        String xrayUrl = "https://github.com/XTLS/Xray-core/releases/download/v1.8.24/Xray-linux-64.zip";
        
        // 下载
        System.out.println("   下载中...");
        URL url = new URL(xrayUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        
        // 处理重定向
        int status = conn.getResponseCode();
        if (status == 302 || status == 301) {
            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
        }
        
        try (InputStream in = conn.getInputStream();
             FileOutputStream fos = new FileOutputStream(zipPath)) {
            byte[] buffer = new byte[8192];
            int len;
            long total = 0;
            while ((len = in.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                total += len;
            }
            System.out.println("   下载完成: " + (total / 1024 / 1024) + " MB");
        }
        
        // 解压
        System.out.println("   解压中...");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(xrayDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
        
        // 删除 zip
        new File(zipPath).delete();
        System.out.println("   解压完成");
    }

    static void generateConfig(String xrayDir, int port, String uuid, String host) throws Exception {
        String config = "{\n" +
            "  \"log\": {\n" +
            "    \"loglevel\": \"info\"\n" +
            "  },\n" +
            "  \"inbounds\": [\n" +
            "    {\n" +
            "      \"port\": " + port + ",\n" +
            "      \"protocol\": \"vless\",\n" +
            "      \"settings\": {\n" +
            "        \"clients\": [\n" +
            "          {\n" +
            "            \"id\": \"" + uuid + "\",\n" +
            "            \"level\": 0\n" +
            "          }\n" +
            "        ],\n" +
            "        \"decryption\": \"none\"\n" +
            "      },\n" +
            "      \"streamSettings\": {\n" +
            "        \"network\": \"ws\",\n" +
            "        \"wsSettings\": {\n" +
            "          \"path\": \"/vless\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"outbounds\": [\n" +
            "    {\n" +
            "      \"protocol\": \"freedom\",\n" +
            "      \"tag\": \"direct\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        try (FileWriter fw = new FileWriter(xrayDir + "/config.json")) {
            fw.write(config);
        }
        
        // 生成连接链接
        String vlessLink = "vless://" + uuid + "@" + host + ":" + port + "?encryption=none&type=ws&path=%2Fvless#Pterodactyl-VLESS";
        
        System.out.println("\n✅ VLESS 节点配置完成!");
        System.out.println("========================================");
        System.out.println("📱 连接链接 (复制到客户端):");
        System.out.println(vlessLink);
        System.out.println("========================================");
        System.out.println("📋 手动配置:");
        System.out.println("   协议: VLESS");
        System.out.println("   地址: " + host);
        System.out.println("   端口: " + port);
        System.out.println("   UUID: " + uuid);
        System.out.println("   传输: WebSocket");
        System.out.println("   路径: /vless");
        System.out.println("   加密: none");
        System.out.println("========================================\n");
    }
}
