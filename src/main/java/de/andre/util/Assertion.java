package de.andre.util;

public class Assertion {

    public static void notNull(Object obj, ErrorMsgCreator errorMsgCreator) {
	if (obj == null) {
	    throw new AssertionException(errorMsgCreator.createMessage(obj));
	}
    }

    public static class AssertionException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AssertionException(String errorMsg) {
	    super(errorMsg);
	}
    }

    @FunctionalInterface
    public static interface ErrorMsgCreator {
	String createMessage(Object x);
    }
}
