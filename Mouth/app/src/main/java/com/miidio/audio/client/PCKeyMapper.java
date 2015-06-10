package com.miidio.audio.client;

import java.util.HashMap;

public class PCKeyMapper {
    private static HashMap<Integer, Integer> map = new HashMap<>();

    // Since the server is written in Java. We should map the keycode to java.awt.event.KeyEvent.
    // But we should change to python version once we want to merge with other open source
    // libraries.
    static {
        map.put(29, 65);//A
        map.put(30, 66);//B
        map.put(31, 67);//C
        map.put(32, 68);//D
        map.put(33, 69);//E
        map.put(34, 70);//F
        map.put(35, 71);//G
        map.put(36, 72);//H
        map.put(37, 73);//I
        map.put(38, 74);//J
        map.put(39, 75);//K
        map.put(40, 76);//L
        map.put(41, 77);//M
        map.put(42, 78);//N
        map.put(43, 79);//O
        map.put(44, 80);//P
        map.put(45, 81);//Q
        map.put(46, 82);//R
        map.put(47, 83);//S
        map.put(48, 84);//T
        map.put(49, 85);//U
        map.put(50, 86);//V
        map.put(51, 87);//W
        map.put(52, 88);//X
        map.put(53, 89);//Y
        map.put(54, 90);//Z
        map.put(7, 48);//0
        map.put(8, 49);//1
        map.put(9, 50);//2
        map.put(10, 51);//3
        map.put(11, 52);//4
        map.put(12, 53);//5
        map.put(13, 54);//6
        map.put(14, 55);//7
        map.put(15, 56);//8
        map.put(16, 57);//9
        map.put(66, 10);//ENTER
        map.put(111, 27);//ESCAPE
        map.put(67, 8);//BACKSPACE
        map.put(61, 9);//TAB
        map.put(62, 32);//SPACEBAR
        map.put(69, 45);//MINUS
        map.put(70, 61);//EQUAL
        map.put(71, 91);//LEFTBRACE
        map.put(72, 93);//RIGHTBRACE
        map.put(73, 92);//BACKSLASH
        map.put(74, 59);//SEMICOLON
        map.put(75, 222);//APOSTROPHE
        map.put(68, 192);//GRAVE
        map.put(55, 44);//COMMA
        map.put(56, 46);//DOT
        map.put(57, 42);//SLASH
        map.put(131, 112);//F1
        map.put(132, 113);//F2
        map.put(133, 114);//F3
        map.put(134, 115);//F4
        map.put(135, 116);//F5
        map.put(136, 117);//F6
        map.put(137, 118);//F7
        map.put(138, 119);//F8
        map.put(139, 120);//F9
        map.put(140, 121);//F10
//        We use F11 and F12 to replace meta and alt for overriding android shortcuts.
//        map.put(141, 122);//F11
//        map.put(142, 123);//F12
        map.put(120, 154);//SRNPRT
        map.put(116, 145);//SCROLLLOCK
        map.put(121, 19);//PAUSE
        map.put(124, 155);//INSERT
        map.put(122, 36);//HOME
        map.put(92, 33);//PAGEUP
        map.put(93, 34);//PAGEDOWN
        map.put(112, 127);//FORWARD_DELETE
        map.put(123, 35);//END
        map.put(19, 38);//UP
        map.put(20, 40);//DOWN
        map.put(21, 37);//LEFT
        map.put(22, 39);//RIGHT
        map.put(143, 144);//NUMLOCK
        map.put(154, 111);//NUMPAD_DIVIDE
        map.put(155, 106);//NUMPAD_MULTIPLY
        map.put(156, 109);//NUMPAD_SUBTRACT
        map.put(157, 107);//NUMPAD_ADD
        map.put(144, 96);//NUMPAD_0
        map.put(145, 97);//NUMPAD_1
        map.put(146, 98);//NUMPAD_2
        map.put(147, 99);//NUMPAD_3
        map.put(148, 100);//NUMPAD_4
        map.put(149, 101);//NUMPAD_5
        map.put(150, 102);//NUMPAD_6
        map.put(151, 103);//NUMPAD_7
        map.put(152, 104);//NUMPAD_8
        map.put(153, 105);//NUMPAD_9
        map.put(158, 46);//NUMPAD_DOT
        map.put(79, 92);//NUMPAD_BACKSLASH
        map.put(161, 61);//NUMPAD_EQUALS
//        Java doesn't support media keys.
//        map.put(86, ??);//MEDIA_STOP
//        map.put(164, ??);//VOLUME_MUTE
//        map.put(24, ??);//VOLUME_UP
//        map.put(25, ??);//VOLUME_DOWN
        map.put(158, 44);//NUMPAD_COMMA
        map.put(76, 111);//DIVIDE
//        map.put(57, 18);//LEFTALT
//        map.put(58, 18);//RIGHTALT
        map.put(141, 18);// remap F11 to alt, that's on purpose
        map.put(59, 16);//LEFTSHIFT
        map.put(60, 16);//RIGHTSHIFT
        map.put(113, 17);//LEFTCTRL
        map.put(114, 17);//RIGHTCTRL
//        map.put(117, 157);//LEFTMETA
//        map.put(118, 157);//RIGHTMETA
        map.put(142, 157);// remap F12 to meta, that's on purpose
    }

    public static int get(int key) {
        return map.containsKey(key) ? map.get(key) : 0;
    }
}
