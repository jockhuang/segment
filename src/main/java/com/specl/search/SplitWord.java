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
 * �����ֱ���ֲ�Ϊ����b1��b2�ֱ�Ϊ��λ�ֽں͵�λ�ֽڣ���0xB0<=b1<=0xF7, 0xA1<=b2<=0xFE �����ű���ֲ�����b1��b2�ֱ�Ϊ��λ�ֽں͵�λ�ֽڣ���0xA1<=b1<=0xA9,
 * 0xA1<=b2<=0xFE
 * @author JOCK
 */
public class SplitWord {
    // ���ô���
    WordTree comwordlib;

    // ������(��Ƶ��)��
    WordTree noisywordlib;

    // ���캯������ɴʿ������ļ�������ʹʿ����ĳ�ʼ��
    // �������ôʿ�������ʿ�ĳ�ʼ��
    public SplitWord() {
        // ���ôʿ����ĳ�ʼ��
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

        // �����ʿ����ĳ�ʼ��
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

        // ��ʼ���ִʽ������
        initWordArray();
    }

    // �жϵ����ֽڵ��ַ��Ƿ���ĸ
    public static boolean isLetter(byte b) {
        if (((b >= 'a') && (b <= 'z')) || ((b >= 'A') && (b <= 'Z'))) {
            return true;
        }
        return false;
    }

    // �жϵ����ֽڵ��ַ��Ƿ�����
    public static boolean isDigit(byte b) {
        if ((b >= '0') && (b <= '9')) {
            return true;
        }
        return false;
    }

    // �жϵ����ֽڵ��ַ��Ƿ������(�������ֺ���ĸ����Ϊ������)
    public static boolean isPunctuation(byte b) {
        if (((b >= 'a') && (b <= 'z')) || ((b >= 'A') && (b <= 'Z')) || ((b >= '0') && (b <= '9'))) {
            return false;
        }
        return true;
    }

    // �ж�˫�ֽ��ַ��Ƿ�����ĸ(GB2312ȫ��)
    public static boolean isLetter(byte b1, byte b2) {
        int i1 = b1 + 0x100;
        int i2 = b2 + 0x100;

        if ((i1 == 0xA3) && (((i2 >= 0xC1) && (i2 <= 0xDA)) || ((i2 >= 0xE1) && (i2 <= 0xFA)))) {
            return true;
        }
        return false;
    }

    // �ж�˫�ֽ��ַ��Ƿ�������(GB2312ȫ��)
    public static boolean isDigit(byte b1, byte b2) {
        int i1 = b1 + 0x100;
        int i2 = b2 + 0x100;

        if ((i1 == 0xA3) && (i2 >= 0xB0) && (i2 <= 0xB9)) {
            return true;
        }
        return false;
    }

    // �ж�˫�ֽ��ַ��Ƿ������(GB2312)
    public static boolean isPunctuation(byte b1, byte b2) {
        int i1 = b1 + 0x100;
        int i2 = b2 + 0x100;

        if ((i1 < 0xA1) || (i1 > 0xA9) || (i2 < 0xA1) || (i2 > 0xFE) || (isLetter(b1, b2)) || (isDigit(b1, b2))) {
            return false;
        }
        return true;
    }

    // ����ִʽ��������
    //private static final int MAX_WORD_NUM = 0x10000;
    private List<WordAndPosition> words;
    private int wordnum;

    // ��ʼ���ִʽ������ĺ���
    private void initWordArray() {
        words = null;
        words = new ArrayList<WordAndPosition>();
        
    }

    // ȫ���ַ��ĵڶ����ֽں���Ӧ����ַ��Ĳ�ֵ
    // �����ַ�A��ȫ�Ǳ���ĵڶ����ֽ�(��һ���ֽڶ���0xA3) = �ַ�A�İ�Ǳ���(ASCII����) + DIFFERENCE
    private static final byte DIFFERENCE = (0xB0 - 0x100) - 0x30;
    // ��������ȫ���ַ�ת���ɰ���ַ����������
    private static final int MAX_CHAR_PER_WORD = 0x100;
    private byte dbccase[] = new byte[MAX_CHAR_PER_WORD];
    private int dbccount;

    // ������ƥ������λ������
    private int positions[] = new int[0x10];
    private int poscount;

    public List<WordAndPosition> getResult() {
        return words;
    }

    public int getResultNum() {
        return wordnum;
    }

    // ��������������(���̹߳���ִʶ���)���ýӿ�
    // ���������û�������зִʣ��ִ��㷨�������ƥ�䣬�������Ӵ���
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
                // ����ǵ��ֽ��ַ�
                if ((isLetter(b[offset])) || (isDigit(b[offset]))) {
                    // �������ĸ�����ֵ���ϣ�ֱ�ӷ��뵥��
                    begin = offset;
                    while ((offset < b.length) && ((isLetter(b[offset])) || (isDigit(b[offset])))) {
                        offset++;
                    }
                    // Ϊ����߲�ȫ�ʣ���������ʱ����дȫ��ת��ΪСд
                    aword = (new String(b, begin, offset - begin)).toLowerCase();

                    // ���aword���������ʣ������������
                    if (!noisywordlib.match(aword)) {
                        WordAndPosition word = new WordAndPosition();
                        word.setWord(aword);
                        word.setPosition(begin);
                        words.add(word);
                        wordnum++;
                    }
                } else {
                    // ����Ǳ�㣬���÷���
                    offset++;
                }
            } else {
                // ������˫�ֽ��ַ�
                if ((offset + 1) >= b.length) {
                    // ������ְ��˫�ֽ��ַ���β���������������
                    offset++;
                } else {
                    if (isPunctuation(b[offset], b[offset + 1])) {
                        // ����Ǳ����ţ����÷���
                        offset += 2;
                    } else {
                        if ((isLetter(b[offset], b[offset + 1])) || (isDigit(b[offset], b[offset + 1]))) {
                            // �������ĸ���ִ���ת���ɰ��Сд�ַ�����������
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
                            // ���aword���������ʣ������������
                            if (!noisywordlib.match(aword)) {
                                WordAndPosition word = new WordAndPosition();
                                word.setWord(aword);
                                word.setPosition(begin);
                                words.add(word);
                                
                                wordnum++;
                            }
                        } else {
                            // Ȼ����������ַ�(Ҳ��������δ�������ֵ)�����Ƚ�����ƥ��
                            poscount = comwordlib.match(b, offset, positions);
                            if (poscount == 0) {
                                // ���ƥ�䲻�ɹ�����ȡ1���ַ�(2���ֽ�)��������
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
                                // ���ƥ��ɹ�����ֻ��ƥ����Ĵ��ｨ������
                                aword = new String(b, offset, positions[poscount - 1] + 1 - offset);
                                if (!noisywordlib.match(aword)) {
                                    WordAndPosition word = new WordAndPosition();
                                    word.setWord(aword);
                                    word.setPosition(offset);
                                    words.add(word);
                                    wordnum++;
                                }
                                // offset��ǰǰ���ƥ�����ĳ���
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
