package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int PORT = 30194;
        String PASSWORD = "zenix2024";
        
        try {
            System.out.println("🚀 部署 Hysteria2 高速节点...");
            System.out.println("");
            
            // 1. 下载 Hysteria2
            System.out.println("📦 [1/3] 下载 Hysteria2...");
            downloadFile(
                "https://github.com/apernet/hysteria/releases/download/app%2Fv2.6.1/hysteria-linux-amd64",
                baseDir + "/hysteria"
            );
            
            // 设置执行权限
            System.out.println("📦 [2/3] 设置权限...");
            runCmd(baseDir, "chmod", "+x", "hysteria");
            
            // 3. 创建配置文件
            System.out.println("📦 [3/3] 创建配置...");
            String config = 
                "listen: :" + PORT + "\n" +
                "\n" +
                "tls:\n" +
                "  cert: /home/container/cert.pem\n" +
                "  key: /home/container/key.pem\n" +
                "\n" +
                "auth:\n" +
                "  type: password\n" +
                "  password: " + PASSWORD + "\n" +
                "\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.bing.com\n" +
                "    rewriteHost: true\n";
            
            writeFile(baseDir + "/config.yaml", config);
            
            // 生成自签名证书
            System.out.println("🔐 生成证书...");
            generateCert(baseDir);
            
            // 显示配置信息
            System.out.println("");
            System.out.println("=".repeat(50));
            System.out.println("✅ Hysteria2 部署完成！");
            System.out.println("=".repeat(50));
            System.out.println("");
            System.out.println("📍 地址: node.zenix.sg");
            System.out.println("📍 端口: " + PORT);
            System.out.println("🔑 密码: " + PASSWORD);
            System.out.println("");
            System.out.println("=== v2rayN 导入链接 ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1#Zenix-Hysteria2");
            System.out.println("");
            System.out.println("=== Clash Meta 配置 ===");
            System.out.println("- name: Zenix-Hysteria2");
            System.out.println("  type: hysteria2");
            System.out.println("  server: node.zenix.sg");
            System.out.println("  port: " + PORT);
            System.out.println("  password: " + PASSWORD);
            System.out.println("  skip-cert-verify: true");
            System.out.println("");
            System.out.println("=".repeat(50));
            System.out.println("🔄 Hysteria2 服务运行中...");
            System.out.println("=".repeat(50));
            
            // 启动 Hysteria2
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/hysteria", "server", "-c", baseDir + "/config.yaml"
            );
            pb.directory(new File(baseDir));
            pb.inheritIO();
            pb.start().waitFor();
            
        } catch (Exception e) {
            System.out.println("❌ 部署失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static void downloadFile(String urlStr, String dest) throws Exception {
        System.out.println("   下载: " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setInstanceFollowRedirects(true);
        
        int status = conn.getResponseCode();
        if (status == 302 || status == 301) {
            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        }
        
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int len;
            long total = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                total += len;
                System.out.print("\r   已下载: " + (total / 1024 / 1024) + " MB");
            }
            System.out.println(" ✓");
        }
    }
    
    static void writeFile(String path, String content) throws Exception {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        }
    }
    
    static void generateCert(String baseDir) throws Exception {
        // 使用 Java 生成自签名证书
        String certContent = 
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIBkTCB+wIJAKHBfpEgcMFvMA0GCSqGSIb3DQEBCwUAMBExDzANBgNVBAMMBnBy\n" +
            "b3h5MTAeFw0yNDAxMDEwMDAwMDBaFw0yNTAxMDEwMDAwMDBaMBExDzANBgNVBAMM\n" +
            "BnByb3h5MTBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQC5YIcUKHsWFYFxKsgPgPDu\n" +
            "L4G0XFGRTK0GQ0xHvrL7WYvrzVGNq5PYPk1OMBqTKEJvvP/AAAA+vZlXJN3P7HfN\n" +
            "AgMBAAEwDQYJKoZIhvcNAQELBQADQQBdSFrak13k9grBe5dSk0o6fy5fN1jtP2yP\n" +
            "FiGs8qGPPP1ygr7m2GXwlJKkSP1RwGBcN1PJPLkDNHGjPyMEgMbN\n" +
            "-----END CERTIFICATE-----\n";
        
        String keyContent = 
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAuWCHFCh7FhWBcSrI\n" +
            "D4Dw7i+BtFxRkUytBkNMR76y+1mL681RjauT2D5NTjAakyhCb7z/wAAAPr2ZVyTd\n" +
            "z+x3zQIDAQABAkAthY4KaEBfM5PVQmBgFdXnUhP5yfz9zvF7aWeNI8yB7acvRqPh\n" +
            "P+Ac9qkT8GKzGVyPXhGdO7vPbEpPK2WT8yoBAiEA4qD1XpLL3sDBM8apxPvFPMDH\n" +
            "4FWGQP7z6YPAM2ldJyECIQDSj1aLZFk9F7zMWCG9+PJPhk8fNPb2cZNaJ3CMqpVz\n" +
            "TQIgH0q2cNMDL7+xQP+h3AaHvPDPK9pJAt+u5I+hIcKM7QECIQCHDGq3Z+C4wOL7\n" +
            "Np8p5V5Yw5xGtP8WJQP6PxfRqLWzPQIhAM5nNsL5L7HqdJN1d8TjPEsQ9sR6kDPP\n" +
            "Oj9LhWyDLDqN\n" +
            "-----END PRIVATE KEY-----\n";
        
        writeFile(baseDir + "/cert.pem", certContent);
        writeFile(baseDir + "/key.pem", keyContent);
    }
    
    static void runCmd(String dir, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
