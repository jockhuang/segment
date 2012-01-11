package com.specl.search;
/**
 * Specl.com Inc.
 * Copyright (c) 2010-2010 All Rights Reserved.
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 即汉字编码分布为：设b1、b2分别为高位字节和低位字节，则0xB0<=b1<=0xF7, 0xA1<=b2<=0xFE 标点符号编码分布：设b1、b2分别为高位字节和低位字节，则0xA1<=b1<=0xA9,
 * 0xA1<=b2<=0xFE
 * @author JOCK
 */
public class SplitWord {
    // 常用词树
    WordTree comwordlib;

    // 噪声词(高频词)树
    WordTree noisywordlib;

    // 构造函数，完成词库词语从文件的载入和词库树的初始化
    // 包括常用词库和噪声词库的初始化
    public SplitWord() {
        // 常用词库树的初始化
        String comwordlibfilename = this.getClass().getResource("/")+"wordlib/comwordlib.lib";
        comwordlibfilename = comwordlibfilename.substring(6);
        comwordlib = new WordTree();
        int wordcount = 0;
        try {

            BufferedReader br = new BufferedReader(new FileReader(comwordlibfilename));
            String aword;
            aword = br.readLine();
            while (aword != null) {
                comwordlib.insertWord(aword);
                wordcount++;

                aword = br.readLine();
            }
            br.close();

        } catch (FileNotFoundException fnfe) {
            System.err.println("\r\nError: can't find the comwordlib file of " + comwordlibfilename);
            System.exit(-1);
        } catch (IOException ioe) {
            System.err.println("\r\nError: while loading comwordlib, " + ioe);
            System.exit(-1);
        }

        // 噪声词库树的初始化
        noisywordlib = new WordTree();
        String noisywordlibfilename = this.getClass().getResource("/")+"wordlib/noisywordlib.lib";
        noisywordlibfilename = noisywordlibfilename.substring(6);
        wordcount = 0;
        try {

            BufferedReader br = new BufferedReader(new FileReader(noisywordlibfilename));
            String aword;
            aword = br.readLine();
            while (aword != null) {
                noisywordlib.insertWord(aword);
                wordcount++;
                aword = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("\r\nError: can't find the noisywordlib file of " + noisywordlibfilename);
            System.exit(-1);
        } catch (IOException ioe) {
            System.err.println("\r\nError: while loading noisywordlib, " + ioe);
            System.exit(-1);
        }

        // 初始化分词结果数组
        initWordArray();
    }

    // 判断单个字节的字符是否字母
    public static boolean isLetter(byte b) {
        if (((b >= 'a') && (b <= 'z')) || ((b >= 'A') && (b <= 'Z'))) {
            return true;
        }
        return false;
    }

    // 判断单个字节的字符是否数字
    public static boolean isDigit(byte b) {
        if ((b >= '0') && (b <= '9')) {
            return true;
        }
        return false;
    }

    // 判断单个字节的字符是否标点符号(除了数字和字母都视为标点符号)
    public static boolean isPunctuation(byte b) {
        if (((b >= 'a') && (b <= 'z')) || ((b >= 'A') && (b <= 'Z')) || ((b >= '0') && (b <= '9'))) {
            return false;
        }
        return true;
    }

    // 判断双字节字符是否是字母(GB2312全角)
    public static boolean isLetter(byte b1, byte b2) {
        int i1 = b1 + 0x100;
        int i2 = b2 + 0x100;

        if ((i1 == 0xA3) && (((i2 >= 0xC1) && (i2 <= 0xDA)) || ((i2 >= 0xE1) && (i2 <= 0xFA)))) {
            return true;
        }
        return false;
    }

    // 判断双字节字符是否是数字(GB2312全角)
    public static boolean isDigit(byte b1, byte b2) {
        int i1 = b1 + 0x100;
        int i2 = b2 + 0x100;

        if ((i1 == 0xA3) && (i2 >= 0xB0) && (i2 <= 0xB9)) {
            return true;
        }
        return false;
    }

    // 判断双字节字符是否标点符号(GB2312)
    public static boolean isPunctuation(byte b1, byte b2) {
        int i1 = b1 + 0x100;
        int i2 = b2 + 0x100;

        if ((i1 < 0xA1) || (i1 > 0xA9) || (i2 < 0xA1) || (i2 > 0xFE) || (isLetter(b1, b2)) || (isDigit(b1, b2))) {
            return false;
        }
        return true;
    }

    // 保存分词结果的数组
    //private static final int MAX_WORD_NUM = 0x10000;
    private List<WordAndPosition> words;
    private int wordnum;

    // 初始化分词结果数组的函数
    private void initWordArray() {
        words = null;
        words = new ArrayList<WordAndPosition>();
        
    }

    // 全角字符的第二个字节和相应半角字符的差值
    // 即：字符A的全角编码的第二个字节(第一个字节都是0xA3) = 字符A的半角编码(ASCII编码) + DIFFERENCE
    private static final byte DIFFERENCE = (0xB0 - 0x100) - 0x30;
    // 用来保存全角字符转换成半角字符结果的数组
    private static final int MAX_CHAR_PER_WORD = 0x100;
    private byte dbccase[] = new byte[MAX_CHAR_PER_WORD];
    private int dbccount;

    // 供返回匹配结果的位置数组
    private int positions[] = new int[0x10];
    private int poscount;

    public List<WordAndPosition> getResult() {
        return words;
    }

    public int getResultNum() {
        return wordnum;
    }

    // 检索服务器程序(多线程共享分词对象)调用接口
    // 本函数对用户输入进行分词，分词算法：最大左匹配，不分离子词语
    public synchronized List<WordAndPosition> segment(String sentence) {
        byte b[] = sentence.getBytes();
        // debug
        /*
         * int bi; for (int j = 0; j < b.length; j ++) { if (b[j] >= 0) { bi = b[j]; } else { bi = b[j] + 0x100; }
         * System.out.print(Integer.toHexString(bi) + " "); } System.out.println();
         */
        int offset = 0;
        int begin;
        String aword;

        wordnum = 0;
        while (offset < b.length) {
            if (b[offset] >= 0) {
                // 如果是单字节字符
                if ((isLetter(b[offset])) || (isDigit(b[offset]))) {
                    // 如果是字母和数字的组合，直接分离单词
                    begin = offset;
                    while ((offset < b.length) && ((isLetter(b[offset])) || (isDigit(b[offset])))) {
                        offset++;
                    }
                    // 为了提高查全率，建立索引时将大写全部转换为小写
                    aword = (new String(b, begin, offset - begin)).toLowerCase();

                    // 如果aword不是噪声词，则放入结果集合
                    if (!noisywordlib.match(aword)) {
                        WordAndPosition word = new WordAndPosition();
                        word.setWord(aword);
                        word.setPosition(begin);
                        words.add(word);
                        wordnum++;
                    }
                } else {
                    // 如果是标点，则不用分离
                    offset++;
                }
            } else {
                // 否则是双字节字符
                if ((offset + 1) >= b.length) {
                    // 如果出现半个双字节字符结尾的情况，不做索引
                    offset++;
                } else {
                    if (isPunctuation(b[offset], b[offset + 1])) {
                        // 如果是标点符号，不用分离
                        offset += 2;
                    } else {
                        if ((isLetter(b[offset], b[offset + 1])) || (isDigit(b[offset], b[offset + 1]))) {
                            // 如果是字母数字串，转换成半角小写字符串建立索引
                            begin = offset;
                            dbccount = 0;
                            while ((isLetter(b[offset], b[offset + 1])) || (isDigit(b[offset], b[offset + 1]))) {
                                dbccase[dbccount] = (byte) (b[offset + 1] - DIFFERENCE);
                                dbccount++;
                                if (dbccount >= MAX_CHAR_PER_WORD) {
                                    break;
                                }

                                offset += 2;
                                if ((offset + 1) >= b.length) {
                                    break;
                                }
                            }
                            aword = (new String(dbccase, 0, dbccount)).toLowerCase();
                            // 如果aword不是噪声词，则放入结果集合
                            if (!noisywordlib.match(aword)) {
                                WordAndPosition word = new WordAndPosition();
                                word.setWord(aword);
                                word.setPosition(begin);
                                words.add(word);
                                
                                wordnum++;
                            }
                        } else {
                            // 然后分离中文字符(也可能是尚未编码的码值)，首先进行左匹配
                            poscount = comwordlib.match(b, offset, positions);
                            if (poscount == 0) {
                                // 如果匹配不成功，则取1个字符(2个字节)建立索引
                                aword = new String(b, offset, 2);
                                if (!noisywordlib.match(aword)) {
                                    WordAndPosition word = new WordAndPosition();
                                    word.setWord(aword);
                                    word.setPosition(offset);
                                    words.add(word);
                                    wordnum++;
                                }
                                offset += 2;
                            } else {
                                // 如果匹配成功，将只对匹配最长的词语建立索引
                                aword = new String(b, offset, positions[poscount - 1] + 1 - offset);
                                if (!noisywordlib.match(aword)) {
                                    WordAndPosition word = new WordAndPosition();
                                    word.setWord(aword);
                                    word.setPosition(offset);
                                    words.add(word);
                                    wordnum++;
                                }
                                // offset向前前进最长匹配词语的长度
                                offset = positions[poscount - 1] + 1;
                            }
                        }
                    }
                }
            }


        }

        
        return words;
    }

    public static void main(String[] args) {
        if (args.length < 1)
            return;
        SplitWord sw = new SplitWord();
        long now = System.currentTimeMillis();
        List<WordAndPosition> w1 = sw.segment(args[0]);
        for (WordAndPosition w:w1) {
            System.out.print(w.getWord()+"(" + w.getPosition()+") ");
        }
        now = System.currentTimeMillis()- now;
        System.out.println("\n"+now+" ms.");
    }
}
