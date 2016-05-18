package de.tinf13aibi.cardboardbro.Shader;

/**
 * Created by dthom on 14.01.2016.
 */
public enum Textures {
    TextureNone(' '),

    TextureButtonCreateEntity(' '),
    TextureButtonCopyEntity(' '),
    TextureButtonMoveEntity(' '),
    TextureButtonDeleteEntity(' '),

    TextureButtonBack(' '),
    TextureButtonFreeLine(' '),
    TextureButtonPolyLine(' '),
    TextureButtonCylinder(' '),
    TextureButtonCuboid(' '),
    TextureButtonSphere(' '),
    TextureButtonText(' '),

//    char[] keyRow0 = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0','\b'};
//    char[] keyRow1 = new char[]{'Q', 'W', 'E', 'R', 'T', 'Z', 'U', 'I', 'O', 'P', 'Ü'};
//    char[] keyRow2 = new char[]{'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Ö', 'Ä'};
//    char[] keyRow3 = new char[]{'<', '>', 'Y', 'X', 'C', 'V', 'B', 'N', 'M', ',', '.'};
//    char[] keyRow4 = new char[]{'+', '-', '*', '/',      ' '     , '?', '!',   '\n'  };

    TextureKey0('0'),
    TextureKey1('1'),
    TextureKey2('2'),
    TextureKey3('3'),
    TextureKey4('4'),
    TextureKey5('5'),
    TextureKey6('6'),
    TextureKey7('7'),
    TextureKey8('8'),
    TextureKey9('9'),
    TextureKeyBackSpc('\b'),
    TextureKeyQ('Q'),
    TextureKeyW('W'),
    TextureKeyE('E'),
    TextureKeyR('R'),
    TextureKeyT('T'),
    TextureKeyZ('Z'),
    TextureKeyU('U'),
    TextureKeyI('I'),
    TextureKeyO('O'),
    TextureKeyP('P'),
    TextureKeyÜ('Ü'),
    TextureKeyA('A'),
    TextureKeyS('S'),
    TextureKeyD('D'),
    TextureKeyF('F'),
    TextureKeyG('G'),
    TextureKeyH('H'),
    TextureKeyJ('J'),
    TextureKeyK('K'),
    TextureKeyL('L'),
    TextureKeyÖ('Ö'),
    TextureKeyÄ('Ä'),
    TextureKeyY('Y'),
    TextureKeyX('X'),
    TextureKeyC('C'),
    TextureKeyV('V'),
    TextureKeyB('B'),
    TextureKeyN('N'),
    TextureKeyM('M'),
    TextureKeySmallerThan('<'),
    TextureKeyBiggerThan('>'),
    TextureKeyComma(','),
    TextureKeyDot('.'),
    TextureKeyPlus('+'),
    TextureKeyMinus('-'),
    TextureKeyStar('*'),
    TextureKeySlash('/'),
    TextureKeyQuestionMark('?'),
    TextureKeyExclamationMark('!'),
    TextureKeySpace(' '),
    TextureKeyEnter('\n');


    private char mCharValue;

    Textures(char value){
        mCharValue = value;
    }

    public char getValue() {
        return mCharValue;
    }

    public static Textures parseValue(char value) {
        for(Textures v : values()) {
            if (v.getValue() == value){
                return v;
            }
        }
        return TextureNone;
        //throw new IllegalArgumentException();
    }
}
