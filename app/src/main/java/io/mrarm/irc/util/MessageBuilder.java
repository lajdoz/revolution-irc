package io.mrarm.irc.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;

import java.util.Arrays;

import io.mrarm.irc.R;

public class MessageBuilder {

    public static final String DEFAULT_EVENT_MESSAGE_FORMAT = "{time} {sender}{text}";
    public static final String DEFAULT_NOTICE_MESSAGE_FORMAT = "{time} {sender}{text}";

    private final Context mContext;
    private final boolean mShowHostname;
    private CharSequence mEventMessageFormat = DEFAULT_EVENT_MESSAGE_FORMAT;
    private CharSequence mNoticeMessageFormat = DEFAULT_NOTICE_MESSAGE_FORMAT;

    public MessageBuilder(Context context) {
        this(context, true);
    }

    public MessageBuilder(Context context, boolean showHostname) {
        mContext = context;
        mShowHostname = showHostname;
    }

    public void setNoticeMessageFormat(CharSequence format) {
        mNoticeMessageFormat = format;
    }

    public CharSequence getEventMessageFormat() {
        return mEventMessageFormat;
    }

    public void setEventMessageFormat(CharSequence format) {
        mEventMessageFormat = format;
    }

    public boolean getEventMessageShowHostname() {
        return mShowHostname;
    }

    public void setEventMessageShowHostname(boolean enabled) {
        // no-op for compatibility, the builder is created with a fixed value
    }

    public CharSequence createTimestamp(Date date, boolean addDefaultColorSpan) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(DateFormat.getTimeFormat(mContext).format(date));
        if (addDefaultColorSpan) {
            builder.setSpan(new ForegroundColorSpan(IRCColorUtils.getTimestampTextColor(mContext)),
                    0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public CharSequence buildDisconnectWarning(Date date) {
        ColoredTextBuilder builder = new ColoredTextBuilder();
        builder.append(createTimestamp(date, true));
        builder.append(" Disconnected", new ForegroundColorSpan(mContext.getResources().getColor(R.color.messageDisconnected)));
        return builder.getSpannable();
    }

    private CharSequence buildColoredMessage(CharSequence msg, int color, boolean priority) {
        SpannableString spannable = new SpannableString(msg);
        spannable.setSpan(new ForegroundColorSpan(color), 0, msg.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE | (priority ? Spanned.SPAN_PRIORITY : 0));
        return spannable;
    }

    private CharSequence buildColoredNick(String nick) {
        return buildColoredMessage(nick, IRCColorUtils.getNickColor(mContext, nick), false);
    }

    private CharSequence buildColoredNickWithHostname(MessageSenderInfo sender) {
        if (sender == null || sender.getHostname() == null || sender.getHostname().length() == 0 || !mShowHostname)
            return buildColoredNick(sender == null ? "" : sender.getNick());
        String nickWithHost = sender.getNick() + "!" + sender.getHostname();
        return buildColoredMessage(nickWithHost, IRCColorUtils.getNickColor(mContext, sender.getNick()), false);
    }

    private CharSequence buildEventMessage(String arrow, int color, MessageSenderInfo sender) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString arrowText = new SpannableString(arrow);
        arrowText.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(color)), 0, arrow.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        arrowText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, arrow.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(arrowText);
        builder.append(" ");
        builder.append(buildColoredNickWithHostname(sender));
        return builder;
    }

    private CharSequence buildModeMessage(String senderNick,
                                          List<ChannelModeMessageInfo.Entry> list) {
        SpannableStringBuilder msg = new SpannableStringBuilder();
        for (ChannelModeMessageInfo.Entry entry : list) {
            if (msg.length() > 0)
                msg.append(mContext.getString(R.string.text_comma));

            String sign = entry.isRemoved() ? "-" : "+";
            SpannableString mode = new SpannableString(sign + entry.getMode());
            mode.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.messageStatusText)),
                    0, mode.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mode.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    0, mode.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            msg.append(mode);

            String param = entry.getParam();
            if (param != null && param.length() > 0) {
                msg.append(" ");
                msg.append(buildColoredMessage(param,
                        mContext.getResources().getColor(R.color.messageStatusText), false));
            }
        }

        SpannableStringBuilder result = new SpannableStringBuilder();
        result.append(buildColoredMessage(senderNick == null ? "" : senderNick,
                mContext.getResources().getColor(R.color.messageStatusText), false));
        result.append(" ");
        result.append(SpannableStringHelper.getText(mContext, R.string.message_mode_set, msg));
        return result;
    }

    // remainder omitted for brevity
}
