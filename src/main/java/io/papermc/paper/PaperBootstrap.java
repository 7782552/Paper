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
            System.out.println("🛠️ [Bridge 专项修复] 正在重构链路...");

            // 1. 清理残留
            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 2. 写入 OpenClaw 唯一认可的极简 JSON
            File configDir = new File(baseDir + "/.openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            String configJson = "{\"meta\":{\"lastTouchedVersion\":\"2026.2.1\"},\"gateway\":{\"port\":" + internalPort + ",\"mode\":\"local\",\"bind\":\"loopback\"},\"plugins\":{\"enabled\":true}}";
            Files.write(Paths.get(baseDir + "/.openclaw/openclaw.json"), configJson.getBytes());

            // 3. 物理隧道 (确保网页能再次打开)
            new Thread(() -> {
                try (ServerSocket ss = new ServerSocket(publicPort, 128, InetAddress.getByName("0.0.0.0"))) {
                    while (true) {
                        Socket c = ss.accept();
                        new Thread(() -> {
                            try (Socket t = new Socket("127.0.0.1", internalPort)) {
                                c.setTcpNoDelay(true); t.setTcpNoDelay(true);
                                Thread t1 = new Thread(() -> pipe(c, t));
                                Thread t2 = new Thread(() -> pipe(t, c));
                                t1.start(); t2.start();
                                t1.join(); t2.join();
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {}
            }).start();

            // 4. 启动 Node：关键在于修复 WebSocket 握手地址
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
            
            // --- 核心修复：强制前端网页去连公网端口，而不是内网端口 ---
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_WS_URL", "ws://" + publicHost + ":" + publicPort); 
            env.put("OPENCLAW_PUBLIC_URL", "http://" + publicHost + ":" + publicPort);

            pb.inheritIO();
            Process p = pb.start();

            // 5. 暴力自动审批
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            new Thread(() -> {
                try {
                    while (p.isAlive()) {
                        Thread.sleep(10000);
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
        try (InputStream is = f.getInputStream(); OutputStream os = t.getOutputStream()) {
            byte[] b = new byte[65536];
            int l;
            while ((l = is.read(b)) != -1) { os.write(b, 0, l); os.flush(); }
        } catch (Exception ignored) {}
    }
}
