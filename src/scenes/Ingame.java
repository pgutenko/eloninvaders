package scenes;

import entities.Boss;
import entities.Enemy;
import entities.Entity;
import entities.Player;
import mote4.scenegraph.Scene;
import mote4.util.audio.AudioPlayback;
import mote4.util.matrix.Transform;
import mote4.util.shader.ShaderMap;
import mote4.util.shader.Uniform;
import mote4.util.texture.TextureMap;
import mote4.util.vertex.FontUtils;
import mote4.util.vertex.mesh.Mesh;

/**
 * Created by Peter on 12/2/16.
 */
public class Ingame implements Scene {

    private Transform trans;
    private Mesh hits, gameover, gamewon;
    private Player player;
    private Boss boss;
    private int lasthits = 0;
    private boolean loose = false, win = false;

    private static Mesh text;
    private static double credit;

    public Ingame() {
        trans = new Transform();
        player = new Player();
        boss = new Boss();
        Entity.add(player);
        Entity.add(boss);
        Enemy.initEnemyState(6, 10);

        credit = 11.5;

        //text = FontUtils.createString("CREDIT: 11BN",-1.3f,-.95f,.08f,.08f);
        text = FontUtils.createString("CREDIT: "+credit+"BN",-1.3f,-.95f,.08f,.08f);
        lasthits = player.health();
        hits = FontUtils.createString("HP: "+lasthits,.7f,-.95f,.08f,.08f);

        gameover = FontUtils.createString("GAME OVER",-.7f,0,.16f,.16f);
        gamewon = FontUtils.createString("    YOU WIN\nCONGRATULATIONS",-1.2f,-.2f,.16f,.16f);
    }
    public static void looseCredit() {
        credit -= .5;
        String s = "";
        //if (Math.abs(credit)%2 == 1)
        //    s = ".5";
        text.destroy();
        text = FontUtils.createString("CREDIT: "+credit+s+"BN",-1.3f,-.95f,.08f,.08f);
    }

    @Override
    public void update(double time, double delta) {
        if (player.health() != lasthits) {
            lasthits = player.health();
            hits.destroy();
            hits = FontUtils.createString("HP: " + lasthits, .7f, -.95f, .08f, .08f);

            if (lasthits <= 0 && !win) {
                AudioPlayback.stopMusic();
                loose = true;
            }
        }
        if (boss.phase() > 1 && !loose)
            win = true;
        Entity.updateAll();
    }

    @Override
    public void render(double time, double delta) {
        //glClear(GL_COLOR_BUFFER_BIT);
        //glDisable(GL_DEPTH_TEST);

        ShaderMap.use("texture");
        TextureMap.bindUnfiltered("font_1");
        trans.model.setIdentity();
        trans.bind();
        text.render();
        hits.render();

        ShaderMap.use("sprite");
        //trans.view.rotate(.1f,0,0,1);
        trans.model.setIdentity();
        trans.bind();

        Entity.renderAll(trans.model);

        if (loose || win) {
            ShaderMap.use("texture");
            TextureMap.bindUnfiltered("font_1");
            trans.model.setIdentity();
            trans.bind();
            if (loose) {
                Uniform.vec("colorMult",0,0,0,1);
                gameover.render();
                trans.model.translate(-.02f,-.02f);
                trans.model.bind();
                Uniform.vec("colorMult",1,0,0,1);
                gameover.render();
            }
            if (win)
                gamewon.render();
        }
    }

    public boolean canResetGame() { return loose || win; }

    @Override
    public void framebufferResized(int width, int height) {
        float aspect = (float)width/height;
        trans.projection.setOrthographic(-aspect, -1, aspect, 1, -1, 1);
        //trans.projection.setOrthographic(0, 0, width, height, -1, 1);
    }

    @Override
    public void destroy() {
        text.destroy();
        hits.destroy();
        gameover.destroy();
        gamewon.destroy();
    }
}
