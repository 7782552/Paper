package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int PORT = 30194;
        String PASSWORD = "zenix2024";
        
        try {
            System.out.println("🚀 部署 Hysteria2 稳定版节点...");
            System.out.println("");
            
            // 检测服务器 IP
            System.out.println("🔍 检测服务器网络...");
            String serverIP = "node.zenix.sg";
            String detectedIP = "";
            try {
                URL ipv4 = new URL("https://api.ipify.org");
                HttpURLConnection conn = (HttpURLConnection) ipv4.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                BufferedReader r4 = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                detectedIP = r4.readLine();
                System.out.println("📍 IPv4: " + detectedIP);
                r4.close();
            } catch (Exception e) {
                System.out.println("📍 IPv4: 检测失败");
            }
            System.out.println("");
            
            // 下载 Hysteria2
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
                    System.out.println("   使用 keytool...");
                    generateCertWithKeytool(baseDir, serverIP);
                }
            } else {
                System.out.println("📦 [2/3] 证书已存在 ✓");
            }
            
            // 创建稳定版配置
            System.out.println("📦 [3/3] 创建稳定版配置...");
            
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
                "# 保守带宽设置，提高稳定性\n" +
                "bandwidth:\n" +
                "  up: 50 mbps\n" +
                "  down: 100 mbps\n" +
                "\n" +
                "# 稳定性优化配置\n" +
                "quic:\n" +
                "  initStreamReceiveWindow: 4194304\n" +
                "  maxStreamReceiveWindow: 8388608\n" +
                "  initConnReceiveWindow: 8388608\n" +
                "  maxConnReceiveWindow: 16777216\n" +
                "  maxIdleTimeout: 120s\n" +
                "  maxIncomingStreams: 256\n" +
                "  disablePathMTUDiscovery: true\n" +
                "\n" +
                "# 忽略客户端带宽设置\n" +
                "ignoreClientBandwidth: true\n" +
                "\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.bing.com\n" +
                "    rewriteHost: true\n";
            
            writeFile(baseDir + "/config.yaml", config);
            
            // 显示信息
            System.out.println("");
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║     ✅ Hysteria2 稳定版节点部署完成！                ║");
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║  📍 地址: node.zenix.sg                              ║");
            System.out.println("║  📍 端口: " + PORT + "                                     ║");
            System.out.println("║  🔑 密码: " + PASSWORD + "                               ║");
            System.out.println("║  🚄 带宽: ↑50 / ↓100 Mbps（稳定模式）                ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
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
            System.out.println("    up: \"50 Mbps\"");
            System.out.println("    down: \"100 Mbps\"");
            System.out.println("");
            System.out.println("rules:");
            System.out.println("  - DOMAIN-SUFFIX,zenix.sg,DIRECT");
            System.out.println("  - MATCH,Zenix-Hysteria2");
            System.out.println("");
            
            System.out.println("=== 📱 NekoBox 导入 ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1#Zenix-Hysteria2");
            System.out.println("");
            
            System.out.println("══════════════════════════════════════════════════════");
            System.out.println("🔄 Hysteria2 服务启动中...");
            System.out.println("══════════════════════════════════════════════════════");
            System.out.println("");
            
            // 启动
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/hysteria", "server", 
                "-c", baseDir + "/config.yaml"
            );
            pb.directory(new File(baseDir));
            pb.inheritIO();
            
            Process process = pb.start();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n⏹️ 正在关闭 Hysteria2...");
                process.destroy();
            }));
            
            process.waitFor();
            
        } catch (Exception e) {
            System.out.println("❌ 部署失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static void generateCertWithKeytool(String baseDir, String cn) throws Exception {
        new File(baseDir + "/keystore.p12").delete();
        
        runCmd(baseDir, "keytool", "-genkeypair",
            "-alias", "hysteria", "-keyalg", "RSA", "-keysize", "2048",
            "-validity", "3650", "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12", "-storepass", "changeit",
            "-keypass", "changeit", "-dname", "CN=" + cn
        );
        
        runCmd(baseDir, "keytool", "-exportcert",
            "-alias", "hysteria", "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12", "-storepass", "changeit",
            "-rfc", "-file", baseDir + "/server.crt"
        );
        
        runCmd(baseDir, "openssl", "pkcs12",
            "-in", baseDir + "/keystore.p12", "-nocerts", "-nodes",
            "-out", baseDir + "/server.key", "-passin", "pass:changeit"
        );
    }
    
    static void downloadFile(String urlStr, String dest) throws Exception {
        System.out.println("   下载: " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        
        int status = conn.getResponseCode();
        if (status == 302 || status == 301) {
            conn = (HttpURLConnection) new URL(conn.getHeaderField("Location")).openConnection();
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
        try (FileWriter w = new FileWriter(path)) { w.write(content); }
    }
    
    static void runCmd(String dir, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
