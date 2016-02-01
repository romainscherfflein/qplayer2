package org.qstuff.qplayer.data;

import java.io.Serializable;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class Track  implements Serializable {

    private String uri;

    private String name;
    
    public Track(String name, String uri) {
        this.uri = uri;
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    public String getName() {
        return name;
    }
}
