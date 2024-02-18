package com.github.truefmartin.records;

/**
 * Abstract class to be extended to allow interaction with FixedRecordsAccess.
 */
public abstract class Writeable {

    protected int NUM_ATTRIBUTES;
    protected String[] attributes;
    protected String[] attributeNames;
    private int next = 0;

    private int rowNum;

    Writeable(String... attributes) {
        this.rowNum = -1;
        this.attributes = attributes;
        this.NUM_ATTRIBUTES = attributes.length;
        initAttributeNames();
    }
    public String getNext() {
        return attributes[next++];
    }

    public void setNext(String data) {
        attributes[next++] = data;
        // After all columns have been set, reassign the field variables with columns values
        if (next == NUM_ATTRIBUTES) {
            reassignFields();
        }
    }

    public boolean setAttributeValue(int col, String in) {
        if (col < 0 || col >= NUM_ATTRIBUTES) {
            return false;
        }
        attributes[col] = in;
        reassignFields();
        return true;
    }

    public boolean hasNext() {
        return next < NUM_ATTRIBUTES;
    }

    public void resetNext() {
        next = 0;
    }

    public String getValue(int columnNumber) {
        return columnNumber < attributes.length? attributes[columnNumber]: null;
    }

    public int getNumAttributes() {return NUM_ATTRIBUTES;}

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getAttributeName(int column) {
        return attributeNames[column];
    }

    public boolean isFirstFieldEmpty() {
        return attributes[0].strip().equals("-1");
    }
    abstract public void reassignFields();
    abstract public void print();
    abstract public boolean isEmpty();
    abstract public void printAttributeNames();
    abstract protected void initAttributeNames();
}
