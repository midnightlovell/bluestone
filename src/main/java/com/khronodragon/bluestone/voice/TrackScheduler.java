package com.khronodragon.bluestone.voice;

import com.khronodragon.bluestone.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.*;

public class TrackScheduler extends AudioEventAdapter {
    private boolean repeating = false;
    public final AudioPlayer player;
    public final Queue<AudioTrack> queue = new LinkedList<>();
    public final Map<AudioTrack, MessageChannel> channelMap = new HashMap<>();
    AudioTrack lastTrack;
    public AudioTrack current;
    public AudioState state;

    TrackScheduler(AudioPlayer player, AudioState state) {
        this.player = player;
        this.state = state;
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true))
            queue.offer(track);
    }

    public void queue(AudioTrack track, MessageChannel textChannel) {
        channelMap.put(track, textChannel);
        queue(track);
    }

    public void nextTrack() {
        if (queue.size() > 0) {
            player.startTrack(queue.poll(), false);
        } else {
            player.destroy();
            state.guild.getAudioManager().closeAudioConnection();
            state.guild.getAudioManager().setSendingHandler(new DummySendHandler());
            state.parent.audioStates.remove(state.guild.getIdLong());
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        current = track;
        if (channelMap.containsKey(track)) {
            AudioTrackInfo info = track.getInfo();
            channelMap.get(track).sendMessage(":arrow_forward: **" + mentionClean(info.title) + "**, length **" + Bot.formatDuration(info.length / 1000) + "**").queue();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        lastTrack = track;
        current = null;
        try {
            if (endReason.mayStartNext) {
                if (repeating) {
                    AudioTrack clone = track.makeClone();
                    if (channelMap.containsKey(track)) {
                        channelMap.put(clone, channelMap.get(track));
                    }
                    player.startTrack(clone, false);
                } else {
                    nextTrack();
                }
            }
        } finally {
            if (channelMap.containsKey(track)) {
                channelMap.remove(track);
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if (channelMap.containsKey(track)) {
            channelMap.get(track).sendMessage(":bangbang: Error in audio player! " + exception.getMessage());
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        if (channelMap.containsKey(track)) {
            channelMap.get(track).sendMessage(":warning: Song appears to be frozen, skipping.").queue();
        }
        track.stop();
        nextTrack();
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void shuffleQueue() {
        Collections.shuffle((List<?>) queue);
    }

    private String mentionClean(String in) {
        return in.replace("@everyone", "@\u200beveryone").replace("@here", "@\u200bhere");
    }
}
