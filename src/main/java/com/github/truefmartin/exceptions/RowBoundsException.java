package com.github.truefmartin.exceptions;

public class RowBoundsException extends Exception {
    public RowBoundsException(String msg){
        super(msg);
    }
    public RowBoundsException(String msg, Throwable error){
        super(msg, error);
    }
}
