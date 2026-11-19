package com.mrmr.animmod;

public enum MoveState {
    IDLE("idle"), WALK("walk"), RUN("run"), SNEAK("sneak"),
    CLIMB("climb"), SWIM("swim"), FALL("fall"), JUMP("jump"), GLIDE("glide");

    public final String animName;
    MoveState(String animName) { this.animName = animName; }
}
