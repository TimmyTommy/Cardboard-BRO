package de.tinf13aibi.cardboardbro.Entities.Lined;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tinf13aibi.cardboardbro.Entities.BaseEntity;
import de.tinf13aibi.cardboardbro.Entities.Interfaces.IEntity;
import de.tinf13aibi.cardboardbro.Geometry.Simple.Vec3d;

/**
 * Created by dthom on 15.04.2016.
 */
public class TextCharEntity extends BaseEntity implements IEntity {
    private char mChar = '\0';

    private ArrayList<PolyLineEntity> mLineArray = new ArrayList<>();

    public void setColor(float red, float green, float blue, float alpha) {
        for (PolyLineEntity entity : mLineArray) {
            entity.setColor(red, green, blue, alpha);
        }
    }

    @Override
    public void draw(float[] view, float[] perspective, float[] lightPosInEyeSpace) {
        for (PolyLineEntity entity : mLineArray) {
            entity.draw(view, perspective, lightPosInEyeSpace);
        }
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        return null;
    }

    @Override
    public void loadFromJsonObject(JSONObject jsonEntity) throws JSONException {

    }

    public TextCharEntity(int program, char chr){
        super();
        mChar = chr;
        mProgram = program;
        transformCharToLines(chr);
    }

    private void transformCharToLines(char chr){
        switch (chr){
            case 'b':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'Q':
            case 'W':
            case 'E':
            case 'R':
            case 'T':
            case 'Z':
            case 'U':
            case 'I':
            case 'O':
            case 'P':
            case 'Ü':
            case 'A':
            case 'S':
            case 'D':
            case 'F':
            case 'G':
            case 'H':
            case 'J':
            case 'K':
            case 'L':
            case 'Ö':
            case 'Ä':
            case 'Y':
            case 'X':
            case 'C':
            case 'V':
            case 'B':
            case 'N':
            case 'M':
            case '<':
            case '>':
            case ',':
            case '.':
            case '+':
            case '-':
            case '/':
            case '?':
            case '!':
            case ' ':
            case '\n':
                defaultChar();
        }
    }

    private void defaultChar(){
        mLineArray.clear();

        PolyLineEntity line;
        line = new PolyLineEntity(mProgram);
        line.addVert(new Vec3d(-0.9f, 0, -0.9f));
        line.addVert(new Vec3d(0.9f, 0, 0.9f));
        line.addVert(new Vec3d(0.9f, 0, -0.9f));
        line.addVert(new Vec3d(-0.9f, 0, 0.9f));
        line.addVert(new Vec3d(-0.9f, 0, -0.9f));
        mLineArray.add(line);
    }
}
