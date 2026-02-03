package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 用户核心配置 ---
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        int publicPort = 30196;   // 翼龙分配的公网端口
        int internalPort = 18789; // OpenClaw 内部端口
        String publicHost = "node.zenix.sg"; // 你的公网域名/IP

        try {
            System.out.println("🔧 [OpenClaw 2026 最终修复版] 正在执行全量部署...");

            // 1. 强力清场
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            
            // 2. 写入符合 2026 规范的极简 JSON
            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{"
                    + "\"port\":" + internalPort + ","
                    + "\"mode\":\"local\","
                    + "\"bind\":\"loopback\","
                    + "\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}"
                + "},"
                + "\"plugins\":{\"enabled\":true}"
                + "}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());
            System.out.println("✅ 官方规范 JSON 已写入。");

            // 3. 开启双向流量隧道 (0.0.0.0:30196 -> 127.0.0.1:18789)
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(publicPort, 128, InetAddress.getByName("0.0.0.0"));
                    System.out.println("🌉 [隧道直连] 监听地址: http://" + publicHost + ":" + publicPort);
                    while (true) {
                        Socket client = serverSocket.accept();
                        new Thread(() -> {
                            try (Socket target = new Socket("127.0.0.1", internalPort)) {
                                client.setTcpNoDelay(true);
                                target.setTcpNoDelay(true);
                                Thread t1 = new Thread(() -> pipeStreams(client, target));
                                Thread t2 = new Thread(() -> pipeStreams(target, client));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 隧道异常: " + e.getMessage());
                }
            }).start();

            // 4. 启动 Node：注入公网身份与插件 Token
            System.out.println("🚀 启动主程序并同步公网状态...");
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(internalPort), 
                "--public-url", "http://" + publicHost + ":" + publicPort, // 修复前端 Bridge 
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // 按照 2026.02.02 最新指南强灌环境变量
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_WS_URL", "ws://" + publicHost + ":" + publicPort); // 强制 WebSocket 握手

            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void pipeStreams(Socket from, Socket to) {
        try {
            InputStream is = from.getInputStream();
            OutputStream os = to.getOutputStream();
            byte[] buf = new byte[16384];
            int len;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }
        } catch (Exception ignored) {}
    }
}
