package com.github.truefmartin.exceptions;

import java.util.Arrays;

public class EmptyResultsException extends Exception {
    public EmptyResultsException(String msg){
        super(msg);
    }
    public EmptyResultsException(String msg, Throwable error){
        super(msg, error);
    }
    public static EmptyResultsException fromInput(String ...input) {
        return new EmptyResultsException(
                String.format("no results found with input: %s", Arrays.toString(input)));
    }
}


