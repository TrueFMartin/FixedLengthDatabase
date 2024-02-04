package com.github.truefmartin.exceptions;

public class DatabaseIsClosedException extends Exception {
    public DatabaseIsClosedException(String msg){
        super(msg);
    }
    public DatabaseIsClosedException(String msg, Throwable error){
        super(msg, error);
    }
}


