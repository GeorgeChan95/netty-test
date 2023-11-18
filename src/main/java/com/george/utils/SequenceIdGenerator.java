package com.george.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.11.18 11:08
 */
public abstract class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }
}
