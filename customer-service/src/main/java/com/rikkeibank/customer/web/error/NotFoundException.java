package com.rikkeibank.customer.web.error;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String what) { super("Không tìm thấy: " + what); }
}
