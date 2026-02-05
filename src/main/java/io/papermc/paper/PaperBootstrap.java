package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 正在配置 VLESS 节点...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            
            // 配置参数
            String host = "node.zenix.sg";
            int port = 30194;
            String uuid = UUID.randomUUID().toString();  // 自动生成 UUID
            
            System.out.println("📋 VLESS 配置信息:");
            System.out.println("   地址: " + host);
            System.out.println("   端口: " + port);
            System.out.println("   UUID: " + uuid);
            
            // 下载并安装 xray
            System.out.println("\n📦 下载 Xray...");
            downloadXray(baseDir);
            
            // 生成配置文件
            System.out.println("📝 生成配置文件...");
            generateConfig(baseDir, port, uuid);
            
            // 启动 Xray
            System.out.println("🚀 启动 VLESS 服务...");
            ProcessBuilder xrayPb = new ProcessBuilder(
                baseDir + "/xray/xray", 
                "run", 
                "-c", baseDir + "/xray/config.json"
            );
            xrayPb.inheritIO();
            xrayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void downloadXray(String baseDir) throws Exception {
        String xrayDir = baseDir + "/xray";
        new File(xrayDir).mkdirs();
        
        // 下载 Xray
        String xrayUrl = "https://github.com/XTLS/Xray-core/releases/latest/download/Xray-linux-64.zip";
        System.out.println("   下载: " + xrayUrl);
        
        ProcessBuilder pb = new ProcessBuilder("sh", "-c",
            "cd " + xrayDir + " && " +
            "curl -L -o xray.zip '" + xrayUrl + "' && " +
            "unzip -o xray.zip && " +
            "chmod +x xray && " +
            "rm -f xray.zip"
        );
        pb.inheritIO();
        int code = pb.start().waitFor();
        
        if (code != 0) {
            // 备用下载方式
            System.out.println("   使用备用下载...");
            ProcessBuilder pb2 = new ProcessBuilder("sh", "-c",
                "cd " + xrayDir + " && " +
                "wget -O xray.zip 'https://github.com/XTLS/Xray-core/releases/download/v1.8.24/Xray-linux-64.zip' && " +
                "unzip -o xray.zip && " +
                "chmod +x xray"
            );
            pb2.inheritIO();
            pb2.start().waitFor();
        }
    }

    static void generateConfig(String baseDir, int port, String uuid) throws Exception {
        String config = "{\n" +
            "  \"log\": {\n" +
            "    \"loglevel\": \"info\"\n" +
            "  },\n" +
            "  \"inbounds\": [\n" +
            "    {\n" +
            "      \"port\": " + port + ",\n" +
            "      \"protocol\": \"vless\",\n" +
            "      \"settings\": {\n" +
            "        \"clients\": [\n" +
            "          {\n" +
            "            \"id\": \"" + uuid + "\",\n" +
            "            \"level\": 0\n" +
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
            "      \"protocol\": \"freedom\",\n" +
            "      \"tag\": \"direct\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        FileWriter fw = new FileWriter(baseDir + "/xray/config.json");
        fw.write(config);
        fw.close();
        
        // 生成连接链接
        String vlessLink = "vless://" + uuid + "@node.zenix.sg:30194?encryption=none&type=ws&path=%2Fvless#Pterodactyl-VLESS";
        
        System.out.println("\n✅ VLESS 节点配置完成!");
        System.out.println("========================================");
        System.out.println("📱 连接链接 (复制到客户端):");
        System.out.println(vlessLink);
        System.out.println("========================================");
        System.out.println("\n📋 手动配置:");
        System.out.println("   协议: VLESS");
        System.out.println("   地址: node.zenix.sg");
        System.out.println("   端口: 30194");
        System.out.println("   UUID: " + uuid);
        System.out.println("   传输: WebSocket");
        System.out.println("   路径: /vless");
        System.out.println("   加密: none");
        System.out.println("========================================");
        
        // 保存链接到文件
        FileWriter linkFw = new FileWriter(baseDir + "/vless-link.txt");
        linkFw.write("VLESS 连接链接:\n" + vlessLink + "\n\n");
        linkFw.write("UUID: " + uuid + "\n");
        linkFw.write("地址: node.zenix.sg\n");
        linkFw.write("端口: 30194\n");
        linkFw.write("路径: /vless\n");
        linkFw.close();
    }
}
