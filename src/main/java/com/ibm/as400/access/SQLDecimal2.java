///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SQLDecimal2.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2006 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Date;
/* ifdef JDBC40 */
import java.sql.NClob;
import java.sql.RowId;
/* endif */ 
import java.sql.SQLException;
/* ifdef JDBC40 */
import java.sql.SQLXML;
/* endif */ 
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

final class SQLDecimal2
extends SQLDataBase
{ 
    static final String copyright = "Copyright (C) 1997-2006 International Business Machines Corporation and others.";

    // Private data.
    private int                     precision_;
    private int                     scale_;
    private AS400PackedDecimal      typeConverter_;
    private double                  value_;
    private JDProperties            properties_;  // @M0A - added JDProperties so we can get the scale & precision
    private int                     vrm_;         // @M0A

    SQLDecimal2(int precision,
                int scale,
                SQLConversionSettings settings,
                int vrm,                  // @M0C
                JDProperties properties)  // @M0C
    {
        super(settings); 
        precision_      = precision;
        scale_          = scale;
        typeConverter_  = new AS400PackedDecimal(precision_, scale_);
        value_          = 0;
        vrm_            = vrm;         // @M0A
        properties_     = properties;  // @M0A
    }

    public Object clone()
    {
        return new SQLDecimal2(precision_, scale_, settings_, vrm_, properties_);  // @M0C
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSION TO AND FROM RAW BYTES                        //
    //                                                         //
    //---------------------------------------------------------//

    public void convertFromRawBytes(byte[] rawBytes, int offset, ConvTable ccisdConverter, boolean ignoreConversionErrors) //@P0C
    throws SQLException
    {
        value_ = typeConverter_.toDouble(rawBytes, offset);
    }

    public void convertToRawBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter) //@P0C
    throws SQLException
    {
        typeConverter_.toBytes(value_, rawBytes, offset);
    }

    //---------------------------------------------------------//
    //                                                         //
    // SET METHODS                                             //
    //                                                         //
    //---------------------------------------------------------//

    public void set(Object object, Calendar calendar, int scale)
    throws SQLException
    {
        if(object instanceof String)
        {
            try
            {
                value_ = Double.valueOf((String)object).doubleValue();
            }
            catch(NumberFormatException nfe)
            {                                // @E4A
                JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH, nfe); // @E4A
            }
        }

        else if(object instanceof Number)
            value_ = ((Number)object).doubleValue();

        else if(object instanceof Boolean)
            value_ = ((Boolean)object).booleanValue() ? 1 : 0;

        else
            JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
    }

    //---------------------------------------------------------//
    //                                                         //
    // DESCRIPTION OF SQL TYPE                                 //
    //                                                         //
    //---------------------------------------------------------//

    public int getSQLType()
    {
        return SQLData.DECIMAL_USING_DOUBLE;
    }

    public String getCreateParameters()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(AS400JDBCDriver.getResource("PRECISION",null));
        buffer.append(",");
        buffer.append(AS400JDBCDriver.getResource("SCALE",null));
        return buffer.toString();
    }

    public int getDisplaySize()
    {
        return precision_ + 2;
    }

    //@F1A JDBC 3.0
    public String getJavaClassName()
    {
        return "java.math.BigDecimal";   
    }

    public String getLiteralPrefix()
    {
        return null;
    }

    public String getLiteralSuffix()
    {
        return null;
    }

    public String getLocalName()
    {
        return "DECIMAL";
    }

    public int getMaximumPrecision()
    {
        // @M0C - change to check vrm and JDProperties
        if(vrm_ >= JDUtilities.vrm530)
            return properties_.getInt(JDProperties.MAXIMUM_PRECISION);
        else
            return 31;
    }

    public int getMaximumScale()
    {
        // @M0C - change to check vrm and JDProperties
        if(vrm_ >= JDUtilities.vrm530)
            return properties_.getInt(JDProperties.MAXIMUM_SCALE);
        else
            return 31;
    }

    public int getMinimumScale()
    {
        return 0;
    }

    public int getNativeType()
    {
        return 484;
    }

    public int getPrecision()
    {
        return precision_;
    }

    public int getRadix()
    {
        return 10;
    }

    public int getScale()
    {
        return scale_;
    }

    public int getType()
    {
        return java.sql.Types.DECIMAL;
    }

    public String getTypeName()
    {
        return "DECIMAL";
    }

    public boolean isSigned()
    {
        return true;
    }

    public boolean isText()
    {
        return false;
    }

    public int getActualSize()
    {
        return precision_;
    }

    public int getTruncated()
    {
        return truncated_;
    }

    public boolean getOutOfBounds() {
      return outOfBounds_; 
    }

    //---------------------------------------------------------//
    //                                                         //
    // CONVERSIONS TO JAVA TYPES                               //
    //                                                         //
    //---------------------------------------------------------//


    public BigDecimal getBigDecimal(int scale)
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return new BigDecimal(value_);
    }

    public InputStream getBinaryStream()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Blob getBlob()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public boolean getBoolean()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return(value_ != 0);
    }

    public byte getByte()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Byte.MAX_VALUE || value_ < Byte.MIN_VALUE)
        {
            if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
            {
                if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
                {
                    truncated_ = 7; outOfBounds_=true;
                }
                else
                {
                    truncated_ = 3; outOfBounds_=true;
                }
            }
            else
            {
                truncated_ = 1; outOfBounds_=true;
            }
        }
        return(byte)value_;
    }

    public byte[] getBytes()
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }


    public Date getDate(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public double getDouble()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return value_;
    }

    public float getFloat()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Float.MAX_VALUE || value_ < -Float.MAX_VALUE)  //@trunc min_val is a posative number. //Float.MIN_VALUE)
        {
            truncated_ = 4; outOfBounds_=true;
        }
        return(float)value_;
    }

    public int getInt()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
        {
            truncated_ = 4; outOfBounds_=true;
        }
        return(int)value_;
    }

    public long getLong()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Long.MAX_VALUE || value_ < Long.MIN_VALUE)
        {
            truncated_ = 1; // this is not necessarily correct, but we know there is truncation
            outOfBounds_=true;
        }
        return(long)value_;
    }

    public Object getObject()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        return Double.valueOf(value_);
    }

    public short getShort()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        if(value_ > Short.MAX_VALUE || value_ < Short.MIN_VALUE)
        {
            if(value_ > Integer.MAX_VALUE || value_ < Integer.MIN_VALUE)
            {
                truncated_ = 6;
                outOfBounds_=true;
            }
            else
            {
                truncated_ = 2;
                outOfBounds_=true;
            }
        }
        return(short)value_;
    }

    public String getString()
    throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        String stringRep = Double.toString(value_);
        int decimal = stringRep.indexOf('.');
        if(decimal == -1)
            return stringRep;
        else
            return stringRep.substring(0, decimal)
            + settings_.getDecimalSeparator()
            + stringRep.substring(decimal+1);
    }

    public Time getTime(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    public Timestamp getTimestamp(Calendar calendar)
    throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    
    
    //@pda jdbc40
    public String getNString() throws SQLException
    {
        truncated_ = 0; outOfBounds_ = false; 
        String stringRep = Double.toString(value_);
        int decimal = stringRep.indexOf('.');
        if(decimal == -1)
            return stringRep;
        else
            return stringRep.substring(0, decimal)
            + settings_.getDecimalSeparator()
            + stringRep.substring(decimal+1);
    }

/* ifdef JDBC40 */

    //@pda jdbc40
      
    public RowId getRowId() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }

    //@pda jdbc40
    public SQLXML getSQLXML() throws SQLException
    {
        JDError.throwSQLException(this, JDError.EXC_DATA_TYPE_MISMATCH);
        return null;
    }
/* endif */ 
    
    public void saveValue() {
      savedValue_ = Double.valueOf(value_); 
   }
}
