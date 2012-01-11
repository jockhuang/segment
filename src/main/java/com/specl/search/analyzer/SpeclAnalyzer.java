package com.specl.search.analyzer;

public class SpeclAnalyzer {
    public native static int init(byte[] dict_path);

    public native static long get_segger();

    public native static int return_segger(long segid);

    public native static int hiseg_do(long key, byte[] input, int len);

    public native static int hiseg_next_res(long s, byte[] out, int len);

    public native static int hiseg_do_next_res(long s, byte[] input,
            int input_len, byte[] output, int output_len, int[] output_len_array);

    public native static int destroy();
}
