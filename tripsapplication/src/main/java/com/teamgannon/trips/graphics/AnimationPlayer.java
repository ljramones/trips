package com.teamgannon.trips.graphics;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;

public class AnimationPlayer extends AnimationTimer {

    private final Group starGroup;
    private final Group labelGroup;

    public AnimationPlayer(Group starGroup, Group labelGroup) {
        this.starGroup = starGroup;
        this.labelGroup = labelGroup;
    }


    @Override
    public void handle(long now) {

    }

}
