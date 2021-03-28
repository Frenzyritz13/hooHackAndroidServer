package com.hoohacks.widgetserverapp.data;

import java.util.Map;

public class DeviceData {

    Acc Acc;
    float Flex;
    float RE;

    public Acc getAcc() {
        return Acc;
    }

    public void setAcc(Acc acc) {
        Acc = acc;
    }

    public float getFlex() {
        return Flex;
    }

    public void setFlex(float flex) {
        Flex = flex;
    }

    public float getRE() {
        return RE;
    }

    public void setRE(float RE) {
        this.RE = RE;
    }

    public static class Acc{
        float x,y,z;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getZ() {
            return z;
        }

        public void setZ(float z) {
            this.z = z;
        }
    }
}
