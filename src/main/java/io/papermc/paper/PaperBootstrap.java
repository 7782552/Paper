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
            System.out.println("🛡️ [破盾模式 23.0] 正在强制适配 Cloudflare 加密链路...");

            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            String configJson = "{\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},\"gateway\":{\"port\":" + internalPort + ",\"mode\":\"local\",\"bind\":\"loopback\"},\"plugins\":{\"enabled\":true}}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 1. 物理隧道保持
            new Thread(() -> {
                try (ServerSocket ss = new ServerSocket(publicPort, 128, InetAddress.getByName("0.0.0.0"))) {
                    while (true) {
                        Socket c = ss.accept();
                        new Thread(() -> {
                            try (Socket t = new Socket("127.0.0.1", internalPort)) {
                                pipe(c, t); pipe(t, c);
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {}
            }).start();

            // 2. 启动 Node：核心改动 wss:// 和 https://
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(internalPort),
                "--token", gatewayToken,
                "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            
            // --- 核心修复：Cloudflare 节点必须使用 WSS 协议 ---
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_WS_URL", "wss://" + publicHost + ":" + publicPort); 
            env.put("OPENCLAW_PUBLIC_URL", "https://" + publicHost + ":" + publicPort);
            env.put("OPENCLAW_GATEWAY_WS_URL", "wss://" + publicHost + ":" + publicPort);

            pb.inheritIO();
            Process p = pb.start();

            // 3. 自动审批注入
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            new Thread(() -> {
                try {
                    while (p.isAlive()) {
                        Thread.sleep(8000);
                        writer.write("pairing approve telegram all\n");
                        writer.flush();
                    }
                } catch (Exception ignored) {}
            }).start();

            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void pipe(Socket f, Socket t) {
        new Thread(() -> {
            try (InputStream is = f.getInputStream(); OutputStream os = t.getOutputStream()) {
                byte[] b = new byte[65536];
                int l;
                while ((l = is.read(b)) != -1) { os.write(b, 0, l); os.flush(); }
            } catch (Exception ignored) {}
        }).start();
    }
}
