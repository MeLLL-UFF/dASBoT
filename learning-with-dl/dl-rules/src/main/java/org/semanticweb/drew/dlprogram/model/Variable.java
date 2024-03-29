/* 
 * Copyright (C) 2010, 2011 Guohui Xiao
 * Copyright (C) 2006-2009 Samuel
 */


package org.semanticweb.drew.dlprogram.model;

/**
 * Variable implementation.
 * 
 */
public class Variable implements Term {
	private String name;

	private int hash;

	/**
	 * Constructor that is only visible inside the package. Customer should use {@link CacheManager} to create process unique
	 * variables.
	 * 
	 * @param name name of the term
	 */
	public Variable(String name) {
		this.name = name;
		hash = name.hashCode();
	}

	/**
	 * Get the name of the term.
	 * 
	 * @return name of the term
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Variable clone() {
		return this;
	}

	@Override
	public boolean equals(Object that) {
		return this == that;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}