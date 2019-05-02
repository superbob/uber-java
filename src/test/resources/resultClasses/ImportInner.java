package uberjava;

public class BaseInner {

    public static void main(String[] argv) {
        new Inner().doSomething();
    }
}

class ImportInner {

    public static class Inner {

        public void doSomething() {
        }
    }
}
