package com.distantfuture.videos.channellookup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.distantfuture.videos.R;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.imageutils.ToolbarIcons;
import com.distantfuture.videos.misc.Debug;

import java.util.List;

public class ChannelLookupAdapter extends ArrayAdapter<YouTubeData> {

  private final Context mContext;
  private final float mAspectRatio = 9f / 16f;
  private BitmapDrawable mPlusButtonBitmap;
  View.OnClickListener mButtonListener;

  public ChannelLookupAdapter(Context context) {
    super(context, 0);
    this.mContext = context;

    mPlusButtonBitmap = ToolbarIcons.iconBitmap(context, ToolbarIcons.IconID.CLOSE, Color.RED, 36);

    mButtonListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Debug.log("fuck you");
      }
    };
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    ViewHolder holder;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    YouTubeData mm = getItem(position);

    if (convertView == null) {
      convertView = inflater.inflate(R.layout.channel_lookup_list_item, null);
      holder = new ViewHolder();
      holder.imgView = (ImageView) convertView.findViewById(R.id.imageView1);
      holder.addButton = (ImageView) convertView.findViewById(R.id.add_remove_button);
      holder.titleView = (TextView) convertView.findViewById(R.id.textView1);
      holder.descrView = (TextView) convertView.findViewById(R.id.textView2);
      convertView.setTag(holder);

      holder.addButton.setOnClickListener(mButtonListener);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    AQuery aq = new AQuery(convertView);
    aq.id(holder.imgView)
        .width(110)
        .image(mm.mThumbnail, true, true, 0, 0, null, 0, mAspectRatio);
    aq.id(holder.titleView).text(mm.mTitle);
    aq.id(holder.descrView).text(mm.mDescription);

    holder.addButton.setImageDrawable(mPlusButtonBitmap);

    return convertView;
  }

  private class ViewHolder {
    TextView titleView;
    TextView descrView;
    ImageView imgView;
    ImageView addButton;
  }

  public void setData(List<YouTubeData> data) {
    clear();
    if (data != null) {
      for (YouTubeData item : data) {
        add(item);
      }
    }

  }
}
