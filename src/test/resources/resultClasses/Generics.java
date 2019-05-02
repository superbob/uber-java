package uberjava;

import java.util.ArrayList;

public class BaseGenerics {

    public static void main(String[] argv) {
        doStuff();
    }

    public static <T extends Something> void doStuff() {
        new ArrayList<T>();
    }
}

class Something {
}
