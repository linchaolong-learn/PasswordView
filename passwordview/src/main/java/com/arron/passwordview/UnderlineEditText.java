package com.arron.passwordview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 支持下划线、矩形框、光标的 EditText
 */
public class UnderlineEditText extends AppCompatEditText {

    private static final int DEFAULT_COUNT = 6;
    private Underline underlines[];
    private Paint underlinePaint;
    private Paint textPaint;
    private Paint backgroundPaint;
    private float underlineReduction;
    private float underlineStrokeWidth;
    private float underlineWidth;
    private float textSize;
    private float textMarginBottom;
    private float viewHeight;
    private int height;
    private int width;
    private int characterCount;
    private int underlineColor;
    private int textColor;
    private int backgroundColor;
    private float density;
    private int textLength;
    private Paint rectPaint;
    private int rectColor;
    private float rectRound;
    private Paint cursorPaint;
    private boolean isCursorShowing;//光标是否正在显示
    private long cursorFlashTime;//光标闪动间隔时间
    private Timer timer;
    private TimerTask timerTask;
    private boolean isCursorEnable;
    private boolean isUnderlineEnable;
    private boolean isRectEnable;


    public UnderlineEditText(Context context) {
        super(context);
        init(null);
    }

    public UnderlineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public UnderlineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        density = getContext().getResources().getDisplayMetrics().density;
        initDefaultAttributes();
        initCustomAttributes(attrs);
        initDataStructures();
        initPaint();
        setSingleLine();
        setCursorVisible(false);
        setLongClickable(false);
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
    }

    private void initDataStructures() {
        underlines = new Underline[characterCount];
    }

    private void initPaint() {
        underlinePaint = new Paint();
        underlinePaint.setColor(underlineColor);
        underlinePaint.setStrokeWidth(underlineStrokeWidth);
        underlinePaint.setStyle(Paint.Style.STROKE);
        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setColor(rectColor);
        cursorPaint = new Paint();
        cursorPaint.setColor(Color.parseColor("#ffffff"));
        cursorPaint.setStrokeWidth(1 * density);
        cursorPaint.setStyle(Paint.Style.FILL);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                isCursorShowing = !isCursorShowing;
                postInvalidate();
            }
        };
        timer = new Timer();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //cursorFlashTime为光标闪动的间隔时间
        timer.scheduleAtFixedRate(timerTask, 0, cursorFlashTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (selStart == selEnd) {
            setSelection(getText().length());
        }
    }

    /**
     * 设置字符个数
     */
    public void setCharacterCount(int count) {
        characterCount = count;
        initDataStructures();
    }

    public void setCursorEnable(boolean cursorEnable) {
        isCursorEnable = cursorEnable;
    }

    public void setUnderlineEnable(boolean underlineEnable) {
        isUnderlineEnable = underlineEnable;
    }

    public void setRectEnable(boolean rectEnable) {
        isRectEnable = rectEnable;
    }

    private void initCustomAttributes(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.UnderlineEditText);
        try {
            underlineColor = attributes.getColor(R.styleable.UnderlineEditText_underline_color, underlineColor);
            characterCount = attributes.getInt(R.styleable.UnderlineEditText_character_count, characterCount);
            textColor = attributes.getInt(R.styleable.UnderlineEditText_text_color, textColor);
            textSize = attributes.getDimension(R.styleable.UnderlineEditText_text_size, textSize);
            backgroundColor = attributes.getColor(R.styleable.UnderlineEditText_background_color, backgroundColor);
        } finally {
            attributes.recycle();
        }
    }

    private void initDefaultAttributes() {
        underlineStrokeWidth = 2 * density;
        underlineWidth = 35 * density;
        underlineReduction = 5 * density;
        textSize = 15 * density;
        textMarginBottom = 10 * density;
        underlineColor = Color.parseColor("#cccccc");
        textColor = Color.parseColor("#000000");
        viewHeight = 40 * density;
        characterCount = DEFAULT_COUNT;
        backgroundColor = Color.parseColor("#ffffff");
        rectColor = Color.parseColor("#88ffffff");
        rectRound = 4 * density;
        cursorFlashTime = 500;
        isCursorEnable = true;
        isUnderlineEnable = false;
        isRectEnable = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged((int) ((underlineWidth + underlineReduction) * characterCount - underlineReduction), (int) viewHeight, oldw, oldh);
        height = h;
        width = w;
        initUnderline();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int) ((underlineWidth + underlineReduction) * characterCount - underlineReduction), (int) viewHeight);
    }

    private void initUnderline() {
        for (int i = 0; i < characterCount; i++) {
            underlines[i] = createPath(i, underlineWidth);
        }
    }

    private Underline createPath(int position, float sectionWidth) {
        float fromX = (sectionWidth + underlineReduction) * (float) position;
        return new Underline(fromX, height, fromX + sectionWidth, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < underlines.length; i++) {
            Underline line = underlines[i];
            float fromX = line.fromX;
            float fromY = line.fromY;
            float toX = line.toX;
            float toY = line.toY;
            drawRect(fromX, toX, height, canvas);
            drawUnderline(fromX, fromY, toX, toY, canvas);
        }
        for (int i = 0; i < textLength; i++) {
            drawCharacter(underlines[i].fromX, underlines[i].toX, getText().toString().charAt(i), canvas);
        }

        //绘制光标
        drawCursor(canvas);
    }

    /**
     * 绘制矩形框
     */
    private void drawRect(float fromX, float toX, float height, Canvas canvas) {
        if (isRectEnable) {
            RectF rect = new RectF(fromX, getPaddingTop(), toX, getPaddingTop() + height);
            canvas.drawRoundRect(rect, rectRound, rectRound, rectPaint);
        }
    }

    /**
     * 绘制光标
     */
    private void drawCursor(Canvas canvas) {
        //光标未显示 && 开启光标 && 输入位数未满 && 获得焦点
        if (!isCursorShowing && isCursorEnable && textLength < characterCount && hasFocus()) {
            Underline section = underlines[textLength];
            float cursorMarginBottom = 3 * density;
            float cursorMarginLeftRight = 5 * density;
            canvas.drawLine(section.fromX + cursorMarginLeftRight, section.fromY - cursorMarginBottom,
                    section.toX - cursorMarginLeftRight, section.toY - cursorMarginBottom, cursorPaint);
        }
    }

    /**
     * 绘制下划线
     */
    private void drawUnderline(float fromX, float fromY, float toX, float toY, Canvas canvas) {
        if (isUnderlineEnable) {
            Paint paint = underlinePaint;
            canvas.drawLine(fromX, fromY, toX, toY, paint);
        }
    }

    /**
     * 绘制字符
     */
    private void drawCharacter(float fromX, float toX, Character character, Canvas canvas) {
        float actualWidth = toX - fromX;
        float centerWidth = actualWidth / 2;
        float centerX = fromX + centerWidth;
        canvas.drawText(String.valueOf(character), centerX, height - textMarginBottom, textPaint);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.textLength = text.toString().length();
        Editable text1 = getText();
        if (text1.length() > characterCount) {
            setText(text1.subSequence(0, characterCount));
            this.textLength = characterCount;
        }
    }

    static class Underline {

        float fromX;
        float fromY;
        float toX;
        float toY;

        public Underline(float fromX, float fromY, float toX, float toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }
}
