package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        int publicPort = 30196;   
        int internalPort = 18789; 
        String publicHost = "node.zenix.sg";

        try {
            System.out.println("🩺 [2026 最终战役版] 执行环境变量全量修复...");

            // 1. 清理进程
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            
            // 2. 写入官方极简 JSON
            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            String configJson = "{\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},\"gateway\":{\"port\":" + internalPort + ",\"mode\":\"local\",\"bind\":\"loopback\",\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}},\"plugins\":{\"enabled\":true}}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 3. 开启公网映射隧道
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(publicPort, 128, InetAddress.getByName("0.0.0.0"));
                    System.out.println("🌉 [隧道直连] 已监听: http://" + publicHost + ":" + publicPort);
                    while (true) {
                        Socket client = serverSocket.accept();
                        new Thread(() -> {
                            try (Socket target = new Socket("127.0.0.1", internalPort)) {
                                pipe(client, target);
                                pipe(target, client);
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 隧道错误: " + e.getMessage());
                }
            }).start();

            // 4. 启动 Node：删掉报错的参数，全部改用环境变量
            System.out.println("🚀 启动 OpenClaw (环境变量模式)...");
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", String.valueOf(internalPort), "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // --- 2026 指南核心变量注入 ---
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            
            // 核心修复：用环境变量声明公网身份，修复 Bridge Missing
            env.put("OPENCLAW_PUBLIC_URL", "http://" + publicHost + ":" + publicPort);
            env.put("OPENCLAW_WS_URL", "ws://" + publicHost + ":" + publicPort);
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void pipe(Socket from, Socket to) {
        new Thread(() -> {
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
        }).start();
    }
}
