package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        // 面板分配给你的外部端口
        int publicPort = 30196; 
        // 内部躲起来的端口 (随便选一个不冲突的)
        int internalPort = 18789; 

        String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

        // 1. 建立隧道：外部 30196 -> 内部 18789
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 稍微等下 OpenClaw 启动
                System.out.println("🌉 [隧道开启] 外部访问端口 " + publicPort + " 已连接至内部 " + internalPort);
                ServerSocket serverSocket = new ServerSocket(publicPort, 50, InetAddress.getByName("0.0.0.0"));
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
                System.err.println("❌ 隧道建立失败: " + e.getMessage());
            }
        }).start();

        try {
            // 2. 启动 OpenClaw：让它监听 internalPort (18789)
            System.out.println("🚀 OpenClaw 正在内部端口 " + internalPort + " 启动...");
            
            ProcessBuilder pb = new ProcessBuilder(
                baseDir + "/node-v22.12.0-linux-x64/bin/node",
                "dist/index.js", "gateway", 
                "--port", String.valueOf(internalPort), // 关键：错开端口
                "--force"
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
