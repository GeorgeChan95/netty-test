package com.george.server.service;

/**
 * <p></p>
 *
 * @author George
 * @date 2023.11.18 09:37
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        int i = 1/0;
        return "你好, " + name;
    }
}
