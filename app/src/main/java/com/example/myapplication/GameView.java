package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    public interface OnScoreChangedListener { void onScoreChanged(int score); }

    private final Paint bugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint();
    private final Random random = new Random();
    private final Handler handler = new Handler();

    private final List<Bug> bugs = new ArrayList<>();

    private int widthPx;
    private int heightPx;

    private int speedMultiplier = 1;
    private int maxBugs = 10;
    @SuppressWarnings("FieldCanBeLocal")
    private int bonusIntervalSec = 15;

    private int score = 0;
    private boolean running = false;

    private long lastSpawnMs = 0L;

    private OnScoreChangedListener scoreListener;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            long now = System.currentTimeMillis();
            updatePositions();
            spawnLogic(now);
            invalidate();
            handler.postDelayed(this, 16); // ~60 FPS
        }
    };

    public GameView(Context context) { super(context); init(); }
    public GameView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public GameView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        bgPaint.setColor(Color.WHITE);
        bugPaint.setStyle(Paint.Style.FILL);
        setClickable(true);
    }

    public void configure(int speedMultiplier, int maxBugs, int bonusIntervalSec) {
        this.speedMultiplier = Math.max(1, speedMultiplier);
        this.maxBugs = Math.max(1, maxBugs);
        this.bonusIntervalSec = Math.max(1, bonusIntervalSec);
    }

    public void setOnScoreChangedListener(OnScoreChangedListener listener) { this.scoreListener = listener; }

    public void start() {
        if (running) return;
        running = true;
        lastSpawnMs = System.currentTimeMillis();
        handler.post(tick);
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void resetGame() {
        score = 0;
        if (scoreListener != null) scoreListener.onScoreChanged(score);
        bugs.clear();
        invalidate();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        widthPx = w; heightPx = h;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#FAFAFA"));

        for (Bug bug : bugs) {
            if (bug.drawable != null) {
                int half = (int) bug.radius;
                bug.drawable.setBounds((int)(bug.x - half), (int)(bug.y - half), (int)(bug.x + half), (int)(bug.y + half));
                bug.drawable.draw(canvas);
            } else {
                bugPaint.setColor(bug.color);
                canvas.drawCircle(bug.x, bug.y, bug.radius, bugPaint);
            }
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float tx = event.getX();
            float ty = event.getY();
            boolean hit = false;
            Iterator<Bug> it = bugs.iterator();
            while (it.hasNext()) {
                Bug b = it.next();
                float dx = tx - b.x;
                float dy = ty - b.y;
                if (dx*dx + dy*dy <= b.radius * b.radius) {
                    score += b.points;
                    if (scoreListener != null) scoreListener.onScoreChanged(score);
                    it.remove();
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                score = Math.max(0, score - 1); // штраф
                if (scoreListener != null) scoreListener.onScoreChanged(score);
            }
        }
        return super.onTouchEvent(event) || true;
    }

    private void spawnLogic(long now) {
        if (bugs.size() < maxBugs) {
            if (now - lastSpawnMs > 500) {
                spawnBug();
                lastSpawnMs = now;
            }
        }
    }

    private void spawnBug() {
        float radius = 32 + random.nextInt(20);
        float x = radius + random.nextInt(Math.max(1, (int)(widthPx - 2*radius)));
        float y = radius + random.nextInt(Math.max(1, (int)(heightPx - 2*radius)));
        float base = 2f + random.nextFloat() * 3f;
        float vx = (random.nextBoolean() ? 1 : -1) * base * speedMultiplier;
        float vy = (random.nextBoolean() ? 1 : -1) * base * speedMultiplier;
        int type = random.nextInt(3);
        int drawableRes = (type == 0) ? R.drawable.bug_black : (type == 1) ? R.drawable.bug_brown : R.drawable.bug_red;
        int points = (type == 2) ? 5 : (type == 0 ? 3 : 2);
        Bug b = new Bug(x, y, vx, vy, radius, Color.BLACK, points);
        try {
            b.drawable = getContext().getDrawable(drawableRes);
        } catch (Exception ignored) {}
        bugs.add(b);
    }

    private int randomBugColor() {
        int[] colors = new int[]{ Color.RED, Color.BLACK, Color.parseColor("#795548"), Color.GREEN, Color.BLUE };
        return colors[random.nextInt(colors.length)];
    }

    private int colorPoints(int color) {
        if (color == Color.RED) return 5;
        if (color == Color.BLACK) return 3;
        if (color == Color.GREEN) return 2;
        return 1;
    }

    private void updatePositions() {
        for (Bug b : bugs) {
            b.x += b.vx;
            b.y += b.vy;
            if (b.x - b.radius < 0) { b.x = b.radius; b.vx = Math.abs(b.vx); }
            if (b.x + b.radius > widthPx) { b.x = widthPx - b.radius; b.vx = -Math.abs(b.vx); }
            if (b.y - b.radius < 0) { b.y = b.radius; b.vy = Math.abs(b.vy); }
            if (b.y + b.radius > heightPx) { b.y = heightPx - b.radius; b.vy = -Math.abs(b.vy); }
        }
    }

    private static class Bug {
        float x, y, vx, vy, radius;
        int color;
        int points;
        android.graphics.drawable.Drawable drawable;
        Bug(float x, float y, float vx, float vy, float radius, int color, int points) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.radius = radius; this.color = color; this.points = points;
        }
    }
}


