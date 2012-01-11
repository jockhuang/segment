package com.specl.search;
/**
 * Specl.com Inc.
 * Copyright (c) 2010-2010 All Rights Reserved.
 */

/**
 * 保存词库的树类 即汉字编码分布为：设b1、b2分别为高位字节和低位字节，则0xB0<=b1<=0xF7, 0xA1<=b2<=0xFE
 * 
 * @author JOCK
 */
public class WordTree {
    private WordTreeNode root;

    // 采用ASCII码寻址的词树的层数(对应于采用ASCII码寻址的词语的汉字个数*2)
    private static final int ASCII_LEVEL = 4;

    // 中文汉字GB2312编码的范围
    private static final String ENCODING = "GB2312";
    private static final int B1_LOWER = 0xB0;
    private static final int B1_UPPER = 0xF7;
    private static final int B2_LOWER = 0xA1;
    private static final int B2_UPPER = 0xFE;
    private static final int BOUND[][] = { { B1_LOWER, B1_UPPER }, { B2_LOWER, B2_UPPER } };

    // 构造函数完成root节点的初始化
    public WordTree() {
        // root节点的bword值无关紧要，为它的children分配一个[B1_LOWER, B1_UPPER]的大小
        root = new WordTreeNode();
        root.setIfWord(false);
        root.assignChildren(B1_UPPER - B1_LOWER + 1);
    }

    public void insertWord(String word) {
        int i;
        byte b[] = null;

        b = word.getBytes();

        if (b.length < 2) {
            System.err.println("Error: A Chinese word must include at least 1 Chinese character!");
            return;
        }

        WordTreeNode cursor = root;
        // 是汉字前一个字节还是后一个字节的标志
        int pflag = 0;

        // 前两个字节按ASCII值下标搜索
        int index;
        for (i = 0; (i < ASCII_LEVEL) && (i < b.length); i++) {
            // 将有符号的byte类型转换成0~255之间的int类型
            if (b[i] >= 0) {
                index = b[i];
            } else {
                index = b[i] + 0x100;
            }

            pflag = i % 2;
            if ((index < BOUND[pflag][0]) || (index > BOUND[pflag][1])) {
                System.err.println("Error: insert a word not Chinese or not using encoding of " + ENCODING + ".");
                System.exit(-1);
            }
            // 从ASCII码转换成数组下标值
            index -= BOUND[pflag][0];

            // 如果当前节点尚没有分配子女节点数组，则分配之
            if (cursor.getChildren() == null) {
                cursor.assignChildren(BOUND[pflag][1] - BOUND[pflag][0] + 1);
            }

            // 如果第index子女尚为空，则扩展之
            if (cursor.getChild(index) == null) {
                cursor.setChild(new WordTreeNode(b[i], false), index);
            }
            cursor = cursor.getChild(index);
        }

        // 其余的字节按折半查找算法找到插入位置并插入
        while (i < b.length) {
            cursor = cursor.insertChild(new WordTreeNode(b[i], false));
            i++;
        }
        cursor.setIfWord(true);
    }

    // 判断一个字符串是否和词库中的某个词语匹配的函数
    public boolean match(String word) {
        int i;
        byte b[] = word.getBytes();
        if (b.length < 2) {
            return false;
        }

        WordTreeNode cursor = root;
        int pflag = 0;
        int index;
        for (i = 0; (i < ASCII_LEVEL) && (i < b.length); i++) {
            if (b[i] >= 0) {
                index = b[i];
            } else {
                index = b[i] + 0x100;
            }

            pflag = i % 2;
            index -= BOUND[pflag][0];
            if (cursor.getChildren() == null) {
                return false;
            }

            if ((index < 0) || (index >= cursor.getChildren().length)) {
                return false;
            }
            cursor = cursor.getChild(index);
            if (cursor == null) {
                return false;
            }
        }

        while (i < b.length) {
            cursor = cursor.searchChild(b[i]);
            if (cursor == null) {
                return false;
            }
            i++;
        }

        if (cursor.getIfWord()) {
            return true;
        }
        return false;
    }

    // 对一个字节数组从offset位置开始进行一次最大左匹配的函数
    // 匹配的词语的位置(按byte计)保存到结果数组result[]中，返回匹配的具有相同左部的词语个数
    public int match(byte b[], int offset, int positions[]) {
        int wordcount = 0;

        WordTreeNode cursor = root;
        int pflag = 0;
        int index;
        int i = offset;
        // 按ASCII码寻址匹配
        while ((i < b.length) && ((i - offset) < ASCII_LEVEL)) {
            if (b[i] >= 0) {
                index = b[i];
            } else {
                index = 0x100 + b[i];
            }

            pflag = (i - offset) % 2;
            index -= BOUND[pflag][0];

            if (cursor.getChildren() == null) {
                return wordcount;
            }

            if ((index < 0) || (index >= cursor.getChildren().length)) {
                return wordcount;
            }

            cursor = cursor.getChild(index);
            if (cursor == null) {
                return wordcount;
            }

            if (cursor.getIfWord()) {
                positions[wordcount] = i;
                wordcount++;
                // 如果子词语的个数大于结果数组的长度，则舍弃后面的结果
                if (wordcount >= positions.length) {
                    return wordcount;
                }
            }
            i++;
        }

        // 折半查找匹配
        if (cursor != null) {
            while (i < b.length) {
                cursor = cursor.searchChild(b[i]);
                if (cursor == null) {
                    break;
                }
                if (cursor.getIfWord()) {
                    positions[wordcount] = i;
                    wordcount++;
                    if (wordcount >= positions.length) {
                        break;
                    }
                }
                i++;
            }
        }

        return wordcount;
    }
}
