package com.specl.search;
/**
 * Specl.com Inc.
 * Copyright (c) 2010-2010 All Rights Reserved.
 */

/**
 * 
 * @author JOCK
 */
public class WordAndPosition {
    // wordΪ�������ӣ�positionΪ�����������ı��еĳ���λ��
    private StringBuffer word;
    private int position;

    public WordAndPosition() {
        word = new StringBuffer();
    }

    public WordAndPosition(String w, int p) {
        word = new StringBuffer(w);
        position = p;
    }

    public void setWord(String w) {
        if (word == null) {
            word = new StringBuffer(w);
        } else {
            word.setLength(0);
            word.append(w);
        }
    }

    public void setPosition(int p) {
        position = p;
    }

    public StringBuffer getWord() {
        return word;
    }

    public int getPosition() {
        return position;
    }
}
