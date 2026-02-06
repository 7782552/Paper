package io.papermc.paper;

import java.io.*;
import java.net.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        int PORT = 30194;
        String PASSWORD = "zenix2024";
        
        try {
            System.out.println("🚀 部署 Shadowsocks 节点...");
            System.out.println("");
            
            // 检测服务器 IP
            System.out.println("🔍 检测服务器网络...");
            try {
                URL ipv4 = new URL("https://api.ipify.org");
                BufferedReader r4 = new BufferedReader(new InputStreamReader(ipv4.openStream()));
                System.out.println("📍 IPv4 地址: " + r4.readLine());
            } catch (Exception e) {
                System.out.println("📍 IPv4: 不可用");
            }
            
            try {
                URL ipv6 = new URL("https://api6.ipify.org");
                BufferedReader r6 = new BufferedReader(new InputStreamReader(ipv6.openStream()));
                System.out.println("📍 IPv6 地址: " + r6.readLine());
            } catch (Exception e) {
                System.out.println("📍 IPv6: 不可用");
            }
            System.out.println("");
            
            // 下载 shadowsocks-rust
            File ss = new File(baseDir + "/ssserver");
            if (!ss.exists()) {
                System.out.println("📦 [1/2] 下载 Shadowsocks...");
                downloadFile(
                    "https://github.com/shadowsocks/shadowsocks-rust/releases/download/v1.18.2/shadowsocks-v1.18.2.x86_64-unknown-linux-gnu.tar.xz",
                    baseDir + "/ss.tar.xz"
                );
                runCmd(baseDir, "tar", "-xf", "ss.tar.xz");
                runCmd(baseDir, "rm", "ss.tar.xz");
                runCmd(baseDir, "chmod", "+x", "ssserver");
            } else {
                System.out.println("📦 Shadowsocks 已存在，跳过下载");
            }
            
            // 创建配置（监听 IPv4 和 IPv6）
            System.out.println("📦 [2/2] 创建配置...");
            String config = "{\n" +
                "    \"server\": \"[::]\",\n" +  // 同时监听 IPv4 和 IPv6
                "    \"server_port\": " + PORT + ",\n" +
                "    \"password\": \"" + PASSWORD + "\",\n" +
                "    \"method\": \"aes-256-gcm\",\n" +
                "    \"timeout\": 300,\n" +
                "    \"mode\": \"tcp_and_udp\"\n" +
                "}\n";
            
            writeFile(baseDir + "/ss-config.json", config);
            
            System.out.println("");
            System.out.println("==================================================");
            System.out.println("✅ Shadowsocks 部署完成！");
            System.out.println("==================================================");
            System.out.println("");
            System.out.println("📍 地址: node.zenix.sg");
            System.out.println("📍 端口: " + PORT);
            System.out.println("🔑 密码: " + PASSWORD);
            System.out.println("🔐 加密: aes-256-gcm");
            System.out.println("");
            System.out.println("=== v2rayN 导入链接 ===");
            String encoded = java.util.Base64.getEncoder().encodeToString(
                ("aes-256-gcm:" + PASSWORD).getBytes()
            );
            String ssLink = "ss://" + encoded + "@node.zenix.sg:" + PORT + "#Zenix-SS";
            System.out.println(ssLink);
            System.out.println("");
            System.out.println("=== Clash 配置 ===");
            System.out.println("- name: Zenix-SS");
            System.out.println("  type: ss");
            System.out.println("  server: node.zenix.sg");
            System.out.println("  port: " + PORT);
            System.out.println("  cipher: aes-256-gcm");
            System.out.println("  password: " + PASSWORD);
            System.out.println("");
            System.out.println("==================================================");
            System.out.println("🔄 Shadowsocks 服务运行中...");
            System.out.println("==================================================");
            
            // 启动 Shadowsocks
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/ssserver", "-c", baseDir + "/ss-config.json"
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
                System.out.print("\r   已下载: " + (total / 1024) + " KB");
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
