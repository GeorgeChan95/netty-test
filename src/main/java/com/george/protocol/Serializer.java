package com.george.protocol;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 *     自定义序列化接口，完成对象的序列化和反序列化
 * </p>
 *
 * @author George
 * @date 2023.11.12 16:12
 */
public interface Serializer {

    /**
     * 反序列化，将数组反序列化为对象
     * @param clazz 对象类型
     * @param bytes 数据数组
     * @return
     * @param <T>
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    /**
     * 序列化方法，将对象序列化为数组
     * @param object
     * @return
     * @param <T>
     */
    <T> byte[] serialize(T object);

    /**
     * 自定义序列化算法，
     * 针对不同的类型有不同的序列化反序列化的方式
     */
    @Slf4j
    enum Algorithm implements Serializer {
        JAVA {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    T t = (T) ois.readObject();
                    return t;
                } catch (IOException e) {
                    log.error("java反序列化失败，异常信息：", e);
                    throw new RuntimeException("java反序列化失败", e);
                } catch (ClassNotFoundException e) {
                    log.error("java反序列化失败，异常信息：", e);
                    throw new RuntimeException("java反序列化失败", e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                    return bos.toByteArray();
                } catch (IOException e) {
                    log.error("java序列化失败，异常信息：", e);
                    throw new RuntimeException("java序列化失败");
                }
            }
        },

        JSON {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
                String json = new String(bytes, StandardCharsets.UTF_8);
                return gson.fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Serializer.ClassCodec()).create();
                String json = gson.toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }
    }

    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override             //   String.class
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            // class -> json
            return new JsonPrimitive(src.getName());
        }
    }
}
