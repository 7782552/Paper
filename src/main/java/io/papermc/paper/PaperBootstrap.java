package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 配置中 (本地代理方案)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String apiKey = "sk-g4f-token-any";
            String zeaburUrl = "https://888888888888.zeabur.app";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            // ★★★ 关键：让 OpenClaw 连接本地代理 ★★★
            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", "http://127.0.0.1:9999/v1"); // 本地代理
            env.put("OPENAI_API_BASE", "http://127.0.0.1:9999/v1");
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");

            try {
                URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getResponseCode();
            } catch (Exception e) {}

            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) deleteDirectory(openclawDir);
            openclawDir.mkdirs();
            new File(baseDir + "/.openclaw/workspace").mkdirs();

            // 配置文件
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"agents\": {\n");
            sb.append("    \"defaults\": {\n");
            sb.append("      \"model\": { \"primary\": \"openai/gpt-4o-mini\" },\n");
            sb.append("      \"workspace\": \"").append(baseDir).append("/.openclaw/workspace\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"channels\": {\n");
            sb.append("    \"telegram\": {\n");
            sb.append("      \"enabled\": true,\n");
            sb.append("      \"botToken\": \"").append(telegramToken).append("\",\n");
            sb.append("      \"dmPolicy\": \"open\",\n");
            sb.append("      \"groupPolicy\": \"open\",\n");
            sb.append("      \"allowFrom\": [\"*\"]\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"gateway\": {\n");
            sb.append("    \"mode\": \"local\",\n");
            sb.append("    \"port\": 18789,\n");
            sb.append("    \"bind\": \"lan\",\n");
            sb.append("    \"auth\": { \"mode\": \"token\", \"token\": \"").append(gatewayToken).append("\" }\n");
            sb.append("  }\n");
            sb.append("}");
            Files.write(new File(baseDir + "/.openclaw/openclaw.json").toPath(), sb.toString().getBytes());

            // ★★★ OpenAI API 代理 - 转发到 Zeabur ★★★
            System.out.println("📝 创建 OpenAI API 代理...");
            StringBuilder apiProxy = new StringBuilder();
            apiProxy.append("const http = require('http');\n");
            apiProxy.append("const https = require('https');\n");
            apiProxy.append("const { URL } = require('url');\n\n");
            apiProxy.append("const TARGET = '").append(zeaburUrl).append("';\n\n");
            apiProxy.append("http.createServer((req, res) => {\n");
            apiProxy.append("  const target = new URL(req.url, TARGET);\n");
            apiProxy.append("  console.log('[API Proxy] ' + req.method + ' ' + req.url + ' -> ' + target.href);\n");
            apiProxy.append("  const options = {\n");
            apiProxy.append("    hostname: target.hostname,\n");
            apiProxy.append("    port: 443,\n");
            apiProxy.append("    path: target.pathname + target.search,\n");
            apiProxy.append("    method: req.method,\n");
            apiProxy.append("    headers: { ...req.headers, host: target.hostname }\n");
            apiProxy.append("  };\n");
            apiProxy.append("  const proxyReq = https.request(options, (proxyRes) => {\n");
            apiProxy.append("    console.log('[API Proxy] Response: ' + proxyRes.statusCode);\n");
            apiProxy.append("    res.writeHead(proxyRes.statusCode, proxyRes.headers);\n");
            apiProxy.append("    proxyRes.pipe(res);\n");
            apiProxy.append("  });\n");
            apiProxy.append("  proxyReq.on('error', (e) => {\n");
            apiProxy.append("    console.error('[API Proxy] Error:', e.message);\n");
            apiProxy.append("    res.writeHead(502);\n");
            apiProxy.append("    res.end('Proxy Error: ' + e.message);\n");
            apiProxy.append("  });\n");
            apiProxy.append("  req.pipe(proxyReq);\n");
            apiProxy.append("}).listen(9999, '127.0.0.1', () => console.log('[API Proxy] OpenAI -> Zeabur on :9999'));\n");
            Files.write(new File(baseDir + "/api-proxy.js").toPath(), apiProxy.toString().getBytes());

            // 主代理
            StringBuilder proxy = new StringBuilder();
            proxy.append("const http=require('http'),httpProxy=require('http-proxy');\n");
            proxy.append("const p=httpProxy.createProxyServer({ws:true});\n");
            proxy.append("p.on('error',(e,q,r)=>{if(r&&r.writeHead){r.writeHead(503);r.end();}});\n");
            proxy.append("http.createServer((q,r)=>p.web(q,r,{target:q.headers.host?.startsWith('5.')?'http://127.0.0.1:18789':'http://127.0.0.1:5678'})).on('upgrade',(q,s,h)=>p.ws(q,s,h,{target:q.headers.host?.startsWith('5.')?'ws://127.0.0.1:18789':'ws://127.0.0.1:5678'})).listen(30196,'0.0.0.0',()=>console.log('Proxy:30196'));\n");
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxy.toString().getBytes());

            new File(baseDir + "/.n8n").mkdirs();

            // ★★★ 先启动 API 代理 ★★★
            System.out.println("🚀 启动 API 代理 (9999 -> Zeabur)...");
            ProcessBuilder apiProxyPb = new ProcessBuilder(nodeBin, baseDir + "/api-proxy.js");
            apiProxyPb.environment().putAll(env);
            apiProxyPb.directory(new File(baseDir));
            apiProxyPb.inheritIO();
            apiProxyPb.start();

            Thread.sleep(2000);

            System.out.println("🚀 启动 n8n...");
            ProcessBuilder n8n = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", baseDir + "/node_modules/.bin/n8n", "start");
            n8n.environment().putAll(env);
            n8n.environment().put("N8N_PORT", "5678");
            n8n.environment().put("N8N_HOST", "0.0.0.0");
            n8n.environment().put("N8N_SECURE_COOKIE", "false");
            n8n.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8n.directory(new File(baseDir));
            n8n.inheritIO();
            n8n.start();

            System.out.println("🚀 启动 Gateway...");
            ProcessBuilder gw = new ProcessBuilder(nodeBin, ocBin, "gateway", "--port", "18789", "--bind", "lan", "--token", gatewayToken, "--verbose");
            gw.environment().putAll(env);
            gw.directory(new File(baseDir));
            gw.inheritIO();
            gw.start();

            Thread.sleep(12000);

            System.out.println("🚀 启动主代理...");
            ProcessBuilder px = new ProcessBuilder(nodeBin, baseDir + "/proxy.js");
            px.environment().putAll(env);
            px.directory(new File(baseDir));
            px.inheritIO();
            px.start().waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) { if (f.isDirectory()) deleteDirectory(f); else f.delete(); }
        dir.delete();
    }
}
