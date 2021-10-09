/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.entities;

import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

public interface GuildThread extends GuildMessageChannel, IMemberContainer
{
    //Stuff needed from GuildChannel
    // - Interface: Mentionable
    // - getJDA
    // - getName
    // - getGuild
    // - getChannelType
    // - getManager //TODO will need to be ThreadManager, not ChannelManager
    // - getMembers //TODO might not need this as we'll have getThreadMembers()

    //TODO fields that need to be researched:
    // - rate_limit_per_user
    // - last_pin_timestamp (do we even use this for Text/News channels?)

    //TODO evaluate if Threads support webhooks in the same way that BaseGuildMessageChannel does (Text/News)
    // - Docs makes me think it does: https://discord.com/developers/docs/topics/threads#webhooks

    default boolean isPublic()
    {
        ChannelType type = getType();
        return type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_NEWS_THREAD;
    }

    //TODO docs | Max returned amount is capped at 50 regardless of actual count
    int getMessageCount();

    //TODO docs | Max returned amount is capped at 50 regardless of actual count
    int getMemberCount();

    default boolean isNSFW()
    {
        return getParentChannel().isNSFW();
    }

    //TODO | This name is bad. Looking for alternatives.
    default boolean isSubscribedToThread()
    {
        return getSelfThreadMember() != null;
    }

    boolean isLocked();

    @Nonnull
    BaseGuildMessageChannel getParentChannel();

    @Nullable
    default GuildThreadMember getSelfThreadMember()
    {
        return getThreadMember(getJDA().getSelfUser());
    }

    //Only have access to this with GUILD_MEMBERS
    @Nonnull
    List<GuildThreadMember> getThreadMembers();

    @Nullable
    default GuildThreadMember getThreadMember(Member member)
    {
        return getThreadMemberById(member.getId());
    }

    @Nullable
    default GuildThreadMember getThreadMember(User user)
    {
        return getThreadMemberById(user.getId());
    }

    @Nullable
    default GuildThreadMember getThreadMemberById(String id)
    {
        return getThreadMemberById(MiscUtil.parseSnowflake(id));
    }

    @Nullable
    GuildThreadMember getThreadMemberById(long id);

    long getOwnerIdLong();

    @Nullable
    default String getOwnerId()
    {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    @Nullable
    default Member getOwner()
    {
        return getGuild().getMemberById(getOwnerIdLong());
    }

    @Nullable
    default GuildThreadMember getOwnerThreadMember()
    {
        return getThreadMemberById(getOwnerIdLong());
    }

    boolean isArchived();

    //TODO This name sucks.
    OffsetDateTime getTimeArchive();

    @Nonnull
    AutoArchiveDuration getAutoArchiveDuration();

    int getSlowmode();

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        String out;

        if (alt)
            out = "#" + (upper ? getName().toUpperCase(formatter.locale()) : getName());
        else
            out = getAsMention();

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }

    //////////////////////////

    enum AutoArchiveDuration {
        //TODO: I dislike this naming scheme. Need to come up with something better.
        TIME_1_HOUR(60),
        TIME_24_HOURS(1440),
        TIME_3_DAYS(4320),
        TIME_1_WEEK(10080);

        private final int minutes;

        AutoArchiveDuration(int minutes)
        {
            this.minutes = minutes;
        }

        public int getMinutes()
        {
            return minutes;
        }

        @Nonnull
        public static AutoArchiveDuration fromKey(int minutes)
        {
            for (AutoArchiveDuration duration : values())
            {
                if (duration.getMinutes() == minutes)
                    return duration;
            }
            throw new IllegalArgumentException("Provided key was not recognized. Minutes: " + minutes);
        }
    }
}
