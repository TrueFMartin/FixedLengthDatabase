package com.github.truefmartin.exceptions;

public class DatabaseAlreadyOpenException extends Exception {
    public DatabaseAlreadyOpenException(String msg){
        super(msg);
    }
    public DatabaseAlreadyOpenException(String msg, Throwable error){
        super(msg, error);
    }
}


