package com.github.truefmartin.exceptions;

public class ColumnBoundsException extends Exception {
    public ColumnBoundsException(String msg){
        super(msg);
    }
    public ColumnBoundsException(String msg, Throwable error){
        super(msg, error);
    }
}


