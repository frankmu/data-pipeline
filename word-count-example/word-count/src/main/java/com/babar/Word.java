package com.babar;

import java.io.Serializable;

import lombok.Value;

@Value
public class Word implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String word;
    private int count;
}