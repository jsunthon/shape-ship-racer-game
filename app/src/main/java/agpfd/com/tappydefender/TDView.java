package agpfd.com.tappydefender;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import agpfd.com.tappydefender.models.EnemyShip;
import agpfd.com.tappydefender.models.PlayerShip;
import agpfd.com.tappydefender.models.SpaceDust;

/**
 * Created by jsunthon on 5/21/2017.
 */

public class TDView extends SurfaceView implements Runnable {
    private volatile boolean playing;
    Thread gameThread = null;
    private PlayerShip player;

    private EnemyShip enemy1, enemy2, enemy3;

    public List<SpaceDust> dustList = new LinkedList<>();

    //For drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;

    private int screenX, screenY;

    //for hud
    private float distanceRemaining;
    private long timeTaken;
    private long timeStarted;
    private long fastestTime;

    private Context context;

    private boolean gameEnded;

    //sounds
    private SoundPool soundPool;
    private static int start, bump, destroyed, win;

    public TDView(Context context, int x, int y) {
        super(context);
        this.context = context;

        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        start = soundPool.load(context, R.raw.start, 1);
        win = soundPool.load(context, R.raw.win, 1);
        bump = soundPool.load(context, R.raw.bump, 1);
        destroyed = soundPool.load(context, R.raw.destroyed, 1);

        ourHolder = getHolder();
        paint = new Paint();
        screenX = x;
        screenY = y;
        startGame();
    }

    private void startGame() {
        player = new PlayerShip(context, screenX, screenY);
        enemy1 = new EnemyShip(context, screenX, screenY);
        enemy2 = new EnemyShip(context, screenX, screenY);
        enemy3 = new EnemyShip(context, screenX, screenY);

        for (int i = 0; i < 200; i++) {
            SpaceDust spec = new SpaceDust(screenX, screenY);
            dustList.add(spec);
        }

        distanceRemaining = 10000; // 10 km
        timeTaken = 0;

        timeStarted = System.currentTimeMillis();
        gameEnded = false;
        playSound(start);
    }

    private void playSound(int soundId) {
        AudioManager audioManager = (AudioManager)
                context.getSystemService(Context.AUDIO_SERVICE);
        float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundPool.play(soundId, volume, volume, 1, 0, 1);
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        boolean hitDetected = false;
        if (Rect.intersects(player.getHitbox(), enemy1.getHitbox())) {
            hitDetected = true;
            enemy1.setX(-200);
        }

        if (Rect.intersects(player.getHitbox(), enemy2.getHitbox())) {
            hitDetected = true;
            enemy2.setX(-200);
        }

        if (Rect.intersects(player.getHitbox(), enemy3.getHitbox())) {
            hitDetected = true;
            enemy3.setX(-200);
        }

        if (hitDetected) {
            playSound(bump);
            player.reduceShieldStrength();
            if (player.getShieldStrength() < 0) {
                //game over, do
                playSound(destroyed);
                gameEnded = true;
            }
        }

        player.update();

        enemy1.update(player.getSpeed());
        enemy2.update(player.getSpeed());
        enemy3.update(player.getSpeed());

        for (SpaceDust sd : dustList) {
            sd.update(player.getSpeed());
        }

        if (!gameEnded) {
            distanceRemaining -= player.getSpeed();
            timeTaken = System.currentTimeMillis() - timeStarted;
        }

        if (distanceRemaining < 0) {
            playSound(win);
            if (timeTaken < fastestTime) {
                fastestTime = timeTaken;
            }

            distanceRemaining = 0;

            gameEnded = true;
        }
    }

    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();

            //clear the screen
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            paint.setColor(Color.YELLOW);

            canvas.drawRect(player.getHitbox().left,
                    player.getHitbox().top,
                    player.getHitbox().right,
                    player.getHitbox().bottom,
                    paint);

            canvas.drawRect(enemy1.getHitbox().left,
                    enemy1.getHitbox().top,
                    enemy1.getHitbox().right,
                    enemy1.getHitbox().bottom,
                    paint);

            canvas.drawRect(enemy2.getHitbox().left,
                    enemy2.getHitbox().top,
                    enemy2.getHitbox().right,
                    enemy2.getHitbox().bottom,
                    paint);

            canvas.drawRect(enemy3.getHitbox().left,
                    enemy3.getHitbox().top,
                    enemy3.getHitbox().right,
                    enemy3.getHitbox().bottom,
                    paint);

            paint.setColor(Color.argb(255, 255, 255, 255));

            for (SpaceDust sd : dustList) {
                canvas.drawPoint(sd.getX(), sd.getY(), paint);
            }

            //draw the player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint
            );

            canvas.drawBitmap(
                    enemy1.getBitmap(),
                    enemy1.getX(),
                    enemy1.getY(),
                    paint
            );

            canvas.drawBitmap(
                    enemy2.getBitmap(),
                    enemy2.getX(),
                    enemy2.getY(),
                    paint
            );

            canvas.drawBitmap(
                    enemy3.getBitmap(),
                    enemy3.getX(),
                    enemy3.getY(),
                    paint
            );

            if (!gameEnded) {
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(25);
                canvas.drawText("Fastest:" + fastestTime + "s", 10, 20, paint);
                canvas.drawText("Time:" + timeTaken + "s", screenX / 2, 20, paint);
                canvas.drawText("Distance:" + distanceRemaining / 1000 + " KM", screenX / 3, screenY - 20, paint);
                canvas.drawText("Shield:" + player.getShieldStrength(), 10, screenY - 20, paint);
                canvas.drawText("Speed:" +
                        player.getSpeed() * 60 +
                        " MPS", (screenX /3 ) * 2, screenY - 20, paint);
            } else {
                paint.setTextSize(80);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Game Over", screenX / 2, 100, paint);
                paint.setTextSize(25);
                canvas.drawText("Fastest:" + fastestTime + "s", screenX / 2, 160, paint);
                canvas.drawText("Time:" + timeTaken + "s", screenX / 2, 200, paint);
                canvas.drawText("Distance remaining:" + distanceRemaining + " KM", screenX / 2, 240, paint);
                paint.setTextSize(80);
                canvas.drawText("Tap to replay!", screenX / 2, 350, paint);
            }
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {

        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {

        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                player.setBoosting();
                if (gameEnded) {
                    startGame();
                }
                break;
        }
        return true;
    }
}
