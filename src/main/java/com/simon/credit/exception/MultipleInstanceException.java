package com.simon.credit.exception;

/**
 * 多实例异常
 * @author XUZIMING 2018-06-24
 */
public class MultipleInstanceException extends RuntimeException {
    private static final long serialVersionUID = -30273207816591221L;

    public MultipleInstanceException(Class<?> clazz) {
        super("not allow instantiate again: " + clazz.getName());
    }

}
