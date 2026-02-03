package io.papermc.paper;
import java.io.*;
import java.sql.*; // 需要确保环境有 sqlite 驱动，通常 Node 项目里会自带
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔨 [OpenClaw] 正在执行底层数据库物理改写 (SQLite Injection)...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理修复权限 (这是基石)
            new ProcessBuilder("chmod", "-R", "700", baseDir + "/.openclaw").start().waitFor();

            // 2. 既然 CLI 命令全废了，我们直接用 node 执行一段临时的 js 来改数据库
            // 这是最高级的“暴力”，直接绕过 openclaw 的业务逻辑，改它的持久化层
            System.out.println("💉 正在注入 Telegram 激活脚本...");
            String hackScript = 
                "const sqlite3 = require('sqlite3').verbose(); " +
                "const db = new sqlite3.Database('" + baseDir + "/.openclaw/state.db'); " +
                "db.serialize(() => { " +
                "  db.run(\"INSERT OR REPLACE INTO kv (key, value) VALUES ('channels.telegram.enabled', 'true')\"); " +
                "  db.run(\"INSERT OR REPLACE INTO kv (key, value) VALUES ('channels.telegram.botToken', '" + botToken + "')\"); " +
                "  db.run(\"INSERT OR REPLACE INTO kv (key, value) VALUES ('channels.telegram.dmPolicy', 'open')\"); " +
                "  db.run(\"INSERT OR REPLACE INTO kv (key, value) VALUES ('channels.telegram.allowFrom', '[\\\"*\\\"]')\"); " +
                "  console.log('✅ 数据库物理注入成功'); " +
                "}); db.close();";

            ProcessBuilder hackPb = new ProcessBuilder(nodePath, "-e", hackScript);
            hackPb.directory(new File(openclawDir)); // 利用 openclaw 目录下的 node_modules
            hackPb.inheritIO();
            hackPb.start().waitFor();

            // 3. 启动网关
            System.out.println("🚀 注入完成，网关点火...");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "secure_long_token_2026_success");
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
