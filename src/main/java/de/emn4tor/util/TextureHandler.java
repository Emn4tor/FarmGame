package de.emn4tor.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class TextureHandler {

    private static final Map<Integer, Integer> textures = new HashMap<>();

    public static void load(int id, String resourcePath) {
        if (textures.containsKey(id)) return;

        ByteBuffer imageBuffer = loadResourceToBuffer(resourcePath);
        ByteBuffer image;

        int width, height;

        if (imageBuffer == null) {
            System.err.println("Texture not found: " + resourcePath + " → using fallback texture");

            // Create fallback 16x16 checkerboard (black/purple)
            width = 16;
            height = 16;
            image = BufferUtils.createByteBuffer(width * height * 4);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    boolean purple = ((x / 8 + y / 8) % 2 == 0);
                    image.put((byte)(purple ? 128 : 0)); // R
                    image.put((byte)0);                  // G
                    image.put((byte)(purple ? 128 : 0)); // B
                    image.put((byte)255);                // A
                }
            }
            image.flip();
        } else {
            IntBuffer w = BufferUtils.createIntBuffer(1);
            IntBuffer h = BufferUtils.createIntBuffer(1);
            IntBuffer comp = BufferUtils.createIntBuffer(1);

            image = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (image == null) {
                throw new RuntimeException("STB failed to load image: " + resourcePath);
            }
            width = w.get(0);
            height = h.get(0);
        }

        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, image);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Free image memory if loaded via STB
        if (imageBuffer != null) STBImage.stbi_image_free(image);

        textures.put(id, texId);
    }

    private static ByteBuffer loadResourceToBuffer(String path) {
        try (InputStream is = TextureHandler.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) return null;

            byte[] bytes = is.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void bind(int id) {
        Integer tex = textures.get(id);
        if (tex != null) {
            glBindTexture(GL_TEXTURE_2D, tex);
        }
    }

    public static void cleanup() {
        for (int tex : textures.values()) {
            glDeleteTextures(tex);
        }
        textures.clear();
    }
}
