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
    public interface OnBonusActivatedListener { void onBonusActivated(); }

    private final Paint bugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint();
    private final Paint bonusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final Handler handler = new Handler();

    private final List<Bug> bugs = new ArrayList<>();

    private int widthPx;
    private int heightPx;

    private int speedMultiplier = 1;
    private int maxBugs = 10;
    private int bonusIntervalSec = 15;

    private int score = 0;
    private boolean running = false;

    private long lastSpawnMs = 0L;
    private long lastBonusMs = 0L;
    private long bonusStartTime = 0L;
    private boolean bonusActive = false;
    private float bonusX, bonusY;
    private final float bonusRadius = 60f;
    private final int bonusDurationMs = 5000; // 5 секунд активен бонус

    // Золотой таракан
    private long lastGoldenBugMs = 0L;
    private boolean goldenBugActive = false;
    private float goldenBugX, goldenBugY;
    private final float goldenBugRadius = 50f;
    private final int goldenBugIntervalSec = 20; // каждые 20 секунд
    private double goldRate = 100.0; // курс золота по умолчанию

    private OnScoreChangedListener scoreListener;
    private OnBonusActivatedListener bonusListener;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!running) return;
            long now = System.currentTimeMillis();
            updatePositions();
            spawnLogic(now);
            bonusLogic(now);
            goldenBugLogic(now);
            checkBonusDuration(now);
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
        bonusPaint.setColor(Color.parseColor("#FFD700")); // Золотой цвет для бонуса
        bonusPaint.setStyle(Paint.Style.FILL);
        bonusPaint.setTextSize(24f);
        bonusPaint.setTextAlign(Paint.Align.CENTER);
        setClickable(true);
    }

    public void configure(int speedMultiplier, int maxBugs, int bonusIntervalSec) {
        this.speedMultiplier = Math.max(1, speedMultiplier);
        this.maxBugs = Math.max(1, maxBugs);
        this.bonusIntervalSec = Math.max(1, bonusIntervalSec);
    }

    public void setOnScoreChangedListener(OnScoreChangedListener listener) { this.scoreListener = listener; }
    public void setOnBonusActivatedListener(OnBonusActivatedListener listener) { this.bonusListener = listener; }

    public void start() {
        if (running) return;
        running = true;
        lastSpawnMs = System.currentTimeMillis();
        lastBonusMs = System.currentTimeMillis(); // Инициализируем время последнего бонуса
        lastGoldenBugMs = System.currentTimeMillis(); // Инициализируем время последнего золотого таракана
        handler.post(tick);
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }
    
    public void resume() {
        if (!running) {
            running = true;
            handler.post(tick);
        }
    }

    public void resetGame() {
        score = 0;
        if (scoreListener != null) scoreListener.onScoreChanged(score);
        bugs.clear();
        bonusActive = false;
        lastBonusMs = 0L;
        goldenBugActive = false;
        lastGoldenBugMs = 0L;
        invalidate();
    }

    public int getScore() { return score; }

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

        // Рисуем бонус если он активен
        if (bonusActive) {
            // Рисуем внешнее кольцо для большей заметности
            Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringPaint.setColor(Color.parseColor("#FFA500")); // Оранжевый
            ringPaint.setStyle(Paint.Style.STROKE);
            ringPaint.setStrokeWidth(8f);
            canvas.drawCircle(bonusX, bonusY, bonusRadius + 10, ringPaint);
            
            // Основной круг бонуса
            canvas.drawCircle(bonusX, bonusY, bonusRadius, bonusPaint);
            
            // Текст
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(36f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("G", bonusX, bonusY + 12, textPaint);
        }

        // Рисуем золотого таракана если он активен
        if (goldenBugActive) {
            Paint goldenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            goldenPaint.setColor(Color.parseColor("#FFD700"));
            goldenPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(goldenBugX, goldenBugY, goldenBugRadius, goldenPaint);
            
            // Добавляем блеск
            Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sparklePaint.setColor(Color.parseColor("#FFFF00"));
            sparklePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(goldenBugX - 15, goldenBugY - 15, 8, sparklePaint);
            canvas.drawCircle(goldenBugX + 15, goldenBugY - 10, 6, sparklePaint);
            canvas.drawCircle(goldenBugX - 10, goldenBugY + 15, 7, sparklePaint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float tx = event.getX();
            float ty = event.getY();
            
            // Проверяем попадание по бонусу
            if (bonusActive) {
                float dx = tx - bonusX;
                float dy = ty - bonusY;
                if (dx*dx + dy*dy <= bonusRadius * bonusRadius) {
                    activateBonus();
                    return true;
                }
            }
            
            // Проверяем попадание по золотому таракану
            if (goldenBugActive) {
                float dx = tx - goldenBugX;
                float dy = ty - goldenBugY;
                if (dx*dx + dy*dy <= goldenBugRadius * goldenBugRadius) {
                    int goldPoints = (int)(goldRate / 10); // очки пропорциональны курсу золота
                    score += Math.max(1, goldPoints);
                    if (scoreListener != null) scoreListener.onScoreChanged(score);
                    goldenBugActive = false;
                    return true;
                }
            }
            
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

    private void bonusLogic(long now) {
        // Создаем бонус каждые 15 секунд
        if (!bonusActive && now - lastBonusMs > bonusIntervalSec * 1000L) {
            spawnBonus();
            lastBonusMs = now;
        }
        
        // Временная отладка - можно удалить позже
        if (!bonusActive && (now - lastBonusMs) % 5000 < 100) { // Логируем каждые 5 секунд
            long timeUntilBonus = bonusIntervalSec * 1000L - (now - lastBonusMs);
            android.util.Log.d("GameView", "Time until bonus: " + timeUntilBonus + "ms, bonusInterval: " + bonusIntervalSec);
        }
    }

    private void spawnBonus() {
        bonusX = bonusRadius + random.nextInt(Math.max(1, (int)(widthPx - 2*bonusRadius)));
        bonusY = bonusRadius + random.nextInt(Math.max(1, (int)(heightPx - 2*bonusRadius)));
        bonusActive = true;
        // Временная отладка - можно удалить позже
        android.util.Log.d("GameView", "Bonus spawned at: " + bonusX + ", " + bonusY + ", active: " + bonusActive);
    }

    private void activateBonus() {
        bonusActive = false;
        bonusStartTime = System.currentTimeMillis();
        if (bonusListener != null) {
            bonusListener.onBonusActivated();
        }
    }

    private void checkBonusDuration(long now) {
        // Проверяем, не истекло ли время действия бонуса
        if (bonusStartTime > 0 && now - bonusStartTime > bonusDurationMs) {
            bonusStartTime = 0;
        }
    }

    public boolean isBonusActive() {
        return bonusStartTime > 0 && System.currentTimeMillis() - bonusStartTime <= bonusDurationMs;
    }

    public void applyGravity(float gravityX, float gravityY) {
        if (isBonusActive()) {
            for (Bug b : bugs) {
                b.vx += gravityX * 0.5f;
                b.vy += gravityY * 0.5f;
                // Ограничиваем скорость
                float maxSpeed = 10f;
                if (Math.abs(b.vx) > maxSpeed) b.vx = Math.signum(b.vx) * maxSpeed;
                if (Math.abs(b.vy) > maxSpeed) b.vy = Math.signum(b.vy) * maxSpeed;
            }
        }
    }

    private void goldenBugLogic(long now) {
        // Создаем золотого таракана каждые 20 секунд
        if (!goldenBugActive && now - lastGoldenBugMs > goldenBugIntervalSec * 1000L) {
            spawnGoldenBug();
            lastGoldenBugMs = now;
        }
    }

    private void spawnGoldenBug() {
        goldenBugX = goldenBugRadius + random.nextInt(Math.max(1, (int)(widthPx - 2*goldenBugRadius)));
        goldenBugY = goldenBugRadius + random.nextInt(Math.max(1, (int)(heightPx - 2*goldenBugRadius)));
        goldenBugActive = true;
        // Временная отладка - можно удалить позже
        android.util.Log.d("GameView", "Golden bug spawned at: " + goldenBugX + ", " + goldenBugY);
    }

    public void setGoldRate(double rate) {
        this.goldRate = rate;
        android.util.Log.d("GameView", "Gold rate updated: " + rate);
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


