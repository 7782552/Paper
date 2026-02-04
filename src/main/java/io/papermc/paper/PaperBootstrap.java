package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
        String gatewayToken = "mytoken123";
        
        // 🚨 这里的 publicIp 必须改成你翼龙面板显示的那个数字 IP！
        // 比如 "103.21.5.74" 这种，千万别写 "ip" 两个字母
        String publicIp = "把这里换成你的真实服务器IP"; 
        int publicPort = 30196;   
        int internalPort = 18789; 

        try {
            System.out.println("✅ [说明书模式] 流量搬运: " + publicPort + " -> " + internalPort);

            new ProcessBuilder("pkill", "-9", "node").start().waitFor();

            // 1. 物理隧道
            new Thread(() -> {
                try (ServerSocket ss = new ServerSocket(publicPort, 128, InetAddress.getByName("0.0.0.0"))) {
                    while (true) {
                        Socket c = ss.accept();
                        new Thread(() -> {
                            try (Socket n = new Socket("127.0.0.1", internalPort)) {
                                pipe(c, n); pipe(n, c);
                            } catch (Exception ignored) {}
                        }).start();
                    }
                } catch (Exception e) {}
            }).start();

            // 2. 启动 Node：根据说明书注入 Web 端连接变量
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
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_HOST", "127.0.0.1");
            
            // --- 说明书关键变量：修复 Bridge Missing ---
            env.put("OPENCLAW_WS_URL", "ws://" + publicIp + ":" + publicPort);
            env.put("OPENCLAW_PUBLIC_URL", "http://" + publicIp + ":" + publicPort);

            pb.inheritIO();
            Process p = pb.start();

            // 3. 自动审批
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void pipe(Socket f, Socket t) {
        new Thread(() -> {
            try (InputStream is = f.getInputStream(); OutputStream os = t.getOutputStream()) {
                byte[] b = new byte[32768];
                int l;
                while ((l = is.read(b)) != -1) { os.write(b, 0, l); os.flush(); }
            } catch (Exception ignored) {}
        }).start();
    }
}
