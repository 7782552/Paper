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

        try {
            // 1. 环境清理与网络检查
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();
            System.out.println("📡 阶段 1: 网络与进程清理完成...");

            // 2. 强制写入“医生”无法拒绝的配置
            // 我们通过物理手段，直接把插件开关打开
            File configDir = new File(baseDir + "/.openclaw");
            configDir.mkdirs();
            String configJson = "{"
                + "\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},"
                + "\"gateway\":{\"port\":" + internalPort + ",\"mode\":\"local\",\"bind\":\"loopback\",\"auth\":{\"mode\":\"token\",\"token\":\"" + gatewayToken + "\"}},"
                + "\"plugins\":{\"enabled\":[\"telegram\"]}"
                + "}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 3. 建立公网映射隧道 (0.0.0.0:30196 -> 127.0.0.1:18789)
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(publicPort, 100, InetAddress.getByName("0.0.0.0"));
                    System.out.println("🌉 [公网映射] 已开启: 你的公网地址:30196 现在直达内部 18789");
                    while (true) {
                        Socket client = serverSocket.accept();
                        new Thread(() -> {
                            try (Socket target = new Socket("127.0.0.1", internalPort)) {
                                Thread t1 = new Thread(() -> pipe(client, target));
                                Thread t2 = new Thread(() -> pipe(target, client));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {
                    System.err.println("❌ 隧道异常: " + e.getMessage());
                }
            }).start();

            // 4. 启动 Node 并强灌插件参数
            System.out.println("🚀 阶段 2: 正在点火，并强灌插件 Token...");
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", String.valueOf(internalPort), "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);
            env.put("NODE_ENV", "production");

            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void pipe(Socket in, Socket out) {
        try {
            InputStream is = in.getInputStream();
            OutputStream os = out.getOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }
        } catch (Exception ignored) {}
    }
}
