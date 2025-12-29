package de.emn4tor;

import de.emn4tor.ui.FPSCounter;
import de.emn4tor.ui.Hotbar;
import de.emn4tor.ui.HotbarItem;
import de.emn4tor.util.TextureHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;

public class Renderer {

    private final FPSCounter fpsCounter;
    private final long window;

    private STBTTFontinfo fontInfo;
    private ByteBuffer fontBuffer;
    private ByteBuffer bitmapBuffer;
    private STBTTBakedChar.Buffer chardata;
    private int textureId;
    private final int BITMAP_W = 512;
    private final int BITMAP_H = 512;
    private boolean fontLoaded = false;

    private Hotbar hotbar;

    private static final float HUD_PADDING_X = 10.0f;
    private static final float HUD_PADDING_Y = 10.0f;
    private static final float FONT_PX = 32.0f;

    public static int windowWidth;
    public static int windowHeight;

    public Renderer(FPSCounter fpsCounter, long window) {
        this.fpsCounter = fpsCounter;
        this.window = window;

        // Get initial size
        try (var stack = stackPush()) {
            var widthBuf = stack.mallocInt(1);
            var heightBuf = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuf, heightBuf);
            windowWidth = widthBuf.get(0);
            windowHeight = heightBuf.get(0);
        }

