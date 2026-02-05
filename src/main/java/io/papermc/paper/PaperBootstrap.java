package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 正在配置 VMess 高速节点...");
        try {
            String baseDir = "/home/container";
            String xrayDir = baseDir + "/xray";
            
            String host = "node.zenix.sg";
            int port = 30194;
            String uuid = "195120f0-5bb6-487d-8a91-17ac122f529c";
            
            new File(xrayDir).mkdirs();
            File xrayFile = new File(xrayDir + "/xray");
            
            if (!xrayFile.exists()) {
                System.out.println("📦 下载 Xray...");
                ProcessBuilder curlPb = new ProcessBuilder(
                    "curl", "-L", "-o", xrayDir + "/xray.zip",
                    "https://github.com/XTLS/Xray-core/releases/download/v1.8.24/Xray-linux-64.zip"
                );
                curlPb.inheritIO();
                curlPb.start().waitFor();
                
                try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(xrayDir + "/xray.zip"))) {
                    java.util.zip.ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            File outFile = new File(xrayDir, entry.getName());
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
                new File(xrayDir + "/xray.zip").delete();
            }
            
            xrayFile.setExecutable(true);
            
            // VMess 配置
            String config = "{\n" +
                "  \"log\": { \"loglevel\": \"warning\" },\n" +
                "  \"inbounds\": [{\n" +
                "    \"listen\": \"0.0.0.0\",\n" +
                "    \"port\": " + port + ",\n" +
                "    \"protocol\": \"vmess\",\n" +
                "    \"settings\": {\n" +
                "      \"clients\": [{\n" +
                "        \"id\": \"" + uuid + "\",\n" +
                "        \"alterId\": 0\n" +
                "      }]\n" +
                "    },\n" +
                "    \"streamSettings\": { \"network\": \"tcp\" }\n" +
                "  }],\n" +
                "  \"outbounds\": [{ \"protocol\": \"freedom\" }]\n" +
                "}";
            
            try (FileWriter fw = new FileWriter(xrayDir + "/config.json")) {
                fw.write(config);
            }
            
            // VMess 链接（Base64 格式）
            String vmessJson = "{\"v\":\"2\",\"ps\":\"HighSpeed-VMess\",\"add\":\"" + host + "\",\"port\":\"" + port + "\",\"id\":\"" + uuid + "\",\"aid\":\"0\",\"net\":\"tcp\",\"type\":\"none\",\"tls\":\"\"}";
            String vmessLink = "vmess://" + Base64.getEncoder().encodeToString(vmessJson.getBytes());
            
            System.out.println("\n========================================");
            System.out.println("✅ VMess 高速节点配置完成!");
            System.out.println("========================================");
            System.out.println("\n📱 连接链接:\n");
            System.out.println(vmessLink);
            System.out.println("\n========================================");
            System.out.println("📋 手动配置:");
            System.out.println("   协议: VMess");
            System.out.println("   地址: " + host);
            System.out.println("   端口: " + port);
            System.out.println("   UUID: " + uuid);
            System.out.println("   alterId: 0");
            System.out.println("   传输: tcp");
            System.out.println("   加密: auto");
            System.out.println("========================================\n");
            
            System.out.println("🚀 启动 VMess 节点...\n");
            ProcessBuilder xrayPb = new ProcessBuilder(xrayDir + "/xray", "run", "-c", xrayDir + "/config.json");
            xrayPb.directory(new File(xrayDir));
            xrayPb.inheritIO();
            xrayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
