/*
 * Copyright 2019 Laurent PAGE, Apache Licence 2.0
 */
package live.page.android.utils;

import android.content.Context;

import java.io.StringWriter;
import java.util.Date;

import live.page.android.R;

public class Since {

    private static final double DAYS_PER_YEAR = 365.24225D;
    private static final double M_PER_SECOND = 1000D;
    private static final double M_PER_MINUTE = 60D * M_PER_SECOND;
    private static final double M_PER_HOUR = 60D * M_PER_MINUTE;
    private static final double M_PER_DAY = 24D * M_PER_HOUR;
    private static final double M_PER_WEEKS = 7D * M_PER_DAY;
    private static final double M_PER_MONTH = Math.floor((DAYS_PER_YEAR / 12D) * M_PER_DAY);
    private static final double M_PER_YEAR = Math.floor(DAYS_PER_YEAR * M_PER_DAY);

    public static String format(Context ctx, Date date, int level) {

        long durationInit = System.currentTimeMillis() - date.getTime();
        if (durationInit < 60000D && durationInit > -60000D) {
            return ctx.getString(R.string.just_now);
        }
        boolean past = durationInit < 0;
        double durationMillis = Math.abs(durationInit);

        double yearsD = Math.floor(durationMillis / M_PER_YEAR);
        durationMillis = durationMillis - (yearsD * M_PER_YEAR);

        double monthsD = Math.floor(durationMillis / M_PER_MONTH);
        durationMillis = durationMillis - (monthsD * M_PER_MONTH);

        double weeksD = Math.floor(durationMillis / M_PER_WEEKS);
        durationMillis = durationMillis - (weeksD * M_PER_WEEKS);

        double daysD = Math.floor(durationMillis / M_PER_DAY);
        durationMillis = durationMillis - (daysD * M_PER_DAY);

        double hoursD = Math.floor(durationMillis / M_PER_HOUR);
        durationMillis = durationMillis - (hoursD * M_PER_HOUR);

        double minutesD = Math.floor(durationMillis / M_PER_MINUTE);
        durationMillis = durationMillis - (minutesD * M_PER_MINUTE);

        int years = (int) yearsD;
        int months = (int) monthsD;
        int weeks = (int) weeksD;
        int days = (int) daysD;
        int hours = (int) hoursD;
        int minutes = (int) minutesD;

        // years + "/" + months + "/" + weeks + "/" + days + "/" + hours + "/" +
        // minutes + "/" + seconds;

        StringWriter since = new StringWriter();

        String space_num = " ";
        String space = "";
        while (level > 0) {
            boolean effect = false;
            if (years > 0) {
                since.append(space + years + space_num + (years > 1 ? ctx.getString(R.string.years) : ctx.getString(R.string.year)));
                years = 0;
                effect = true;
            } else if (months > 0) {
                since.append(space + months + space_num + (months > 1 ? ctx.getString(R.string.months) : ctx.getString(R.string.month)));
                months = 0;
                effect = true;
            } else if (weeks > 0) {
                since.append(space + weeks + space_num + (weeks > 1 ? ctx.getString(R.string.weeks) : ctx.getString(R.string.week)));
                weeks = 0;
                effect = true;
            } else if (days > 0) {
                since.append(space + days + space_num + (days > 1 ? ctx.getString(R.string.days) : ctx.getString(R.string.day)));
                days = 0;
                effect = true;
            } else if (hours > 0) {
                since.append(space + hours + space_num + (hours > 1 ? ctx.getString(R.string.hours) : ctx.getString(R.string.hour)));
                hours = 0;
                effect = true;
            } else if (minutes > 0) {
                since.append(space + minutes + space_num + (minutes > 1 ? ctx.getString(R.string.minutes) : ctx.getString(R.string.minute)));
                minutes = 0;
                effect = true;
            }
            level--;
            if (effect) {
                if (level == 1) {
                    space = " " + ctx.getString(R.string.and) + " ";
                } else {
                    space = ", ";
                }
            }
        }
        if (!past) {
            return ctx.getString(R.string.since_ago).replace("%1", since.toString());
        } else {
            return ctx.getString(R.string.since_in).replace("%1", since.toString());
        }
    }
}
