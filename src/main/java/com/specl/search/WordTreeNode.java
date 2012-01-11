package com.specl.search;
/**
 * Specl.com Inc.
 * Copyright (c) 2010-2010 All Rights Reserved.
 */

/**
 * �ʿ����ڴ������������ݽṹ���棬�������������Ľڵ��� �ʿ���ﱻ���byte��ÿ���ڵ㱣��һ��byte
 * 
 * @author JOCK
 */
public class WordTreeNode {
    private byte bword;
    // ���������ݵ����ڵ��Ƿ񹹳ɴ���ı�־
    private boolean ifword;

    private WordTreeNode[] children = null;

    public WordTreeNode() {
    }

    public WordTreeNode(byte b, boolean i) {
        bword = b;
        ifword = i;
    }

    public void setIfWord(boolean i) {
        ifword = i;
    }

    public boolean getIfWord() {
        return ifword;
    }

    public void setBWord(byte b) {
        bword = b;
    }

    public byte getBWord() {
        return bword;
    }

    // �����������ڵ�newһ��size��С��children����
    public void assignChildren(int size) {
        children = new WordTreeNode[size];
    }

    public WordTreeNode[] getChildren() {
        return children;
    }

    // ����i����Ů�ڵ㸳ֵΪw
    public void setChild(WordTreeNode w, int i) {
        if ((children == null) || (i >= children.length)) {
            return; // do nothing
        }

        children[i] = w;
    }

    // �õ���i����Ů�ڵ�
    public WordTreeNode getChild(int i) {
        if (children == null) {
            return null;
        }
        if (i >= children.length) {
            return null;
        }
        return children[i];
    }

    // ����bwordֵ������Ů�ڵ�
    // �������۰������㷨
    public WordTreeNode searchChild(byte b) {
        if (children == null) {
            return null;
        }

        int start = 0;
        int end = children.length - 1;
        int mid;
        while (start <= end) {
            mid = (start + end) / 2;
            if (children[mid].getBWord() == b) {
                return children[mid];
            }
            if (children[mid].getBWord() > b) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }
        return null;
    }

    // ���ڵ�w���뱾�ڵ����Ů�ڵ����飬����bword��С���򣬷��ظղ���Ľڵ�
    // ����и�bwordֵ��ͬ�Ľڵ���ڣ����ò��룬�����ҵ�������ڵ�
    public WordTreeNode insertChild(WordTreeNode w) {
        if (children == null) {
            children = new WordTreeNode[1];
            children[0] = w;
            return w;
        }

        // ���۰�����㷨����wӦ�����λ��
        int start = 0;
        int end = children.length - 1;
        int mid = (start + end) / 2;
        byte b = w.getBWord();
        while (start <= end) {
            mid = (start + end) / 2;
            if (children[mid].getBWord() == b) {
                return children[mid]; // do nothing
            }
            if (children[mid].getBWord() > b) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }
        if (children[mid].getBWord() < b) {
            mid += 1;
        }
        // ��ʱmid����wӦ�����λ��

        WordTreeNode[] tmpchildren = children;
        children = new WordTreeNode[tmpchildren.length + 1];
        System.arraycopy(tmpchildren, 0, children, 0, mid);
        children[mid] = w;
        System.arraycopy(tmpchildren, mid, children, mid + 1, tmpchildren.length - mid);
        return w;
    }
}
