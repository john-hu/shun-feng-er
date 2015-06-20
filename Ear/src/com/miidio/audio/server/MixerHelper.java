package com.miidio.audio.server;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by John Hu on 2015/6/20.
 *
 * The MixerHelper lists all mixers and dumps to console for help user
 */
public class MixerHelper {
    private static Logger logger = Logger.getLogger(MixerHelper.class.getName());

    public static Mixer filterMixer(String mixerName) {
        logger.log(Level.INFO, "query mixer starting with " + mixerName);
        return AudioSystem.getMixer(filterMixerInfo(mixerName)[0]);
    }

    public static void dumpMixers() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        logger.log(Level.INFO, "mixer count: " + mixers.length);

        for(Mixer.Info info : mixers) {
            logger.log(Level.INFO, info.toString());}
    }

    public static Mixer.Info[] filterMixerInfo(String mixerName) {
        ArrayList<Mixer.Info> ret = new ArrayList<Mixer.Info>();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        if (null == mixers || 0 == mixers.length) {
            throw new RuntimeException("no mixer found??");
        }
        for (Mixer.Info info : mixers) {
            if (info.getName().startsWith(mixerName)) {
                ret.add(info);
            }
        }
        if (0 == ret.size()) {
            ret.add(mixers[0]);
        }
        return ret.toArray(new Mixer.Info[ret.size()]);
    }
}
