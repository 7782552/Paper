package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 配置中 (强制替换版)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String apiKey = "sk-g4f-token-any";
            String zeaburUrl = "https://888888888888.zeabur.app/v1";
            String zeaburHost = "888888888888.zeabur.app";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", zeaburUrl);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");

            // ★★★ 使用 sed 强制替换所有文件 ★★★
            System.out.println("📝 使用 sed 强制替换所有 api.openai.com...");
            
            ProcessBuilder sed1 = new ProcessBuilder("sh", "-c",
                "find " + baseDir + "/node_modules -type f \\( -name '*.js' -o -name '*.mjs' -o -name '*.cjs' \\) " +
                "-exec grep -l 'api.openai.com' {} \\; 2>/dev/null | " +
                "xargs -I {} sed -i 's|https://api.openai.com/v1|" + zeaburUrl + "|g; s|https://api.openai.com|https://" + zeaburHost + "|g; s|api.openai.com|" + zeaburHost + "|g' {} 2>/dev/null"
            );
            sed1.inheritIO();
            Process p1 = sed1.start();
            p1.waitFor();
            System.out.println("  ✓ sed 替换完成");

            // ★★★ 验证替换结果 ★★★
            System.out.println("\n📝 验证是否还有 api.openai.com...");
            ProcessBuilder verify = new ProcessBuilder("sh", "-c",
                "grep -rl 'api.openai.com' " + baseDir + "/node_modules/ 2>/dev/null | wc -l"
            );
            verify.inheritIO();
            verify.start().waitFor();

            // 删除 Webhook
            try {
                java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getResponseCode();
            } catch (Exception e) {}

            // 配置目录
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

            // 代理
            StringBuilder proxy = new StringBuilder();
            proxy.append("const http=require('http'),httpProxy=require('http-proxy');\n");
            proxy.append("const p=httpProxy.createProxyServer({ws:true});\n");
            proxy.append("p.on('error',(e,q,r)=>{if(r&&r.writeHead){r.writeHead(503);r.end();}});\n");
            proxy.append("http.createServer((q,r)=>p.web(q,r,{target:q.headers.host?.startsWith('5.')?'http://127.0.0.1:18789':'http://127.0.0.1:5678'})).on('upgrade',(q,s,h)=>p.ws(q,s,h,{target:q.headers.host?.startsWith('5.')?'ws://127.0.0.1:18789':'ws://127.0.0.1:5678'})).listen(30196,'0.0.0.0',()=>console.log('Proxy:30196'));\n");
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxy.toString().getBytes());

            new File(baseDir + "/.n8n").mkdirs();

            System.out.println("\n🚀 启动服务...");
            
            ProcessBuilder n8n = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", baseDir + "/node_modules/.bin/n8n", "start");
            n8n.environment().putAll(env);
            n8n.environment().put("N8N_PORT", "5678");
            n8n.environment().put("N8N_HOST", "0.0.0.0");
            n8n.environment().put("N8N_SECURE_COOKIE", "false");
            n8n.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8n.directory(new File(baseDir));
            n8n.inheritIO();
            n8n.start();

            ProcessBuilder gw = new ProcessBuilder(nodeBin, ocBin, "gateway", "--port", "18789", "--bind", "lan", "--token", gatewayToken, "--verbose");
            gw.environment().putAll(env);
            gw.directory(new File(baseDir));
            gw.inheritIO();
            gw.start();

            Thread.sleep(15000);

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
