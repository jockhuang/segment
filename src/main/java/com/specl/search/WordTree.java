package com.specl.search;
/**
 * Specl.com Inc.
 * Copyright (c) 2010-2010 All Rights Reserved.
 */

/**
 * ����ʿ������ �����ֱ���ֲ�Ϊ����b1��b2�ֱ�Ϊ��λ�ֽں͵�λ�ֽڣ���0xB0<=b1<=0xF7, 0xA1<=b2<=0xFE
 * 
 * @author JOCK
 */
public class WordTree {
    private WordTreeNode root;

    // ����ASCII��Ѱַ�Ĵ����Ĳ���(��Ӧ�ڲ���ASCII��Ѱַ�Ĵ���ĺ��ָ���*2)
    private static final int ASCII_LEVEL = 4;

    // ���ĺ���GB2312����ķ�Χ
    private static final String ENCODING = "GB2312";
    private static final int B1_LOWER = 0xB0;
    private static final int B1_UPPER = 0xF7;
    private static final int B2_LOWER = 0xA1;
    private static final int B2_UPPER = 0xFE;
    private static final int BOUND[][] = { { B1_LOWER, B1_UPPER }, { B2_LOWER, B2_UPPER } };

    // ���캯�����root�ڵ�ĳ�ʼ��
    public WordTree() {
        // root�ڵ��bwordֵ�޹ؽ�Ҫ��Ϊ����children����һ��[B1_LOWER, B1_UPPER]�Ĵ�С
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
        // �Ǻ���ǰһ���ֽڻ��Ǻ�һ���ֽڵı�־
        int pflag = 0;

        // ǰ�����ֽڰ�ASCIIֵ�±�����
        int index;
        for (i = 0; (i < ASCII_LEVEL) && (i < b.length); i++) {
            // ���з��ŵ�byte����ת����0~255֮���int����
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
            // ��ASCII��ת���������±�ֵ
            index -= BOUND[pflag][0];

            // �����ǰ�ڵ���û�з�����Ů�ڵ����飬�����֮
            if (cursor.getChildren() == null) {
                cursor.assignChildren(BOUND[pflag][1] - BOUND[pflag][0] + 1);
            }

            // �����index��Ů��Ϊ�գ�����չ֮
            if (cursor.getChild(index) == null) {
                cursor.setChild(new WordTreeNode(b[i], false), index);
            }
            cursor = cursor.getChild(index);
        }

        // ������ֽڰ��۰�����㷨�ҵ�����λ�ò�����
        while (i < b.length) {
            cursor = cursor.insertChild(new WordTreeNode(b[i], false));
            i++;
        }
        cursor.setIfWord(true);
    }

    // �ж�һ���ַ����Ƿ�ʹʿ��е�ĳ������ƥ��ĺ���
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

    // ��һ���ֽ������offsetλ�ÿ�ʼ����һ�������ƥ��ĺ���
    // ƥ��Ĵ����λ��(��byte��)���浽�������result[]�У�����ƥ��ľ�����ͬ�󲿵Ĵ������
    public int match(byte b[], int offset, int positions[]) {
        int wordcount = 0;

        WordTreeNode cursor = root;
        int pflag = 0;
        int index;
        int i = offset;
        // ��ASCII��Ѱַƥ��
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
                // ����Ӵ���ĸ������ڽ������ĳ��ȣ�����������Ľ��
                if (wordcount >= positions.length) {
                    return wordcount;
                }
            }
            i++;
        }

        // �۰����ƥ��
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
