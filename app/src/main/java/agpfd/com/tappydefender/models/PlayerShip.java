package agpfd.com.tappydefender.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import agpfd.com.tappydefender.R;

/**
 * Created by jsunthon on 5/21/2017.
 */

public class PlayerShip {

    private final int GRAVITY = -12;

    //stop the ship from leaving the screen
    private int maxY;
    private int minY;

    private final int MIN_SPEED = 1;
    private final int MAX_SPEED = 20;

    private Bitmap bitmap;
    private int x, y;
    private int speed = 0;
    private boolean boosting;

    private Rect hitBox;

    private int shieldStrength;

    public PlayerShip(Context context, int screenX, int screenY) {
        x = 50;
        y = 50;
        speed = 1;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);
        maxY = screenY - bitmap.getHeight();
        minY = 0;
        shieldStrength = 2;
        hitBox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    public void update() {
        // Are we boosting?
        if (boosting) {
            // Speed up
            speed += 2;
        } else {
            // Slow down
            speed -= 5;
        }

        // Constrain top speed
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }

        // Never stop completely
        if (speed < MIN_SPEED) {
            speed = MIN_SPEED;
        }

        // move the ship up or down
        y -= speed + GRAVITY;

        // But don't let ship stray off screen
        if (y < minY) {
            y = minY;
        }

        if (y > maxY) {
            y = maxY;
        }

        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + bitmap.getWidth();
        hitBox.bottom = y + bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }

    public void setBoosting() {
        boosting = true;
    }

    public void stopBoosting() {
        boosting = false;
    }


    public Rect getHitbox() {
        return hitBox;
    }

    public int getShieldStrength() {
        return shieldStrength;
    }

    public void reduceShieldStrength() {
        shieldStrength--;
    }
}
