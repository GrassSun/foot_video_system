package editor.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.widget.TableLayout;

import java.util.Locale;

import editor.ijk.demo.R;
import tv.danmaku.ijk.media.example.widget.media.TableLayoutBinder;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaPlayerProxy;

/**
 * Created by sunweijun on 18-3-22.
 */

public class SubViewHolder {
    private TableLayoutBinder mTableLayoutBinder;
    private SparseArray<View> mRowMap = new SparseArray<View>();
    private EditorView mMediaPlayer;
    private long mLoadCost = 0;
    private long mSeekCost = 0;

    public SubViewHolder(Context context, TableLayout tableLayout) {
        mTableLayoutBinder = new TableLayoutBinder(context, tableLayout);
    }

    private void appendSection(int nameId) {
        mTableLayoutBinder.appendSection(nameId);
    }

    private void appendRow(int nameId) {
        View rowView = mTableLayoutBinder.appendRow2(nameId, null);
        mRowMap.put(nameId, rowView);
    }

    private void setRowValue(int id, String value) {
        View rowView = mRowMap.get(id);
        if (rowView == null) {
            rowView = mTableLayoutBinder.appendRow2(id, value);
            mRowMap.put(id, rowView);
        } else {
            mTableLayoutBinder.setValueText(rowView, value);
        }
    }

    public void setMediaPlayer(EditorView mp) {
        mMediaPlayer = mp;
        if (mMediaPlayer != null) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SUB, MSG_DELAY);
        } else {
            mHandler.removeMessages(MSG_UPDATE_SUB);
        }
    }

    private static String formatedDurationMilli(long duration) {
        if (duration >=  1000) {
            return String.format(Locale.US, "%.2f sec", ((float)duration) / 1000);
        } else {
            return String.format(Locale.US, "%d msec", duration);
        }
    }

    private static String formatedSpeed(long bytes,long elapsed_milli) {
        if (elapsed_milli <= 0) {
            return "0 B/s";
        }

        if (bytes <= 0) {
            return "0 B/s";
        }

        float bytes_per_sec = ((float)bytes) * 1000.f /  elapsed_milli;
        if (bytes_per_sec >= 1000000) {
            return String.format(Locale.US, "%.2f MB/s", ((float)bytes_per_sec) / 1000000);
        } else if (bytes_per_sec >= 1000) {
            return String.format(Locale.US, "%.1f KB/s", ((float)bytes_per_sec) / 1000);
        } else {
            return String.format(Locale.US, "%d B/s", (long)bytes_per_sec);
        }
    }

    public void updateLoadCost(long time)  {
        mLoadCost = time;
    }

    public void updateSeekCost(long time)  {
        mSeekCost = time;
    }

    private static String formatedSize(long bytes) {
        if (bytes >= 1000000) {
            return String.format(Locale.US, "%.2f MB", ((float)bytes) / 1000000);
        } else if (bytes >= 1000) {
            return String.format(Locale.US, "%.1f KB", ((float)bytes) / 1000);
        } else {
            return String.format(Locale.US, "%d B", bytes);
        }
    }

    private static final int MSG_UPDATE_SUB = 1;
    private static final int MSG_DELAY = 1;

    private int startPosition = 0;
    private int endPosition = 0;

    public void setSegment(int _startPosition, int _endPosition) {
        startPosition = _startPosition;
        endPosition = _endPosition;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SUB: {
                    EditorView mp = mMediaPlayer;
                    if (mp == null)
                        break;

                    long videoCachedDuration = mp.getCachedDuration();
                    setRowValue(R.string.v_cache, String.format(Locale.US, "%s", formatedDurationMilli(videoCachedDuration)));


                    int current = mp.getMediaCurrentPosition();
                    setRowValue(R.string.current_position, String.valueOf(current));

                    mp.markSub.setText(mp.getSubTitle());

                    if(mp.isPlaying()) {
                        if(startPosition != 0) {
                            mp.seekMediaTo(startPosition);
                            startPosition = 0;
                        } else if(endPosition !=0 && current >= endPosition) {
                            endPosition = 0;
                            mp.getNextSegment();
                        } else {
                            if( current + videoCachedDuration >= endPosition) {
                                mp.prepareNextSegment();
                            }
                            mp.checkSpeed();
                        }
                    }

                    mHandler.removeMessages(MSG_UPDATE_SUB);
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SUB, MSG_DELAY);
                }
            }
        }
    };
}
