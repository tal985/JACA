package com.tuan.jaca;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;

public class DrawSkillGrid extends View
{
    protected boolean[][] skillGrid;
    protected String QWERString = "";
    protected Paint paint = new Paint();
    protected Context mc;

    public DrawSkillGrid(Context c)
    {
        super(c);
        mc = c;
    }

    public DrawSkillGrid(Context c, AttributeSet a)
    {
        super(c, a);
        mc = c;
    }

    public DrawSkillGrid(Context c, AttributeSet a, int ds)
    {
        super(c, a, ds);
        mc = c;
    }

    public void insertHashstring(String hashstring)
    {
        QWERString = cleanString(hashstring);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        float gridCellHeight = 80f, gridCellWidth = getWidth()/19f, x, y;
        int i, j;
        canvas.drawColor(Color.WHITE);
        paint.setStrokeWidth(4);

        //Draw skill squares first if there's a string
        if(!QWERString.isEmpty())
        {
            paint.setColor(ContextCompat.getColor(mc, R.color.colorPrimary));
            skillGrid = QWERtoArray(QWERString);

            for (i = 0; i < 18; i++) {
                for (j = 0; j < 4; j++) {
                    if (skillGrid[i][j])
                        canvas.drawRect(gridCellWidth * (i + 1), gridCellHeight * (j + 1), gridCellWidth * (i + 2), gridCellHeight * (j + 2), paint);
                }
            }
        }

        //Draw grid
        paint.setColor(ContextCompat.getColor(mc, R.color.graycgg));
        paint.setTextSize(40f);

        canvas.drawText("Q", 10, gridCellHeight * 2 - 20, paint);
        canvas.drawText("W", 10, gridCellHeight * 3 - 20, paint);
        canvas.drawText("E", 10, gridCellHeight * 4 - 20, paint);
        canvas.drawText("R", 10, gridCellHeight * 5 - 20, paint);

        for(i = 0; i <= 5; i++)
        {
            y = i * gridCellHeight;
            canvas.drawLine(0, y, getWidth(), y, paint);
        }

        for(j = 0; j < 19; j++)
        {
            if(j > 0 && j < 10)
                canvas.drawText(String.valueOf(j), gridCellWidth * j + 15, gridCellHeight - 20, paint);
            else if(j >= 10)
            {
                canvas.drawText(String.valueOf(j), gridCellWidth * j + 5, gridCellHeight - 20, paint);
            }
            x = j * gridCellWidth;
            canvas.drawLine(x, 0, x, gridCellHeight * 5, paint);
        }
    }

     //Clean champion.gg's hash string to QWER string
    public String cleanString(String hashstring)
    {
        StringBuilder temp = new StringBuilder();
        for(char i: hashstring.toCharArray())
        {
            if(i == 'Q' || i == 'W' || i == 'E' || i == 'R')
                temp.append(i);
        }
        return temp.toString();
    }

    //Convert QWER string to 2D boolean array
    public boolean[][] QWERtoArray(String QWER)
    {
        boolean[][] skills = new boolean[18][4];

		for(int i = 0; i < 18; i++)
		{
			if(QWER.charAt(i) == 'Q')
				skills [i][0] = true;
			else if(QWER.charAt(i) == 'W')
				skills [i][1] = true;
			else if(QWER.charAt(i) == 'E')
				skills [i][2] = true;
			else
				skills [i][3] = true;
		}

        return skills;
    }
}
