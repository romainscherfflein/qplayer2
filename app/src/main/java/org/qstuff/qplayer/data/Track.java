package org.qstuff.qplayer.data;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Claus Chierici (github@antamauna.net) on 2/19/15
 *
 * Copyright (C) 2015 Claus Chierici, All rights reserved.
 */
public class Track  implements Serializable {

    private String uri;

    private String name;
    
    public Track() {
        uri = "";
        name = "";
    }
    
    public Track(File file) {
        this.uri = file.getAbsolutePath();
        this.name = file.getName();
    }
    
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
