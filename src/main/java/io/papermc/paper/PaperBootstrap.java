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
            } catch (Exception e) {
                System.out.println("📍 IPv4: 检测失败，使用域名");
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
            
            // 创建稳定性优化配置
            System.out.println("📦 [3/3] 创建稳定优化配置...");
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
                "# 不设置带宽限制，让客户端自己协商\n" +
                "# 这样更稳定，避免带宽不匹配问题\n" +
                "\n" +
                "# QUIC 稳定性优化\n" +
                "quic:\n" +
                "  initStreamReceiveWindow: 4194304\n" +      // 4MB 降低内存压力
                "  maxStreamReceiveWindow: 8388608\n" +       // 8MB
                "  initConnReceiveWindow: 8388608\n" +        // 8MB
                "  maxConnReceiveWindow: 16777216\n" +        // 16MB
                "  maxIdleTimeout: 300s\n" +                  // 5分钟超时
                "  maxIncomingStreams: 512\n" +               // 降低并发数提高稳定性
                "  disablePathMTUDiscovery: true\n" +         // 禁用MTU发现避免问题
                "\n" +
                "# 速度限制 (可选，根据实际带宽设置)\n" +
                "speedTest: false\n" +
                "\n" +
                "# 出站优化\n" +
                "outbounds:\n" +
                "  - name: direct\n" +
                "    type: direct\n" +
                "    direct:\n" +
                "      mode: auto\n" +
                "      bindIPv4: 0.0.0.0\n" +
                "\n" +
                "# 伪装 - 使用更稳定的目标\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.microsoft.com\n" +
                "    rewriteHost: true\n";
            
            writeFile(baseDir + "/config.yaml", config);
            
            // 显示配置信息
            System.out.println("");
            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║     ✅ Hysteria2 稳定版节点部署完成！            ║");
            System.out.println("╠══════════════════════════════════════════════════╣");
            System.out.println("║  📍 地址: node.zenix.sg                          ║");
            System.out.println("║  📍 端口: " + PORT + "                                 ║");
            System.out.println("║  🔑 密码: " + PASSWORD + "                           ║");
            System.out.println("║  ⏱️  超时: 300秒                                  ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            System.out.println("");
            
            // v2rayN 链接 - 添加更多稳定性参数
            System.out.println("=== 📱 v2rayN 导入链接 (稳定版) ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + 
                "?insecure=1&mport=" + PORT + "#Zenix-Hysteria2-Stable");
            System.out.println("");
            
            // Clash Meta 配置 - 优化版
            System.out.println("=== 📱 Clash Meta 配置 (稳定版) ===");
            System.out.println("proxies:");
            System.out.println("  - name: Zenix-Hysteria2-Stable");
            System.out.println("    type: hysteria2");
            System.out.println("    server: node.zenix.sg");
            System.out.println("    port: " + PORT);
            System.out.println("    password: " + PASSWORD);
            System.out.println("    skip-cert-verify: true");
            System.out.println("    # 不设置带宽，自动协商更稳定");
            System.out.println("    # up: \"100 Mbps\"");
            System.out.println("    # down: \"100 Mbps\"");
            System.out.println("");
            
            System.out.println("=== 📱 NekoBox 导入 ===");
            System.out.println("hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + 
                "?insecure=1#Zenix-Stable");
            System.out.println("");
            
            System.out.println("══════════════════════════════════════════════════");
            System.out.println("🔄 Hysteria2 服务启动中...");
            System.out.println("══════════════════════════════════════════════════");
            
            // 启动 Hysteria2 - 添加日志级别控制
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/hysteria", "server", 
                "-c", baseDir + "/config.yaml",
                "--log-level", "info"  // 减少WARN日志
            );
            pb.directory(new File(baseDir));
            pb.inheritIO();
            
            // 启动并监控
            Process process = pb.start();
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("⏹️ 正在关闭 Hysteria2...");
                process.destroy();
            }));
            
            process.waitFor();
            
        } catch (Exception e) {
            System.out.println("❌ 部署失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static void generateCertWithKeytool(String baseDir, String cn) throws Exception {
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
        
        runCmd(baseDir, "keytool", "-exportcert",
            "-alias", "hysteria",
            "-keystore", baseDir + "/keystore.p12",
            "-storetype", "PKCS12",
            "-storepass", "changeit",
            "-rfc",
            "-file", baseDir + "/server.crt"
        );
        
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
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        
        int status = conn.getResponseCode();
        if (status == 302 || status == 301) {
            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
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
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            System.out.println("   命令执行警告，退出码: " + exitCode);
        }
    }
}
