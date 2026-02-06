package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int PORT = 30194;
        String PASSWORD = "zenix2024";
        
        try {
            System.out.println("🚀 部署 Hysteria2 高速节点（优化版）...");
            System.out.println("");
            
            // 检测服务器 IP
            System.out.println("🔍 检测服务器网络...");
            String serverIP = "node.zenix.sg";
            try {
                URL ipv4 = new URL("https://api.ipify.org");
                BufferedReader r4 = new BufferedReader(new InputStreamReader(ipv4.openStream()));
                String ip = r4.readLine();
                System.out.println("📍 IPv4: " + ip);
            } catch (Exception e) {
                System.out.println("📍 IPv4: 使用域名");
            }
            System.out.println("");
            
            // 检查是否已下载
            File hysteria = new File(baseDir + "/hysteria");
            if (!hysteria.exists()) {
                System.out.println("📦 [1/3] 下载 Hysteria2...");
                downloadFile(
                    "https://github.com/apernet/hysteria/releases/download/app%2Fv2.6.1/hysteria-linux-amd64",
                    baseDir + "/hysteria"
                );
                runCmd(baseDir, "chmod", "+x", "hysteria");
            } else {
                System.out.println("📦 [1/3] Hysteria2 已存在 ✓");
            }
            
            // 生成证书
            File cert = new File(baseDir + "/server.crt");
            if (!cert.exists()) {
                System.out.println("📦 [2/3] 生成证书...");
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                        "openssl", "req", "-x509", "-nodes", "-newkey", "rsa:2048",
                        "-keyout", baseDir + "/server.key",
                        "-out", baseDir + "/server.crt",
                        "-days", "3650",
                        "-subj", "/CN=" + serverIP
                    );
                    pb.directory(new File(baseDir));
                    pb.inheritIO();
                    pb.start().waitFor();
                    System.out.println("   证书生成成功 ✓");
                } catch (Exception e) {
                    System.out.println("   使用 keytool 生成证书...");
                    generateCertWithKeytool(baseDir, serverIP);
                }
            } else {
                System.out.println("📦 [2/3] 证书已存在 ✓");
            }
            
            // 创建优化配置
            System.out.println("📦 [3/3] 创建优化配置...");
            String config = 
                "listen: :" + PORT + "\n" +
                "\n" +
                "tls:\n" +
                "  cert: /home/container/server.crt\n" +
                "  key: /home/container/server.key\n" +
                "\n" +
                "auth:\n" +
                "  type: password\n" +
                "  password: " + PASSWORD + "\n" +
                "\n" +
                "# 速度优化\n" +
                "bandwidth:\n" +
                "  up: 200 mbps\n" +
                "  down: 200 mbps\n" +
                "\n" +
                "# QUIC 优化\n" +
                "quic:\n" +
                "  initStreamReceiveWindow: 8388608\n" +
                "  maxStreamReceiveWindow: 8388608\n" +
                "  initConnReceiveWindow: 20971520\n" +
                "  maxConnReceiveWindow: 20971520\n" +
                "  maxIdleTimeout: 60s\n" +
                "  maxIncomingStreams: 1024\n" +
                "  disablePathMTUDiscovery: false\n" +
                "\n" +
                "# 伪装\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.bing.com\n" +
                "    rewriteHost: true\n";
            
            writeFile(baseDir + "/config.yaml", config);
            
            // 显示配置信息
            System.out.println("");
            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║     ✅ Hysteria2 高速节点部署完成！              ║");
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.println("║  📍 地址: node.zenix.sg                          ║");
            System.out.println("║  📍 端口: " + PORT + "                                 ║");
            System.out.println("║  🔑 密码: " + PASSWORD + "                           ║");
            System.out.println("║  🚄 带宽: 200 Mbps                               ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            System.out.println("");
            System.out.println("=== 📱 v2rayN 导入链接 ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1#Zenix-Hysteria2");
            System.out.println("");
            System.out.println("=== 📱 Clash Meta 配置 ===");
            System.out.println("proxies:");
            System.out.println("  - name: Zenix-Hysteria2");
            System.out.println("    type: hysteria2");
            System.out.println("    server: node.zenix.sg");
            System.out.println("    port: " + PORT);
            System.out.println("    password: " + PASSWORD);
            System.out.println("    skip-cert-verify: true");
            System.out.println("    up: \"200 Mbps\"");
            System.out.println("    down: \"200 Mbps\"");
            System.out.println("");
            System.out.println("=== 📱 NekoBox/Matsuri 导入 ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1#Zenix-Hysteria2");
            System.out.println("");
            System.out.println("══════════════════════════════════════════════════");
            System.out.println("🔄 Hysteria2 服务运行中...");
            System.out.println("══════════════════════════════════════════════════");
            
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
    
    static void generateCertWithKeytool(String baseDir, String cn) throws Exception {
        // 生成 keystore
        runCmd(baseDir, "keytool", "-genkeypair",
            "-alias", "hysteria",
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "3650",
            "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12",
            "-storepass", "changeit",
            "-keypass", "changeit",
            "-dname", "CN=" + cn
        );
        
        // 导出证书
        runCmd(baseDir, "keytool", "-exportcert",
            "-alias", "hysteria",
            "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12",
            "-storepass", "changeit",
            "-rfc",
            "-file", baseDir + "/server.crt"
        );
        
        // 导出私钥
        runCmd(baseDir, "openssl", "pkcs12",
            "-in", baseDir + "/keystore.p12",
            "-nocerts", "-nodes",
            "-out", baseDir + "/server.key",
            "-passin", "pass:changeit"
        );
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
    
    static void runCmd(String dir, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
