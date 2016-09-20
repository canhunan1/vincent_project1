package com.wchen113.android.auggrisignin;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Wenhao on 9/19/2016.
 */
public class Tag {
    int tagId;
    LatLng ll;
    Tag(){

    };
    Tag(int tagId, LatLng ll){
        this.tagId = tagId;
        this.ll = ll;
    }
}
