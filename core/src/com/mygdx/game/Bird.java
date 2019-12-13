package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;

public class Bird {
    public Rectangle hitbox;
    public float speed;

    public Bird(float w, float h){
        hitbox = new Rectangle();
        hitbox.width = w;
        hitbox.height = h;
        hitbox.x = 900;
        hitbox.y = 500;
        speed = 0;
    }

    public void move(){
        if(!(this.hitbox.y <= 0 && speed < 0))
            this.hitbox.y += speed;
        else this.hitbox.y = 0;
    }

    public void accelerate(float a){
        this.speed += a;
        if(this.speed <= -15) this.speed = -27;
    }
}
