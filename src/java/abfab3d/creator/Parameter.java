/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.creator;

// External Imports

/**
 * A parameter to an editor.
 *
 * TODO: Add an ability to chain kernels together ie a Parameter could be the
 * output of another GeometryKernel or a model reference.
 *
 * @author Alan Hudson
 */
public class Parameter implements Comparable {
    /** The data type */
    public enum DataType {STRING, ENUM, DOUBLE, BOOLEAN, INTEGER, FORMULA,
                          STRING_LIST, ENUM_LIST, DOUBLE_LIST, BOOLEAN_LIST, INTEGER_LIST};

    /** The editor to use */
    public enum EditorType {
       DEFAULT,     // Let the libary choose based on DataType
       CONSTANT,    // Constant value, no editing
       FILE_DIALOG, // File Dialog Box
       IMAGE_MAP,    // 2D location from an image map
       TEXT_AREA    // Large amount of text
    };

    /** The name of the parameter.  Must be unique for the editor */
    private String name;

    /** The user friendly name string */
    private String nameDesc;

    /** The description */
    private String desc;

    /** The dataType */
    private DataType dataType;

    /** The editor to use */
    private EditorType editorType;

    /** Unit conversion to meters */
    private double meterConversion;

    /** What step to edit in */
    private int step;

    /** What sequence in step to edit in */
    private int seq;

    /** Is this an advanced item */
    private boolean advanced;

    /** Min range for numeric values */
    private double minRange;

    /** Max range for numeric values */
    private double maxRange;

    /** Fixed values for numeric data */
    private double[] fixedValues;

    /** Specify Values for Enumeration */
    private String[] enumValues;

    /** The default value */
    private String defaultValue;

    public Parameter(String name, String nameDesc, String desc, String defaultValue, double meterConversion,
        DataType dataType, EditorType editorType,
        int step, int seq, boolean advanced, double minRange, double maxRange,
        double[] fixedValues, String[] enumValues) {

        this.name = name;
        this.nameDesc = nameDesc;
        this.desc = desc;
        this.defaultValue = defaultValue;
        this.meterConversion = meterConversion;
        this.dataType = dataType;
        this.editorType = editorType;
        this.step = step;
        this.seq = seq;
        this.advanced = advanced;
        this.minRange = minRange;
        this.maxRange = maxRange;

        if (fixedValues != null) {
            this.fixedValues = fixedValues.clone();
        }

        if (enumValues != null) {
            this.enumValues = enumValues.clone();
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getNameDesc() {
        return nameDesc;
    }

    /**
     * @param name the name to set
     */
    public void setNameDesc(String name) {
        this.nameDesc = name;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return the meterConversion
     */
    public double getMeterConversion() {
        return meterConversion;
    }

    /**
     * @param meterConversion the meterConversion to set
     */
    public void setMeterConversion(double meterConversion) {
        this.meterConversion = meterConversion;
    }

    /**
     * @return the step
     */
    public int getStep() {
        return step;
    }

    /**
     * @param step the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * @return the seq
     */
    public int getSeq() {
        return seq;
    }

    /**
     * @param seq the seq to set
     */
    public void setSeq(int seq) {
        this.seq = seq;
    }

    /**
     * @return the advanced
     */
    public boolean isAdvanced() {
        return advanced;
    }

    /**
     * @param advanced the advanced to set
     */
    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    /**
     * @return the minRange
     */
    public double getMinRange() {
        return minRange;
    }

    /**
     * @param minRange the minRange to set
     */
    public void setMinRange(double minRange) {
        this.minRange = minRange;
    }

    /**
     * @return the maxRange
     */
    public double getMaxRange() {
        return maxRange;
    }

    /**
     * @param maxRange the maxRange to set
     */
    public void setMaxRange(double maxRange) {
        this.maxRange = maxRange;
    }

    /**
     * @return the fixedValues
     */
    public double[] getFixedValues() {
        return fixedValues;
    }

    /**
     * @param fixedValues the fixedValues to set
     */
    public void setFixedValues(double[] fixedValues) {
        if (fixedValues == null) {
            this.fixedValues = null;
        } else {
            this.fixedValues = fixedValues.clone();
        }
    }

    /**
     * @return the enumValues
     */
    public String[] getEnumValues() {
        return enumValues;
    }

    /**
     * @param enumValues the enumValues to set
     */
    public void setEnumValues(String[] enumValues) {
        if (enumValues == null) {
            this.enumValues = null;
        } else {
            this.enumValues = enumValues.clone();
        }
    }

    /**
     * Set the dataType.
     *
     * @param val The new value
     */
    public void setDataType(DataType val) {
        dataType = val;
    }

    /**
     * Get the dataType
     *
     * @return The dataType
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Set the editorType.
     *
     * @param val The new value
     */
    public void setEditorType(EditorType val) {
        editorType = val;
    }

    /**
     * Get the editorType
     *
     * @return The editorType
     */
    public EditorType getEditorType() {
        return editorType;
    }

    public int compareTo(Object o2) {
        if (o2 instanceof Parameter) {
            Parameter p1 = this;
            Parameter p2 = (Parameter) o2;

            if (p1.getStep() == p2.getStep()) {
                return (p1.getSeq() < p2.getSeq() ? -1 : (p1.getSeq()==p2.getSeq() ? 0 : 1));
            } else {
                return (p1.getStep() < p2.getStep() ? -1 : (p1.getStep()==p2.getStep() ? 0 : 1));
            }
        }

        return 1;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        return false;
    }
}