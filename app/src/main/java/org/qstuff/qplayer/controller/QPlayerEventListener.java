package org.qstuff.qplayer.controller;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 1/12/17
 * for Karlmax Berlin GmbH & Co. KG
 * <p>
 * Copyright (C) 2013, 2014, 2015 Karlmax Berlin GmbH & Co. KG,
 * All rights reserved.
 */

public interface QPlayerEventListener {

    void onError();
    void onCompletion();
    void onPitchControllChanged(int progress);
    void onPrepared(boolean prepared);
}
