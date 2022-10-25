/**
 * Integer Wrapper Class to keep reference to an integer out of scope.
 */
public class IntegerWrapper {
    int val;

    /**
     * Creates an IntegerWrapper with a given value.
     * @param newVal
     */
    public IntegerWrapper(int newVal) {
        this.val = newVal;
    }

    /**
     * Sets the wrapper value to the given value.
     * @param newVal
     */
    public void setValue(int newVal) {
        this.val = newVal;
    }

    /**
     * Adds the given value to the wrapper value.
     * @param add
     */
    public void incrementValue(int add) {
        this.val+=add;
    }

    /**
     * Returns the value of the wrapper.
     * @return int
     */
    public int getValue() {
        return this.val;
    }
}
