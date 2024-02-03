package net.sourceforge.vrapper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;

public abstract class ProcessHelper {

    public ProcessHelper() { }

    public static Process start(String cmdarray[]) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmdarray);
        return pb.start();
    }

    public static String[] splitArgs(String command) throws Exception {
        ArrayList<String> matchList = new ArrayList<String>();
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(command));
        tokenizer.resetSyntax();
        tokenizer.wordChars(0, 255);
        tokenizer.whitespaceChars(' ', ' ');
        tokenizer.quoteChar('\'');
        tokenizer.quoteChar('"');
        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            matchList.add(tokenizer.sval);
        }
        return (String[]) matchList.toArray(new String[matchList.size()]);
    }

    public static String ReadProcessOutput(BufferedReader stdInput) throws IOException {
        StringBuffer r = new StringBuffer();
        char readBuf[] = new char[1024];
        int bytesRead;
        while ((bytesRead = stdInput.read(readBuf)) > 0) {
            r.append(readBuf, 0, bytesRead);
        }
        return r.toString();
    }

}
