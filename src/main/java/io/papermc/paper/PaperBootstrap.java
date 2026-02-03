package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String serverPort = "30196";
        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        // 1. 开启一个后台线程做“物理端口转发”
        new Thread(() -> {
            try {
                System.out.println("🌉 [转发器] 正在建立隧道: 0.0.0.0:" + serverPort + " -> 127.0.0.1:" + serverPort);
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(serverPort), 50, InetAddress.getByName("0.0.0.0"));
                while (true) {
                    Socket client = serverSocket.accept();
                    new Thread(() -> {
                        try (Socket target = new Socket("127.0.0.1", Integer.parseInt(serverPort))) {
                            // 简单的双向流拷贝
                            Thread t1 = new Thread(() -> pipe(client, target));
                            Thread t2 = new Thread(() -> pipe(target, client));
                            t1.start(); t2.start();
                            t1.join(); t2.join();
                        } catch (Exception ignored) {}
                    }).start();
                }
            } catch (Exception e) {
                System.err.println("❌ 转发器启动失败 (端口可能被占用，请稍后): " + e.getMessage());
            }
        }).start();

        try {
            // 2. 正常启动 OpenClaw，让它在 127.0.0.1 待着
            System.out.println("🚀 正在点火 OpenClaw (让它维持在 127.0.0.1)...");
            
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", "--port", serverPort, "--force"
            );
            
            pb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_TELEGRAM_BOT_TOKEN", botToken);
            env.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");

            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 转发流工具函数
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
