package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int PORT = 30194;
        String PASSWORD = "zenix2024";
        
        try {
            System.out.println("🚀 部署双协议高速节点...");
            System.out.println("");
            
            // 检测服务器 IP
            System.out.println("🔍 检测服务器网络...");
            try {
                URL ipv4 = new URL("https://api.ipify.org");
                BufferedReader r4 = new BufferedReader(new InputStreamReader(ipv4.openStream()));
                System.out.println("📍 IPv4: " + r4.readLine());
            } catch (Exception e) {
                System.out.println("📍 IPv4: 使用域名");
            }
            System.out.println("");
            
            // ==================== Hysteria2 ====================
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
            
            // ==================== Xray (更快的 Shadowsocks) ====================
            File xray = new File(baseDir + "/xray");
            if (!xray.exists()) {
                System.out.println("📦 [2/3] 下载 Xray...");
                downloadFile(
                    "https://github.com/XTLS/Xray-core/releases/download/v1.8.24/Xray-linux-64.zip",
                    baseDir + "/xray.zip"
                );
                runCmd(baseDir, "unzip", "-o", "xray.zip", "xray");
                runCmd(baseDir, "chmod", "+x", "xray");
                new File(baseDir + "/xray.zip").delete();
            } else {
                System.out.println("📦 [2/3] Xray 已存在 ✓");
            }
            
            // 生成证书
            File cert = new File(baseDir + "/server.crt");
            if (!cert.exists()) {
                System.out.println("📦 [3/3] 生成证书...");
                ProcessBuilder pb = new ProcessBuilder(
                    "openssl", "req", "-x509", "-nodes", "-newkey", "rsa:2048",
                    "-keyout", baseDir + "/server.key",
                    "-out", baseDir + "/server.crt",
                    "-days", "3650",
                    "-subj", "/CN=node.zenix.sg"
                );
                pb.directory(new File(baseDir));
                pb.inheritIO();
                pb.start().waitFor();
            } else {
                System.out.println("📦 [3/3] 证书已存在 ✓");
            }
            
            // Hysteria2 配置
            String hyConfig = 
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
                "bandwidth:\n" +
                "  up: 200 mbps\n" +
                "  down: 200 mbps\n" +
                "\n" +
                "quic:\n" +
                "  initStreamReceiveWindow: 8388608\n" +
                "  maxStreamReceiveWindow: 8388608\n" +
                "  initConnReceiveWindow: 20971520\n" +
                "  maxConnReceiveWindow: 20971520\n" +
                "  maxIdleTimeout: 60s\n" +
                "  maxIncomingStreams: 1024\n" +
                "\n" +
                "masquerade:\n" +
                "  type: proxy\n" +
                "  proxy:\n" +
                "    url: https://www.bing.com\n" +
                "    rewriteHost: true\n";
            writeFile(baseDir + "/hy-config.yaml", hyConfig);
            
            // Xray Shadowsocks 配置（高性能版）
            String xrayConfig = "{\n" +
                "  \"log\": { \"loglevel\": \"warning\" },\n" +
                "  \"inbounds\": [{\n" +
                "    \"port\": " + PORT + ",\n" +
                "    \"protocol\": \"shadowsocks\",\n" +
                "    \"settings\": {\n" +
                "      \"method\": \"2022-blake3-aes-128-gcm\",\n" +
                "      \"password\": \"" + java.util.Base64.getEncoder().encodeToString(PASSWORD.getBytes()).substring(0, 22) + "==\",\n" +
                "      \"network\": \"tcp,udp\"\n" +
                "    }\n" +
                "  }],\n" +
                "  \"outbounds\": [{ \"protocol\": \"freedom\" }]\n" +
                "}\n";
            writeFile(baseDir + "/xray-config.json", xrayConfig);
            
            // 生成 SS 2022 密码
            String ss2022Pass = java.util.Base64.getEncoder().encodeToString(PASSWORD.getBytes()).substring(0, 22) + "==";
            
            // 显示配置
            System.out.println("");
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║         ✅ 双协议高速节点部署完成！                     ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║  📍 地址: node.zenix.sg                                  ║");
            System.out.println("║  📍 端口: " + PORT + "                                         ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.println("");
            System.out.println("┌──────────────────────────────────────────────────────────┐");
            System.out.println("│  🖥️  节点1: Hysteria2（电脑/安卓）                       │");
            System.out.println("├──────────────────────────────────────────────────────────┤");
            System.out.println("│  协议: Hysteria2 (UDP) - 速度最快                        │");
            System.out.println("│  端口: " + PORT + "                                            │");
            System.out.println("│  密码: " + PASSWORD + "                                      │");
            System.out.println("│                                                          │");
            System.out.println("│  v2rayN 导入:                                            │");
            System.out.println("│  hysteria2://" + PASSWORD + "@node.zenix.sg:" + PORT + "?insecure=1#Zenix-Hy2");
            System.out.println("└──────────────────────────────────────────────────────────┘");
            System.out.println("");
            System.out.println("┌──────────────────────────────────────────────────────────┐");
            System.out.println("│  📱 节点2: Shadowsocks 2022（苹果手机）                  │");
            System.out.println("├──────────────────────────────────────────────────────────┤");
            System.out.println("│  协议: Shadowsocks 2022 (TCP/UDP) - 新协议更快           │");
            System.out.println("│  端口: " + PORT + "                                            │");
            System.out.println("│  密码: " + ss2022Pass + "               │");
            System.out.println("│  加密: 2022-blake3-aes-128-gcm                           │");
            System.out.println("│                                                          │");
            System.out.println("│  Shadowrocket 配置:                                      │");
            System.out.println("│    类型: Shadowsocks                                     │");
            System.out.println("│    地址: node.zenix.sg                                   │");
            System.out.println("│    端口: " + PORT + "                                          │");
            System.out.println("│    密码: " + ss2022Pass + "             │");
            System.out.println("│    加密: 2022-blake3-aes-128-gcm                         │");
            System.out.println("└──────────────────────────────────────────────────────────┘");
            System.out.println("");
            System.out.println("══════════════════════════════════════════════════════════");
            System.out.println("🔄 启动服务...");
            System.out.println("══════════════════════════════════════════════════════════");
            
            // 启动 Hysteria2（后台）
            ProcessBuilder hyPb = new ProcessBuilder(
                baseDir + "/hysteria", "server", "-c", baseDir + "/hy-config.yaml"
            );
            hyPb.directory(new File(baseDir));
            hyPb.redirectErrorStream(true);
            Process hyProcess = hyPb.start();
            
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(hyProcess.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Hy2] " + line);
                    }
                } catch (Exception e) {}
            }).start();
            
            Thread.sleep(2000);
            System.out.println("✅ Hysteria2 已启动 (UDP:" + PORT + ")");
            
            // 启动 Xray Shadowsocks（前台）
            System.out.println("✅ Shadowsocks 2022 启动中 (TCP/UDP:" + PORT + ")...");
            System.out.println("");
            
            ProcessBuilder xrayPb = new ProcessBuilder(
                baseDir + "/xray", "run", "-c", baseDir + "/xray-config.json"
            );
            xrayPb.directory(new File(baseDir));
            xrayPb.inheritIO();
            xrayPb.start().waitFor();
            
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
    
    static void runCmd(String dir, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(dir));
        pb.inheritIO();
        pb.start().waitFor();
    }
}
