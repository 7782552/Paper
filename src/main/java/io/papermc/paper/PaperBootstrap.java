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
            
            String host = "node.zenix.sg";
            int port = 30194;
            String uuid = "195120f0-5bb6-487d-8a91-17ac122f529c";
            
            System.out.println("📋 VLESS 配置信息:");
            System.out.println("   地址: " + host);
            System.out.println("   端口: " + port);
            System.out.println("   UUID: " + uuid);
            
            new File(xrayDir).mkdirs();
            
            File xrayFile = new File(xrayDir + "/xray");
            if (!xrayFile.exists()) {
                System.out.println("\n📦 下载 Xray...");
                downloadAndExtract(xrayDir);
            } else {
                System.out.println("\n✓ Xray 已存在，大小: " + xrayFile.length() + " bytes");
            }
            
            // 设置执行权限
            xrayFile.setExecutable(true);
            System.out.println("✓ 设置执行权限");
            
            // 检查文件
            System.out.println("✓ Xray 文件存在: " + xrayFile.exists());
            System.out.println("✓ Xray 可执行: " + xrayFile.canExecute());
            
            // 生成配置
            System.out.println("📝 生成配置文件...");
            generateConfig(xrayDir, port, uuid, host);
            
            // 测试 xray 版本
            System.out.println("\n🔍 测试 Xray...");
            ProcessBuilder testPb = new ProcessBuilder(xrayDir + "/xray", "version");
            testPb.directory(new File(xrayDir));
            testPb.inheritIO();
            int testCode = testPb.start().waitFor();
            System.out.println("   版本检测退出码: " + testCode);
            
            // 启动 Xray
            System.out.println("\n🚀 启动 VLESS 服务 (端口 " + port + ")...");
            System.out.println("   如果看到 'Xray started' 就表示成功\n");
            
            ProcessBuilder xrayPb = new ProcessBuilder(xrayDir + "/xray", "run", "-c", xrayDir + "/config.json");
            xrayPb.directory(new File(xrayDir));
            xrayPb.inheritIO();
            xrayPb.start().waitFor();

        } catch (Exception e) {
            System.out.println("❌ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void downloadAndExtract(String xrayDir) throws Exception {
        String zipPath = xrayDir + "/xray.zip";
        String xrayUrl = "https://github.com/XTLS/Xray-core/releases/download/v1.8.24/Xray-linux-64.zip";
        
        System.out.println("   下载: " + xrayUrl);
        
        // 使用 curl 下载（更可靠）
        ProcessBuilder curlPb = new ProcessBuilder(
            "curl", "-L", "-o", zipPath, xrayUrl
        );
        curlPb.inheritIO();
        int curlCode = curlPb.start().waitFor();
        
        if (curlCode != 0 || !new File(zipPath).exists()) {
            throw new Exception("下载失败");
        }
        
        System.out.println("   下载完成，开始解压...");
        
        // Java 解压
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                File outFile = new File(xrayDir, name);
                
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
                    System.out.println("   解压: " + name);
                }
                zis.closeEntry();
            }
        }
        
        new File(zipPath).delete();
        System.out.println("   解压完成");
    }

    static void generateConfig(String xrayDir, int port, String uuid, String host) throws Exception {
        String config = "{\n" +
            "  \"log\": {\n" +
            "    \"loglevel\": \"warning\"\n" +
            "  },\n" +
            "  \"inbounds\": [\n" +
            "    {\n" +
            "      \"listen\": \"0.0.0.0\",\n" +
            "      \"port\": " + port + ",\n" +
            "      \"protocol\": \"vless\",\n" +
            "      \"settings\": {\n" +
            "        \"clients\": [\n" +
            "          {\n" +
            "            \"id\": \"" + uuid + "\"\n" +
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
            "      \"protocol\": \"freedom\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        try (FileWriter fw = new FileWriter(xrayDir + "/config.json")) {
            fw.write(config);
        }
        
        String vlessLink = "vless://" + uuid + "@" + host + ":" + port + "?encryption=none&type=ws&path=%2Fvless#VLESS-WS";
        
        System.out.println("\n========================================");
        System.out.println("📱 连接链接:");
        System.out.println(vlessLink);
        System.out.println("========================================\n");
    }
}
