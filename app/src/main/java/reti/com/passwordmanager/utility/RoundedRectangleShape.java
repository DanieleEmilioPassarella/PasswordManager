package reti.com.passwordmanager.utility;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import uk.co.deanwild.materialshowcaseview.shape.Shape;
import uk.co.deanwild.materialshowcaseview.target.Target;

/**
 * Created by passada1 on 23/09/2017.
 */

public class RoundedRectangleShape implements Shape {

    private boolean fullWidth;
    private int width;
    private int height;
    private boolean adjustToTarget;
    private Rect rect;

    public RoundedRectangleShape(int width, int height) {
        this.fullWidth = false;
        this.width = 0;
        this.height = 0;
        this.adjustToTarget = true;
        this.width = width;
        this.height = height;
        this.init();
    }

    private void init() {
        this.rect = new Rect(-this.width / 2, -this.height / 2, this.width / 2, this.height / 2);
    }

    @TargetApi(21)
    public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
        if(!this.rect.isEmpty()) {
            canvas.drawRoundRect((float)(this.rect.left + x - padding),(float)(this.rect.top + y - padding), (float)(this.rect.right + x + padding), (float)(this.rect.bottom + y + padding),30,30, paint);
        }

    }

    public RoundedRectangleShape(Rect bounds) {
        this(bounds, false);
    }

    public RoundedRectangleShape(Rect bounds, boolean fullWidth) {
        this.fullWidth = false;
        this.width = 0;
        this.height = 0;
        this.adjustToTarget = true;
        this.fullWidth = fullWidth;
        this.height = bounds.height();
        if(fullWidth) {
            this.width = 2147483647;
        } else {
            this.width = bounds.width();
        }

        this.init();
    }

    public void updateTarget(Target target) {
        if(this.adjustToTarget) {
            Rect bounds = target.getBounds();
            this.height = bounds.height();
            if(this.fullWidth) {
                this.width = 2147483647;
            } else {
                this.width = bounds.width();
            }

            this.init();
        }

    }

    public boolean isAdjustToTarget() {
        return this.adjustToTarget;
    }

    public void setAdjustToTarget(boolean adjustToTarget) {
        this.adjustToTarget = adjustToTarget;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