        // Set callback to update size on resize
        GLFW.glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            windowWidth = w;
            windowHeight = h;
            glViewport(0, 0, w, h);
            if (hotbar != null) hotbar.updatePositions();
        });

        glColor4f(1f, 1f, 1f, 1f);
        initGLState();
        loadTextures();

        initFont();

        // Create hotbar with 6 items
        hotbar = new Hotbar();
    }

    private void loadTextures() {
        TextureHandler.load(0, "textures/wateringcan.png");
        TextureHandler.load(1, "textures/hoe.png");
        TextureHandler.load(2, "textures/item2.png");
        TextureHandler.load(3, "textures/item3.png");
        TextureHandler.load(4, "textures/item4.png");
        TextureHandler.load(5, "textures/item5.png");
    }

    private void initGLState() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initFont() {
        try (var fontStream = getClass().getClassLoader().getResourceAsStream("fonts/opening.ttf")) {
            if (fontStream == null) {
                System.err.println("Font file not found in resources/fonts/opening.ttf");
                return;
            }

            byte[] fontBytes = fontStream.readAllBytes();
            fontBuffer = ByteBuffer.allocateDirect(fontBytes.length);
            fontBuffer.put(fontBytes).flip();

            fontInfo = STBTTFontinfo.create();
            if (!STBTruetype.stbtt_InitFont(fontInfo, fontBuffer)) {
                System.err.println("Failed to initialize font");
                return;
            }

            bitmapBuffer = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            chardata = STBTTBakedChar.malloc(96);

            int result = STBTruetype.stbtt_BakeFontBitmap(fontBuffer, FONT_PX, bitmapBuffer, BITMAP_W, BITMAP_H, 32, chardata);
            if (result < 0) {
                System.err.println("Failed to bake font, result = " + result);
                return;
            }

            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmapBuffer);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            fontLoaded = true;
        } catch (Exception e) {
            System.err.println("Failed to load font: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void renderBackground() {
        glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void renderFPS() {
        if (!fontLoaded) {
            System.out.println("FPS: " + fpsCounter.getFPS() + " (font not loaded)");
            return;
        }

        String fpsText = "FPS: " + fpsCounter.getFPS();

        try (var stack = stackPush()) {
            var widthBuf = stack.mallocInt(1);
            var heightBuf = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuf, heightBuf);
            int width = widthBuf.get(0);
            int height = heightBuf.get(0);

            glViewport(0, 0, width, height);

            glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_TRANSFORM_BIT | GL_TEXTURE_BIT);
            try {
                glMatrixMode(GL_PROJECTION);
                glPushMatrix();
                glLoadIdentity();
                glOrtho(0, width, height, 0, -1, 1);

                glMatrixMode(GL_MODELVIEW);
                glPushMatrix();
                glLoadIdentity();

                glDisable(GL_DEPTH_TEST);
                glDisable(GL_CULL_FACE);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glEnable(GL_TEXTURE_2D);
                glBindTexture(GL_TEXTURE_2D, textureId);

                glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

                float yBaseline = HUD_PADDING_Y + getFontAscentPx(FONT_PX);
                renderText(fpsText, HUD_PADDING_X, yBaseline);

                glPopMatrix();
                glMatrixMode(GL_PROJECTION);
                glPopMatrix();
                glMatrixMode(GL_MODELVIEW);
            } finally {
                glPopAttrib();
            }
        }
    }

    private float getFontAscentPx(float pixelHeight) {
        try (var stack = stackPush()) {
            var a = stack.mallocInt(1);
            var d = stack.mallocInt(1);
            var g = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, a, d, g);

            float scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, pixelHeight);
            return a.get(0) * scale;
        }
    }

    private void renderText(String text, float x, float yBaseline) {
        try (var stack = stackPush()) {
            FloatBuffer xBuf = stack.floats(x);
            FloatBuffer yBuf = stack.floats(yBaseline);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);

            glBegin(GL_QUADS);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 32 || c >= 128) continue;

                STBTruetype.stbtt_GetBakedQuad(chardata, BITMAP_W, BITMAP_H, c - 32, xBuf, yBuf, quad, false);

                glTexCoord2f(quad.s0(), quad.t0()); glVertex2f(quad.x0(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t0()); glVertex2f(quad.x1(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t1()); glVertex2f(quad.x1(), quad.y1());
                glTexCoord2f(quad.s0(), quad.t1()); glVertex2f(quad.x0(), quad.y1());
            }
            glEnd();
        }
    }

    /** Update hotbar items based on mouse input */
    public void updateBox(float mouseX, float mouseY, boolean leftPressed) {
        hotbar.update(mouseX, mouseY, leftPressed);
    }

    /** Handle hotbar item selection */
    public void handleHotbarClick(float mouseX, float mouseY) {
        hotbar.handleClick(mouseX, mouseY);
    }

    /** Get the currently selected hotbar item */
    public HotbarItem getSelectedItem() {
        return hotbar.getSelectedItem();
    }

    /** Setup orthographic view for farm rendering */
    public void setupFarmView() {
        try (var stack = stackPush()) {
            var widthBuf = stack.mallocInt(1);
            var heightBuf = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuf, heightBuf);
            int width = widthBuf.get(0);
            int height = heightBuf.get(0);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
        }
    }

    /** Render the hotbar items */
    public void renderBox() {
        try (var stack = stackPush()) {
            var widthBuf = stack.mallocInt(1);
            var heightBuf = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuf, heightBuf);
            int width = widthBuf.get(0);
            int height = heightBuf.get(0);

            glPushAttrib(GL_ENABLE_BIT);
            glDisable(GL_TEXTURE_2D);
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            hotbar.render();

            // Render item name if dragging
            HotbarItem draggedItem = hotbar.getDraggedItem();
            if (draggedItem != null && fontLoaded) {
                renderItemName(draggedItem);
            }

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
            glPopAttrib();
        }
    }

    private void renderItemName(HotbarItem item) {
        String name = item.getName();

        // Use smaller font for tooltip
        float tooltipFontSize = FONT_PX * 0.6f; // 60% of normal size

        // Calculate text dimensions more accurately
        float charWidth = tooltipFontSize * 0.55f; // Increased for better width estimation
        float textWidth = name.length() * charWidth;
        float textHeight = tooltipFontSize;

        // Position tooltip above item, centered
        float tooltipPadding = 8f;
        float tooltipWidth = textWidth + tooltipPadding * 2;
        float tooltipHeight = textHeight + tooltipPadding * 2;

        float tooltipX = item.getX() + item.getSize() / 2f - tooltipWidth / 2f;
        float tooltipY = item.getY() - tooltipHeight - 10;

        // Draw tooltip background (semi-transparent black)
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(0.0f, 0.0f, 0.0f, 0.8f);

        glBegin(GL_QUADS);
        glVertex2f(tooltipX, tooltipY);
        glVertex2f(tooltipX + tooltipWidth, tooltipY);
        glVertex2f(tooltipX + tooltipWidth, tooltipY + tooltipHeight);
        glVertex2f(tooltipX, tooltipY + tooltipHeight);
        glEnd();

        // Draw tooltip border (light gray)
        glColor4f(0.6f, 0.6f, 0.6f, 1.0f);
        glLineWidth(1.5f);
        glBegin(GL_LINE_LOOP);
        glVertex2f(tooltipX, tooltipY);
        glVertex2f(tooltipX + tooltipWidth, tooltipY);
        glVertex2f(tooltipX + tooltipWidth, tooltipY + tooltipHeight);
        glVertex2f(tooltipX, tooltipY + tooltipHeight);
        glEnd();

        // Draw text at smaller size
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        float textX = tooltipX + tooltipPadding;
        float textY = tooltipY + tooltipPadding + getFontAscentPx(tooltipFontSize);

        renderTextScaled(name, textX, textY, tooltipFontSize / FONT_PX);

        glDisable(GL_TEXTURE_2D);
    }

    private void renderTextScaled(String text, float x, float yBaseline, float scale) {
        try (var stack = stackPush()) {
            FloatBuffer xBuf = stack.floats(x);
            FloatBuffer yBuf = stack.floats(yBaseline);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);

            glPushMatrix();
            glTranslatef(x, yBaseline, 0);
            glScalef(scale, scale, 1);
            glTranslatef(-x, -yBaseline, 0);

            glBegin(GL_QUADS);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 32 || c >= 128) continue;

                STBTruetype.stbtt_GetBakedQuad(chardata, BITMAP_W, BITMAP_H, c - 32, xBuf, yBuf, quad, false);

                glTexCoord2f(quad.s0(), quad.t0()); glVertex2f(quad.x0(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t0()); glVertex2f(quad.x1(), quad.y0());
                glTexCoord2f(quad.s1(), quad.t1()); glVertex2f(quad.x1(), quad.y1());
                glTexCoord2f(quad.s0(), quad.t1()); glVertex2f(quad.x0(), quad.y1());
            }
            glEnd();

            glPopMatrix();
        }
    }
}