package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🕵️ [OpenClaw] 正在强制运行 Schema 生成函数...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            String probeScript = 
                "import path from 'path';\n" +
                "async function probe() {\n" +
                "  console.log('--- START STRUCTURE PROBE ---');\n" +
                "  try {\n" +
                "    const schemaModule = await import('file://' + path.join(process.cwd(), 'dist/config/schema.js'));\n" +
                "    // 核心改动：执行这个函数来获取真正的结构定义\n" +
                "    const schema = schemaModule.buildConfigSchema();\n" +
                "    \n" +
                "    // 递归打印所有属性名，帮我们找到 agents 和 channels 的正确拼写\n" +
                "    const keys = (obj, indent = '') => {\n" +
                "      for (let key in obj.properties || {}) {\n" +
                "        console.log(indent + key);\n" +
                "        if (obj.properties[key].properties) keys(obj.properties[key], indent + '  ');\n" +
                "      }\n" +
                "    };\n" +
                "    keys(schema);\n" +
                "  } catch (e) {\n" +
                "    console.error('Probe failed: ' + e.stack);\n" +
                "  }\n" +
                "  console.log('--- END STRUCTURE PROBE ---');\n" +
                "}\n" +
                "probe();";

            Files.write(Paths.get(openclawDir, "probe.js"), probeScript.getBytes());

            ProcessBuilder pb = new ProcessBuilder(nodePath, "probe.js");
            pb.directory(new File(openclawDir));
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
