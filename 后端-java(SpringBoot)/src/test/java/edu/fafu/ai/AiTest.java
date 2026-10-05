package edu.fafu.ai;

import edu.fafu.database.entity.Ai;

public class AiTest {

    public static void main(String[] args) {
        System.out.println("========== AI模块测试 ==========");
        System.out.println();

        int success = 0, fail = 0;

        boolean r1 = testToTextSegment();
        success += r1 ? 1 : 0;
        fail += r1 ? 0 : 1;

        boolean r3 = testToTextSegmentFormat();
        success += r3 ? 1 : 0;
        fail += r3 ? 0 : 1;

        boolean r5 = testGoodsIdPattern();
        success += r5 ? 1 : 0;
        fail += r5 ? 0 : 1;

        System.out.println();
        System.out.println("========== 测试结果 ==========");
        System.out.println("通过: " + success + ", 失败: " + fail);
        System.out.println("状态: " + (fail == 0 ? "全部通过" : "存在失败"));
    }

    static Ai createTestAi() {
        Ai ai = new Ai();
        ai.setId(1);
        ai.setGoodsId(15);
        ai.setCategory("家用电器");
        ai.setKind("扫地机器人");
        ai.setName("科沃斯 X2");
        ai.setPrice("4599元");
        ai.setSimpleDescription("科沃斯方形扫拖机器人，8000Pa大吸力");
        ai.setFeatures("方形设计、8000Pa吸力、自动清洗、热水洗拖布");
        return ai;
    }

    static boolean testToTextSegment() {
        System.out.println("──────────────────────────────────────");
        System.out.println("测试: toTextSegment()");
        Ai ai = createTestAi();
        String text = ai.toTextSegment();
        System.out.println("输出:");
        System.out.println(text);

        boolean hasGoodsId = text.contains("商品编号 15");
        boolean hasCategory = text.contains("家用电器");
        boolean hasName = text.contains("科沃斯 X2");
        boolean hasPrice = text.contains("4599元");
        boolean hasFeatures = text.contains("方形设计");

        boolean pass = hasGoodsId && hasCategory && hasName && hasPrice && hasFeatures;
        System.out.println("包含商品编号: " + (hasGoodsId ? "通过" : "失败"));
        System.out.println("包含分类: " + (hasCategory ? "通过" : "失败"));
        System.out.println("包含名称: " + (hasName ? "通过" : "失败"));
        System.out.println("包含价格: " + (hasPrice ? "通过" : "失败"));
        System.out.println("包含特性: " + (hasFeatures ? "通过" : "失败"));
        System.out.println("结果: " + (pass ? "通过" : "失败"));
        return pass;
    }

    static boolean testToTextSegmentFormat() {
        System.out.println("──────────────────────────────────────");
        System.out.println("测试: toTextSegment格式（每行以标签开头）");
        Ai ai = createTestAi();
        String text = ai.toTextSegment();
        String[] lines = text.stripIndent().split("\n");

        boolean pass = lines.length == 7
                && lines[0].startsWith("商品编号")
                && lines[1].startsWith("产品类型")
                && lines[2].startsWith("产品种类")
                && lines[3].startsWith("产品名称")
                && lines[4].startsWith("产品价格")
                && lines[5].startsWith("产品简介")
                && lines[6].startsWith("产品特点");

        System.out.println("行数: " + lines.length + " (期望7)");
        for (int i = 0; i < lines.length; i++) {
            System.out.println("  " + (i + 1) + ": " + lines[i]);
        }
        System.out.println("结果: " + (pass ? "通过" : "失败"));
        return pass;
    }

    static boolean testGoodsIdPattern() {
        System.out.println("──────────────────────────────────────");
        System.out.println("测试: 商品编号正则提取");
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("商品编号\\s+(\\d+)");

        String text1 = "商品编号 15\n产品类型 家用电器";
        String text2 = "商品编号 3\n产品类型 电子产品";
        String text3 = "无商品编号的文本";

        java.util.regex.Matcher m1 = pattern.matcher(text1);
        java.util.regex.Matcher m2 = pattern.matcher(text2);
        java.util.regex.Matcher m3 = pattern.matcher(text3);

        boolean p1 = m1.find() && m1.group(1).equals("15");
        boolean p2 = m2.find() && m2.group(1).equals("3");
        boolean p3 = !m3.find();

        boolean pass = p1 && p2 && p3;

        System.out.println("提取15: " + (p1 ? "通过" : "失败"));
        System.out.println("提取3: " + (p2 ? "通过" : "失败"));
        System.out.println("无匹配: " + (p3 ? "通过" : "失败"));
        System.out.println("结果: " + (pass ? "通过" : "失败"));
        return pass;
    }
}