package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        // --- 用户配置区 ---
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        int publicPort = 30196;   // 翼龙分配的公网端口
        int internalPort = 18789; // OpenClaw 监听的本地端口

        try {
            System.out.println("🛠️ [OpenClaw 2026 官方标准模式] 启动中...");

            // 1. 彻底清理旧环境，防止 Doctor 校验报错
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            
            // 2. 写入官方认可的极简 JSON (符合 Schema 校验)
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

            // 3. 建立 Java 隧道：映射公网 30196 -> 本地 18789
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(publicPort, 128, InetAddress.getByName("0.0.0.0"));
                    System.out.println("🌉 [隧道已挂载] 公网直连已就绪: node.zenix.sg:" + publicPort);
                    while (true) {
                        Socket client = serverSocket.accept();
                        new Thread(() -> {
                            try (Socket target = new Socket("127.0.0.1", internalPort)) {
                                client.setTcpNoDelay(true);
                                target.setTcpNoDelay(true);
                                // 启动双向拷贝
                                Thread t1 = new Thread(() -> copyStream(client, target));
                                Thread t2 = new Thread(() -> copyStream(target, client));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 隧道异常: " + e.getMessage());
                }
            }).start();

            // 4. 启动 Node 并强灌环境变量
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", String.valueOf(internalPort), "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("NODE_ENV", "production");
            
            // 根据官方 2026.02.02 指南要求的变量名
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);

            pb.inheritIO();
            System.out.println("🚀 官方进程点火成功，正在等待 Listening...");
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyStream(Socket from, Socket to) {
        try (InputStream is = from.getInputStream(); OutputStream os = to.getOutputStream()) {
            byte[] buf = new byte[16384];
            int len;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }
        } catch (Exception ignored) {}
    }
}
