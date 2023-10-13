package com.george.nio.c3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <p>
 *     测试使用Files、Path 完成文件的拷贝
 * </p>
 *
 * @author George
 * @date 2023.10.12 16:47
 */
public class PathCopyTest {
    public static void main(String[] args) throws IOException {
        String source = "H:\\software\\Snipaste-2.8";
        String to = "H:\\software\\Snipaste-2.8-copy";

        Files.walk(Paths.get(source)).forEach(file -> {
            System.out.println(file.toString());
            String targetName = file.toString().replace(source, to);
            try {
                if (Files.isDirectory(file)) { // 如果是文件夹则创建文件夹
                    // 不能一次创建多级目录，否则会抛异常 NoSuchFileException
                    Files.createDirectories(Paths.get(targetName));
                } else if (Files.isRegularFile(file)){ // 如果是普通文件则创建文件
                    // 如果文件已存在，会抛异常 FileAlreadyExistsException
                    Files.copy(file, Paths.get(targetName));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
