package com.chanse.messaging.fields;

import com.chanse.messaging.utils.SaveLoadUtils;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TODO
 */
public class InterfaceDataFieldTest {

    @Test
    public void quickTestToSeeSaveLoadFormat() {
        EnumDataField randomField = new EnumDataField();

        randomField.getAllowedEnums().add(new EnumDataField.FieldSpecificEnum("Zero Value", new Integer(0)));
        randomField.getAllowedEnums().add(new EnumDataField.FieldSpecificEnum("One Value", new Integer(1)));
        randomField.getAllowedEnums().add(new EnumDataField.FieldSpecificEnum("Three Value", new Integer(3)));
        randomField.getAllowedEnums().add(new EnumDataField.FieldSpecificEnum("Six Value", new Integer(6)));

        randomField.setBitLength(6);
        randomField.setBitOffset(9);


        try {
            randomField.setDataBinaryString("000001");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }


        JsonObject answerAsJson = (JsonObject) SaveLoadUtils.Instance.myRegisteredGson.toJsonTree(randomField, InterfaceDataField.class);
        System.out.println(answerAsJson);

        assertNotNull(answerAsJson);
    }

    @Test
    public void setBitLength() {
    }

    @Test
    public void updateBinaryString() {
    }

    @Test
    public void updateDataValue() {
    }

    @Test
    public void addFieldListener() {
    }

    @Test
    public void removeFieldListener() {
    }

    @Test
    public void setDataValue() {
    }

    @Test
    public void setDataBinaryString() {
    }

    @Test
    public void testClone() {
    }

    @Test
    public void testEquals() {
    }
}