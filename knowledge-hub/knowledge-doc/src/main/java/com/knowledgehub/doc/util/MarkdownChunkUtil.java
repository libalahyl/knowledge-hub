package com.knowledgehub.doc.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown 文档切片工具类（纯静态逻辑，不依赖 Spring）
 *
 * <p>切片规则：</p>
 * <ol>
 *   <li>优先按 ## 二级标题切</li>
 *   <li>某节超过 800 字，再按 ### 三级标题切</li>
 *   <li>还超，按句号（. 或 。）二次切</li>
 *   <li>每片控制在 300-800 字</li>
 *   <li>无标题时整篇作为一片</li>
 *   <li>空内容返回空列表</li>
 * </ol>
 */
public class MarkdownChunkUtil {

    /** 每片最大长度（字符数） */
    private static final int MAX_CHUNK_LENGTH = 800;

    private MarkdownChunkUtil() {
    }

    /**
     * 把 Markdown 原文切成若干片
     *
     * @param content Markdown 原文
     * @return 切片列表（空内容返回空列表）
     */
    public static List<String> splitIntoChunks(String content) {
        List<String> result = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return result;
        }

        // 第一步：按 ## 二级标题切
        List<String> h2Sections = splitByHeader(content, "## ");
        for (String h2 : h2Sections) {
            if (h2.isBlank()) {
                continue;
            }
            if (h2.length() <= MAX_CHUNK_LENGTH) {
                result.add(h2.trim());
            } else {
                // 第二步：超长的节按 ### 三级标题切
                List<String> h3Sections = splitByHeader(h2, "### ");
                for (String h3 : h3Sections) {
                    if (h3.isBlank()) {
                        continue;
                    }
                    if (h3.length() <= MAX_CHUNK_LENGTH) {
                        result.add(h3.trim());
                    } else {
                        // 第三步：仍超长，按句号二次切
                        splitBySentence(h3, result);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 按标题切分，标题行保留在每段开头；没有该级标题时整段作为一片返回
     *
     * @param text   原文
     * @param marker 标题标记（"## " 或 "### "）
     * @return 切分后的段落列表
     */
    private static List<String> splitByHeader(String text, String marker) {
        List<String> sections = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith(marker)) {
                if (current.length() > 0) {
                    sections.add(current.toString());
                }
                current = new StringBuilder();
            }
            current.append(line).append("\n");
        }
        if (current.length() > 0) {
            sections.add(current.toString());
        }
        return sections;
    }

    /**
     * 按句号二次切分，保证每片不超过最大长度；单个超长句会被硬切成多片
     *
     * @param section 超长段落
     * @param result  结果列表
     */
    private static void splitBySentence(String section, List<String> result) {
        String[] sentences = section.split("(?<=[.。])");
        StringBuilder buffer = new StringBuilder();
        for (String sentence : sentences) {
            // 单个句子本身超过上限，硬切成多片
            if (sentence.length() > MAX_CHUNK_LENGTH) {
                if (buffer.length() > 0) {
                    result.add(buffer.toString().trim());
                    buffer = new StringBuilder();
                }
                for (int i = 0; i < sentence.length(); i += MAX_CHUNK_LENGTH) {
                    int end = Math.min(i + MAX_CHUNK_LENGTH, sentence.length());
                    result.add(sentence.substring(i, end).trim());
                }
                continue;
            }
            // 当前缓冲 + 这句要超了，先落盘
            if (buffer.length() + sentence.length() > MAX_CHUNK_LENGTH) {
                result.add(buffer.toString().trim());
                buffer = new StringBuilder();
            }
            buffer.append(sentence);
        }
        if (buffer.length() > 0) {
            result.add(buffer.toString().trim());
        }
    }
}
