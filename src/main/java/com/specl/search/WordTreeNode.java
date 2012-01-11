package com.specl.search;
/**
 * Specl.com Inc.
 * Copyright (c) 2010-2010 All Rights Reserved.
 */

/**
 * 词库在内存中以树的数据结构保存，本类就是这棵树的节点类 词库词语被拆成byte，每个节点保存一个byte
 * 
 * @author JOCK
 */
public class WordTreeNode {
    private byte bword;
    // 从树根下溯到本节点是否构成词语的标志
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

    // 本函数给本节点new一个size大小的children数组
    public void assignChildren(int size) {
        children = new WordTreeNode[size];
    }

    public WordTreeNode[] getChildren() {
        return children;
    }

    // 将第i个子女节点赋值为w
    public void setChild(WordTreeNode w, int i) {
        if ((children == null) || (i >= children.length)) {
            return; // do nothing
        }

        children[i] = w;
    }

    // 得到第i个子女节点
    public WordTreeNode getChild(int i) {
        if (children == null) {
            return null;
        }
        if (i >= children.length) {
            return null;
        }
        return children[i];
    }

    // 根据bword值搜索子女节点
    // 这里用折半搜索算法
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

    // 将节点w插入本节点的子女节点数组，并按bword大小排序，返回刚插入的节点
    // 如果有跟bword值相同的节点存在，不用插入，返回找到的这个节点
    public WordTreeNode insertChild(WordTreeNode w) {
        if (children == null) {
            children = new WordTreeNode[1];
            children[0] = w;
            return w;
        }

        // 用折半查找算法搜索w应插入的位置
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
        // 此时mid就是w应插入的位置

        WordTreeNode[] tmpchildren = children;
        children = new WordTreeNode[tmpchildren.length + 1];
        System.arraycopy(tmpchildren, 0, children, 0, mid);
        children[mid] = w;
        System.arraycopy(tmpchildren, mid, children, mid + 1, tmpchildren.length - mid);
        return w;
    }
}
