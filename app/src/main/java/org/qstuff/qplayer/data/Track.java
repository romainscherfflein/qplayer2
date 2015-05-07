package org.qstuff.qplayer.data;

import java.io.Serializable;

/**
 * Created by Claus Chierici (chierici@karlmax-berlin.com) on 5/7/15
 * for Karlmax Berlin GmbH & Co. KG
 * <p/>
 * Copyright (C) 2014 Karlmax Berlin GmbH & Co. KG, All rights reserved.
 */
public class Track  implements Serializable {

    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
