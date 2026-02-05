package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 正在配置 VLESS 高速节点...");
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
                downloadXray(xrayDir);
            } else {
                System.out.println("\n✓ Xray 已存在");
            }
            
            xrayFile.setExecutable(true);
            
            // 生成 TCP 配置（最快）
            System.out.println("📝 生成配置文件 (TCP模式)...");
            String config = "{\n" +
                "  \"log\": { \"loglevel\": \"warning\" },\n" +
                "  \"inbounds\": [{\n" +
                "    \"listen\": \"0.0.0.0\",\n" +
                "    \"port\": " + port + ",\n" +
                "    \"protocol\": \"vless\",\n" +
                "    \"settings\": {\n" +
                "      \"clients\": [{ \"id\": \"" + uuid + "\" }],\n" +
                "      \"decryption\": \"none\"\n" +
                "    },\n" +
                "    \"streamSettings\": { \"network\": \"tcp\" }\n" +
                "  }],\n" +
                "  \"outbounds\": [{ \"protocol\": \"freedom\" }]\n" +
                "}";
            
            try (FileWriter fw = new FileWriter(xrayDir + "/config.json")) {
                fw.write(config);
            }
            
            // 生成连接链接
            String vlessLink = "vless://" + uuid + "@" + host + ":" + port + "?encryption=none&security=none&type=tcp#HighSpeed-TCP";
            
            System.out.println("\n========================================");
            System.out.println("✅ 高速节点配置完成!");
            System.out.println("========================================");
            System.out.println("\n📱 连接链接 (复制到 V2rayN / Shadowrocket):\n");
            System.out.println(vlessLink);
            System.out.println("\n========================================");
            System.out.println("📋 手动配置:");
            System.out.println("   协议: VLESS");
            System.out.println("   地址: " + host);
            System.out.println("   端口: " + port);
            System.out.println("   UUID: " + uuid);
            System.out.println("   传输: tcp");
            System.out.println("   加密: none");
            System.out.println("   TLS: 关闭");
            System.out.println("========================================\n");
            
            // 启动
            System.out.println("🚀 启动高速节点...\n");
            ProcessBuilder xrayPb = new ProcessBuilder(xrayDir + "/xray", "run", "-c", xrayDir + "/config.json");
            xrayPb.directory(new File(xrayDir));
            xrayPb.inheritIO();
            xrayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void downloadXray(String xrayDir) throws Exception {
        String zipPath = xrayDir + "/xray.zip";
        
        ProcessBuilder curlPb = new ProcessBuilder(
            "curl", "-L", "-o", zipPath,
            "https://github.com/XTLS/Xray-core/releases/download/v1.8.24/Xray-linux-64.zip"
        );
        curlPb.inheritIO();
        curlPb.start().waitFor();
        
        // Java 解压
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(zipPath))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(xrayDir, entry.getName());
                if (!entry.isDirectory()) {
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
        new File(zipPath).delete();
    }
}
