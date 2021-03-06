package scenes;

import entities.Enemy;
import entities.Entity;
import main.Input;
import mote4.scenegraph.Scene;
import mote4.scenegraph.Window;
import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;
import mote4.util.vertex.mesh.MeshMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Peter on 12/3/16.
 */
public class Title implements Scene {

    private Transform trans;
    private int flash, slide, w,h, startdelay, vsyncdisable;
    private float bgfade;
    private boolean start;
    private Mesh help;

    private Ingame ingame;

    public Title() {
        trans = new Transform();
        slide = 200;
        bgfade = 1.5f;
        flash = 0;
        startdelay = 50;
        start = false;
        vsyncdisable = 0;

        help = FontUtils.createString("Press space",.5f,.95f,.03f,.04f);
    }

    @Override
    public void update(double time, double delta) {
        // there are cases where vsync runs unbound
        // messy and imperfect solution
        /*
        if (delta < .001) { // 60fps should be .016, .001 is one millisecond
            vsyncdisable++;
            if (vsyncdisable > 50) {
                System.out.println("Delta has been <1ms for 50 frames, disabling vsync...");
                Window.setVsync(false);
                vsyncdisable = 0;
            }
            return;
        } else
            vsyncdisable = 0;
        */
        if (Input.isKeyNew(Input.Keys.F5) && !System.getProperty("os.name").toLowerCase().contains("mac")) {
            if (Window.isFullscreen()) {
                Window.setWindowedPercent(.75, 171/128f);
            } else {
                Window.setFullscreen();
            }
        }
        if (Input.isKeyNew(Input.Keys.ESCAPE))
            Window.destroy();

        if (slide > 0)
            slide--;
        else {
            flash++;
            flash %= 90;
        }

        if (ingame != null)
        {
            ingame.update(time, delta);
            if (ingame.canResetGame() && Input.isKeyNew(Input.Keys.YES)) {
                ingame.destroy();
                ingame = null;
                slide = 200;
                bgfade = 1.5f;
                flash = 0;
                start = false;
                Entity.resetAll();
                ShaderMap.use("texture");
                Uniform.vec("colorMult",1,1,1,1);

                AudioPlayback.stopMusic();
            }
        }
        else {
            if (start) {
                if (startdelay > 0)
                    startdelay--;
                else if (Input.isKeyNew(Input.Keys.YES)) {
                    ingame = new Ingame();
                    ingame.framebufferResized(w, h);
                    ingame.update(time, delta);
                    AudioPlayback.playSfx("pop");
                    startdelay = 50;
                }
            }
            else if (Input.isKeyNew(Input.Keys.YES))
            {
                start = true;
                startdelay = 50;
                AudioPlayback.playSfx("pop");
            }
        }
    }

    @Override
    public void render(double time, double delta) {

        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        ShaderMap.use("texture");
        trans.bind();

        if (ingame != null) {
            if (Enemy.numEnemies() == 0) {
                TextureMap.bindUnfiltered("bg2");
                MeshMap.render("quad");
                Uniform.vec("colorMult",1,1,1,bgfade);
                TextureMap.bindUnfiltered("bg");
                MeshMap.render("quad");
                Uniform.vec("colorMult",1,1,1,1);
                if (bgfade > 0)
                    bgfade -= .01;
            } else {
                TextureMap.bindUnfiltered("bg");
                MeshMap.render("quad");
            }

            ingame.render(time, delta);
        } else {
            if (startdelay == 50) {
                TextureMap.bindUnfiltered("elon");
                MeshMap.render("quad");

                if (slide == 0) {
                    TextureMap.bindUnfiltered("font_1");
                    trans.model.setIdentity();
                    trans.model.bind();
                    help.render();
                }
            }
            if (start) {
                trans.model.translate(startdelay / 50f, 0);
                trans.model.bind();
                TextureMap.bindUnfiltered("neil");
                MeshMap.render("quad");
                trans.model.setIdentity();

                trans.model.translate(-startdelay / 50f, 0);
                trans.model.bind();
                TextureMap.bindUnfiltered("prestart");
                MeshMap.render("quad");
                trans.model.setIdentity();
                trans.model.bind();
            } else {
                trans.model.translate(0, slide / 100f);
                trans.model.bind();
                TextureMap.bindUnfiltered("title");
                MeshMap.render("quad");
                trans.model.setIdentity();
                trans.model.bind();

                if (flash > 50) {
                    TextureMap.bindUnfiltered("titleflash");
                    MeshMap.render("quad");
                }
            }
        }

        trans.model.setIdentity();
        trans.model.translate(2, 0);
        trans.bind();
        TextureMap.bindUnfiltered("black");
        MeshMap.render("quad");
        trans.model.translate(-4, 0);
        trans.bind();
        MeshMap.render("quad");
        trans.model.setIdentity();
    }

    @Override
    public void framebufferResized(int width, int height) {
        float aspect = (float)width/height;
        trans.projection.setOrthographic(-aspect, -1, aspect, 1, -1, 1);
        trans.view.setIdentity();
        trans.view.scale(171/128f,1,1);

        w = width;
        h = height;
        if (ingame != null)
            ingame.framebufferResized(width, height);
    }

    @Override
    public void destroy() {
        if (ingame != null)
            ingame.destroy();
        help.destroy();
    }
}
