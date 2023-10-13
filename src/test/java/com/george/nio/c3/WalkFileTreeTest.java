package com.george.nio.c3;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <p>
 *     测试使用Files.walkFileTree实现文件的删除
 * </p>
 *
 * @author George
 * @date 2023.10.12 17:23
 */
public class WalkFileTreeTest {
    public static void main(String[] args) throws IOException {
        Files.walkFileTree(Paths.get("H:\\software\\Snipaste-2.8-copy"), new SimpleFileVisitor<Path>() {
            /**
             * 进入文件前执行
             * @param dir
             *          a reference to the directory
             * @param attrs
             *          the directory's basic attributes
             *
             * @return
             * @throws IOException
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return super.preVisitDirectory(dir, attrs);
            }

            /**
             * 访问文件时执行
             * @param file
             *          a reference to the file
             * @param attrs
             *          the file's basic attributes
             *
             * @return
             * @throws IOException
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            /**
             * 访问文件失败时执行
             * @param file
             *          a reference to the file
             * @param exc
             *          the I/O exception that prevented the file from being visited
             *
             * @return
             * @throws IOException
             */
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            /**
             * 退出访问文件后执行
             * @param dir
             *          a reference to the directory
             * @param exc
             *          {@code null} if the iteration of the directory completes without
             *          an error; otherwise the I/O exception that caused the iteration
             *          of the directory to complete prematurely
             *
             * @return
             * @throws IOException
             */
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
