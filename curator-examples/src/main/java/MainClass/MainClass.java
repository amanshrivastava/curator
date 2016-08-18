package MainClass;

import cache.PathCacheExample;

import java.io.InputStreamReader;
import java.util.Scanner;

public class MainClass {
    public static void main(String args[]) throws Exception {
        PathCacheExample pathCacheExample = new PathCacheExample();
        pathCacheExample.add();
        boolean done = false;
        while (!done) {
            Scanner x = new Scanner(System.in);
            String s = x.next();
            if (s.equalsIgnoreCase("exit")) {
                done = true;
            }
        }
        pathCacheExample.remove();
    }
}
