package com.afar.osaio.bean;

import java.lang.reflect.Field;

public enum AlarmLevel {
    Close(0),
    Low(1),
    Medium(2),
    High(3);

    private final int intVal;

    public int getIntVal() {
        return this.intVal;
    }

    private AlarmLevel(int i) {
        this.intVal = i;
    }

    public static AlarmLevel getAlarmLevel(int level) {
        Field[] fields = AlarmLevel.class.getFields();
        Field[] var2 = fields;
        int var3 = fields.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Field field = var2[var4];
            if (field != null && field.isEnumConstant() && field.getDeclaringClass() == AlarmLevel.class) {
                try {
                    AlarmLevel al = (AlarmLevel)field.get((Object)null);
                    if (al.getIntVal() == level) {
                        return al;
                    }
                } catch (IllegalAccessException var7) {
                    var7.printStackTrace();
                }
            }
        }

        return getAlarmLevel(0);
    }
}
