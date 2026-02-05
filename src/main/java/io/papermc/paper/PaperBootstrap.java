package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 启动 Shadowsocks 高速节点...");
        try {
            String baseDir = "/home/container";
            String xrayDir = baseDir + "/xray";
            
            String host = "node.zenix.sg";
            int port = 30194;
            String password = "admin123456";
            String method = "chacha20-ietf-poly1305";
            
            new File(xrayDir).mkdirs();
            File xrayFile = new File(xrayDir + "/xray");
            xrayFile.setExecutable(true);
            
            String config = "{\n" +
                "  \"log\": { \"loglevel\": \"warning\" },\n" +
                "  \"inbounds\": [{\n" +
                "    \"listen\": \"0.0.0.0\",\n" +
                "    \"port\": " + port + ",\n" +
                "    \"protocol\": \"shadowsocks\",\n" +
                "    \"settings\": {\n" +
                "      \"method\": \"" + method + "\",\n" +
                "      \"password\": \"" + password + "\",\n" +
                "      \"network\": \"tcp,udp\"\n" +
                "    }\n" +
                "  }],\n" +
                "  \"outbounds\": [{ \"protocol\": \"freedom\" }]\n" +
                "}";
            
            try (FileWriter fw = new FileWriter(xrayDir + "/config.json")) {
                fw.write(config);
            }
            
            // SS 链接
            String ssLink = "ss://" + Base64.getEncoder().encodeToString(
                (method + ":" + password).getBytes()
            ) + "@" + host + ":" + port + "#SS-HighSpeed";
            
            System.out.println("\n========================================");
            System.out.println("✅ Shadowsocks 节点已启动");
            System.out.println("========================================");
            System.out.println("📱 连接链接:\n" + ssLink);
            System.out.println("\n📋 手动配置:");
            System.out.println("   地址: " + host);
            System.out.println("   端口: " + port);
            System.out.println("   密码: " + password);
            System.out.println("   加密: " + method);
            System.out.println("========================================\n");
            
            ProcessBuilder xrayPb = new ProcessBuilder(xrayDir + "/xray", "run", "-c", xrayDir + "/config.json");
            xrayPb.directory(new File(xrayDir));
            xrayPb.inheritIO();
            xrayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
