package weka.core;

public class AttributeCopyHelper {

    public static Attribute copy(Attribute original, int newIndex) {
        Attribute copy = original.copy(original.name());
        copy.setIndex(newIndex);
        return copy;
    }
}
