package uberjava;

public class BaseInner2 {

    public static void main(String[] argv) {
        new ImportInner.Inner().doSomething();
    }
}

class ImportInner {

    public static class Inner {

        public void doSomething() {
        }
    }
}
