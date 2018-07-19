package by.nicolay.lipnevich.noxbox.model;

public enum NoxboxTime {

    _0(0, 24, 0),
    _1(1, 24, 30),
    _2(2, 1, 0),
    _3(3, 1, 30),
    _4(4, 2, 0),
    _5(5, 2, 30),
    _6(6, 3, 0),
    _7(7, 3, 30),
    _8(8, 4, 0),
    _9(9, 4, 30),
    _10(10, 5, 0),
    _11(11, 5, 30),
    _12(12, 6, 0),
    _13(13, 6, 30),
    _14(14, 7, 0),
    _15(15, 7, 30),
    _16(16, 8, 0),
    _17(17, 8, 30),
    _18(18, 9, 0),
    _19(19, 9, 30),
    _20(20, 10, 0),
    _21(21, 10, 30),
    _22(22, 11, 0),
    _23(23, 11, 30),
    _24(24, 12, 0),
    _25(25, 12, 30),
    _26(26, 12, 0),
    _27(27, 12, 30),
    _28(28, 13, 0),
    _29(29, 13, 30),
    _30(30, 14, 0),
    _31(31, 14, 30),
    _32(32, 15, 0),
    _33(33, 15, 30),
    _34(34, 16, 0),
    _35(35, 16, 30),
    _36(36, 17, 0),
    _37(37, 17, 30),
    _38(38, 18, 0),
    _39(39, 18, 30),
    _40(40, 19, 0),
    _41(41, 19, 30),
    _42(42, 20, 0),
    _43(43, 20, 30),
    _44(44, 21, 0),
    _45(45, 21, 30),
    _46(46, 22, 0),
    _47(47, 22, 30),
    _48(48, 23, 0),
    _49(49, 23, 30);

    private int id;
    private int hourOfDay;
    private int minuteOfHour;

    NoxboxTime(int id, int hourOfDay, int minuteOfHour) {
        this.id = id;
        this.hourOfDay = hourOfDay;
        this.minuteOfHour = minuteOfHour;
    }

    public static String[] getAllAsString() {
        String[] strings = new String[NoxboxTime.values().length];
        for (NoxboxTime value : NoxboxTime.values()) {
            if(value.id % 2 == 0){
                strings[value.id] = value.hourOfDay + ":" + value.minuteOfHour + "0";
                continue;
            }
            strings[value.id] = value.hourOfDay + ":" + value.minuteOfHour;
        }
        return strings;
    }
    public static NoxboxTime byId(int id){
        for (NoxboxTime noxboxTime : NoxboxTime.values()) {
            if (noxboxTime.id == id) {
                return noxboxTime;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public int getMinuteOfHour() {
        return minuteOfHour;
    }
}
