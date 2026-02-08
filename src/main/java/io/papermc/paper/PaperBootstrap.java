package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置 (环境变量注入版)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String baseUrl = "https://888888888888.zeabur.app/v1"; 
            String apiKey = "sk-g4f-2026"; 
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            // 构建官方支持的 Providers 环境变量 JSON
            String providersJson = "{\"openai\":{\"baseUrl\":\"" + baseUrl + "\",\"apiKey\":\"" + apiKey + "\"}}";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENCLAW_PROVIDERS", providersJson); // 核心：官方最高优先级配置
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", baseUrl);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);

            System.out.println("🧹 清理旧配置...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) { deleteDirectory(openclawDir); }
            openclawDir.mkdirs();

            System.out.println("📝 写入极简 openclaw.json (避免校验失败)...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            // 根目录下绝对不放 providers，只放 agent 定义
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"agents\": {\n");
            sb.append("    \"defaults\": {\n");
            sb.append("      \"model\": { \"primary\": \"openai/gpt-4o-mini\" },\n");
            sb.append("      \"workspace\": \"/home/container/.openclaw/workspace\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"channels\": {\n");
            sb.append("    \"telegram\": {\n");
            sb.append("      \"botToken\": \"").append(telegramToken).append("\",\n");
            sb.append("      \"dmPolicy\": \"open\",\n");
            sb.append("      \"allowFrom\": [\"*\"]\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"gateway\": { \"port\": 18789, \"auth\": { \"mode\": \"token\", \"token\": \"").append(gatewayToken).append("\" } }\n");
            sb.append("}");
            Files.write(configFile.toPath(), sb.toString().getBytes());

            // 增强版 Proxy：增加错误忽略，防止 Crash
            String proxyCode = "const http = require('http'); const httpProxy = require('http-proxy'); " +
                "const proxy = httpProxy.createProxyServer({ ws: true }); " +
                "proxy.on('error', (err) => console.log('Proxy connection waiting...')); " +
                "http.createServer((req, res) => { " +
                "  const target = req.headers.host.startsWith('5.') ? 'http://127.0.0.1:18789' : 'http://127.0.0.1:5678'; " +
                "  proxy.web(req, res, { target }); " +
                "}).on('upgrade', (req, socket, head) => { " +
                "  const target = req.headers.host.startsWith('5.') ? 'ws://127.0.0.1:18789' : 'ws://127.0.0.1:5678'; " +
                "  proxy.ws(req, socket, head, { target }); " +
                "}).listen(30196, '0.0.0.0');";
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxyCode.getBytes());

            System.out.println("🚀 顺序启动...");
            
            // 1. 启动 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.start();

            // 2. 启动 Gateway
            ProcessBuilder gatewayPb = new ProcessBuilder(nodeBin, ocBin, "gateway");
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start();

            // 3. 延迟启动 Proxy，给后端留出启动时间
            System.out.println("⏳ 等待后端端口可用 (20s)...");
            Thread.sleep(20000);
            ProcessBuilder proxyPb = new ProcessBuilder(nodeBin, baseDir + "/proxy.js");
            proxyPb.inheritIO();
            proxyPb.start().waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) { for (File f : files) { if (f.isDirectory()) deleteDirectory(f); else f.delete(); } }
        dir.delete();
    }
}
