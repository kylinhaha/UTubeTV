package com.sickboots.sickvideos.imageutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.sickboots.iconicdroid.IconicFontDrawable;
import com.sickboots.iconicdroid.icon.FontAwesomeIcon;
import com.sickboots.iconicdroid.icon.Icon;
import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Utils;

public class ToolbarIcons {

  public static enum IconID {NONE, SOUND, STEP_FORWARD, STEP_BACK, FULLSCREEN, LIST, CLOSE, OVERFLOW, VIDEO_PLAY, ABOUT, UPLOADS, PLAYLISTS, YOUTUBE}

  public static Drawable icon(Context context, IconID iconID, int iconColor, int sizeInDP) {
    StateListDrawable result = null;

    Icon icon = null;

    switch (iconID) {
      case NONE:
        break;
      case SOUND:
        icon = FontAwesomeIcon.VOLUME_UP;
        break;
      case STEP_BACK:
        icon = FontAwesomeIcon.STEP_BACKWARD;
        break;
      case STEP_FORWARD:
        icon = FontAwesomeIcon.STEP_FORWARD;
        break;
      case CLOSE:
        icon = FontAwesomeIcon.TIMES_CIRCLE;
        break;
      case FULLSCREEN:
        icon = FontAwesomeIcon.ARROWS_ALT;
        break;
      case VIDEO_PLAY:
        icon = FontAwesomeIcon.YOUTUBE_PLAY;
        break;
      case LIST:
        icon = FontAwesomeIcon.LIST_UL;
        break;
      case OVERFLOW:
        icon = FontAwesomeIcon.ELLIPSIS_V;
        break;
      case ABOUT:
        icon = FontAwesomeIcon.INFO_CIRCLE;
        break;
      case PLAYLISTS:
        icon = FontAwesomeIcon.FILM;
        break;
      case YOUTUBE:
        icon = FontAwesomeIcon.YOUTUBE_SQUARE;
        break;
      case UPLOADS:
        icon = FontAwesomeIcon.UPLOAD;
        break;
      default:
        break;
    }

    if (icon != null) {
      Drawable pressed = null;
      Drawable normal = null;

      if (icon != null) {
        IconicFontDrawable fpressed = new IconicFontDrawable(context);
        fpressed.setIcon(icon);
        fpressed.setIconColor(context.getResources().getColor(R.color.holo_blue));
        fpressed.setContour(Color.GRAY, 1);
        fpressed.setIconPadding(8);

        IconicFontDrawable fnormal = new IconicFontDrawable(context);
        fnormal.setIcon(icon);
        fnormal.setIconColor(iconColor);
        fnormal.setContour(Color.GRAY, 1);
        fnormal.setIconPadding(8);

        int size = (int) Utils.dpToPx(sizeInDP, context);
        fnormal.setIntrinsicWidth(size);
        fnormal.setIntrinsicHeight(size);
        fpressed.setIntrinsicWidth(size);
        fpressed.setIntrinsicHeight(size);

        normal = fnormal;
        pressed = fpressed;
      }

      result = new StateListDrawable();
      result.addState(new int[]{android.R.attr.state_pressed}, pressed);
      result.addState(new int[]{}, normal);
    }

    return result;
  }

  // this doesn't have the states like above. used to convert an icon to a simple bitmap, assuming things like animations will be faster with a bitmap over a text based drawable
  public static BitmapDrawable iconBitmap(Context context, IconID iconID, int iconColor, int sizeInDP) {
    BitmapDrawable result = null;

    Drawable iconDrawable = icon(context, iconID, iconColor, sizeInDP);

    if (iconDrawable != null) {
      int size = (int) Utils.dpToPx(sizeInDP, context);

      Bitmap map = Utils.drawableToBitmap(iconDrawable, size);

      return new BitmapDrawable(context.getResources(), map);
    }

    return null;
  }

}