package com.example.utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 图形验证码生成
 */
public class VerifyUtil {
    // 默认验证码字符集
    private static final char[] CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
    // 默认字符数量
    private static final int DEFAULT_SIZE = 4;
    // 默认干扰线数量
    private static final int DEFAULT_LINES = 10;
    // 默认宽度
    private static final int DEFAULT_WIDTH = 80;
    // 默认高度
    private static final int DEFAULT_HEIGHT = 35;
    // 默认字体大小
    private static final int DEFAULT_FONT_SIZE = 25;
    // 默认字体是否倾斜
    private static final boolean DEFAULT_TILT = true;
    // 默认背景颜色
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.LIGHT_GRAY;

    // 配置参数
    private final int size;
    private final int lines;
    private final int width;
    private final int height;
    private final int fontSize;
    private final boolean tilt;
    private final Color backgroundColor;

    /**
     * 验证码图片构造器对象
     */
    public static class Builder {
        private int size = DEFAULT_SIZE;
        private int lines = DEFAULT_LINES;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private int fontSize = DEFAULT_FONT_SIZE;
        private boolean tilt = DEFAULT_TILT;
        private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setLines(int lines) {
            this.lines = lines;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public Builder setTilt(boolean tilt) {
            this.tilt = tilt;
            return this;
        }

        public Builder setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public VerifyUtil build() {
            return new VerifyUtil(this);
        }
    }

    /**
     * 实例化构造器对象
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * 初始化基础参数
     */
    private VerifyUtil(Builder builder) {
        this.size = builder.size;
        this.lines = builder.lines;
        this.width = builder.width;
        this.height = builder.height;
        this.fontSize = builder.fontSize;
        this.tilt = builder.tilt;
        this.backgroundColor = builder.backgroundColor;
    }

    /**
     * 验证码图片随机取色
     */
    private Color getRandomColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * @return 生成随机验证码及图片
     * Object[0]：验证码字符串；
     * Object[1]：验证码图片。
     */
    public Object[] createImage() {
        StringBuilder sb = new StringBuilder();
        // 创建空白图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图片画笔
        Graphics2D graphic = image.createGraphics();
        try {
            // 设置抗锯齿
            graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 设置画笔颜色
            graphic.setColor(backgroundColor);
            // 绘制矩形背景
            graphic.fillRect(0, 0, width, height);

            // 计算每个字符占的宽度，这里预留一个字符的位置用于左右边距
            int codeWidth = width / (size + 1);
            // 字符所处的y轴的坐标
            int y = height * 3 / 4;

            // 画随机字符
            for (int i = 0; i < size; i++) {
                // 设置随机颜色
                graphic.setColor(getRandomColor());
                // 初始化字体
                Font font = new Font(null, Font.BOLD + Font.ITALIC, fontSize);

                if (tilt) {
                    // 随机一个倾斜的角度 -45到45度之间
                    int theta = ThreadLocalRandom.current().nextInt(-45, 46);
                    AffineTransform affineTransform = new AffineTransform();
                    affineTransform.rotate(Math.toRadians(theta), 0, 0);
                    font = font.deriveFont(affineTransform);
                }
                // 设置字体大小
                graphic.setFont(font);

                // 计算当前字符绘制的X轴坐标
                int x = (i * codeWidth) + (codeWidth / 2);

                // 取随机字符索引
                int n = ThreadLocalRandom.current().nextInt(CHARS.length);
                // 得到字符文本
                String code = String.valueOf(CHARS[n]);
                // 画字符
                graphic.drawString(code, x, y);

                // 记录字符
                sb.append(code);
            }

            // 画干扰线
            for (int i = 0; i < lines; i++) {
                // 设置随机颜色
                graphic.setColor(getRandomColor());
                // 随机画线
                graphic.drawLine(
                        ThreadLocalRandom.current().nextInt(width),
                        ThreadLocalRandom.current().nextInt(height),
                        ThreadLocalRandom.current().nextInt(width),
                        ThreadLocalRandom.current().nextInt(height)
                );
            }
        } finally {
            // 释放图形上下文资源
            if (graphic != null) {
                graphic.dispose();
            }
        }
        // 返回验证码和图片
        return new Object[]{sb.toString(), image};
    }
}