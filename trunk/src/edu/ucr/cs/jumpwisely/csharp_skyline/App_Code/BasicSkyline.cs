using System;
using System.Data;
using System.Configuration;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.Collections;

/// <summary>
/// BasicSkyline: Basic Skyline computation using BNL.
/// </summary>
/// <author>Jarod Wen</author>
/// <Date>20:18pm, Nov 26th, 2006</Date>
public class BasicSkyline
{
    private static BasicSkyline m_instance = null;
	public BasicSkyline()
	{
	}
    public static BasicSkyline Instance()
    {
        if (m_instance == null)
        {
            try
            {
                m_instance = new BasicSkyline();
            }
            catch(Exception ex)
            {
                m_instance = null;
            }
        }
        return m_instance;
    }
    /// <summary>
    /// Basic Skyline query using BNL
    /// </summary>
    /// <param name="dt">Source Data Table</param>
    /// <param name="columnList">Columns on which Skyline query will be processed</param>
    /// <returns>The list of index of all the skyline objects in source data table</returns>
    public static ArrayList BasicBNLSkyline(DataTable dt, ArrayList columnList)
    {
        ArrayList alWind = new ArrayList();
        alWind.Add(0);
        for (int i = 1; i < dt.Rows.Count; i++)
        {
            bool IsDominated = false;
            int size = alWind.Count;
            for (int j = size - 1; j >= 0; j--)
            {
                if (blnDominate(dt.Rows[i], dt.Rows[(int)alWind[j]], columnList, 0))
                {
                    alWind.RemoveAt(j);
                }
                else
                {
                    if (blnDominate(dt.Rows[(int)alWind[j]], dt.Rows[i], columnList, 0))
                    {
                        IsDominated = true;
                        break;
                    }
                }
            }
            if (!IsDominated)
            {
                alWind.Add(i);
            }
        }
        return alWind;
    }
    /// <summary>
    /// Restruct: Basic Skyline query using BNL
    /// </summary>
    /// <param name="dt">Source Data Table</param>
    /// <param name="columnList">Columns on which Skyline query will be processed</param>
    /// <param name="TypeList">The list of comparation type on each columns in the columnList</param>
    /// <returns>The list of index of all the skyline objects in source data table</returns>
    public static ArrayList BasicBNLSkyline(DataTable dt, ArrayList columnList, ArrayList TypeList)
    {
        ArrayList alWind = new ArrayList();
        alWind.Add(0);
        try
        {
            for (int i = 1; i < dt.Rows.Count; i++)
            {
                bool IsDominated = false;
                int size = alWind.Count;
                for (int j = size - 1; j >= 0; j--)
                {
                    if (blnDominate(dt.Rows[i], dt.Rows[(int)alWind[j]], columnList, TypeList))
                    {
                        alWind.RemoveAt(j);
                    }
                    else
                    {
                        if (blnDominate(dt.Rows[(int)alWind[j]], dt.Rows[i], columnList, TypeList))
                        {
                            IsDominated = true;
                            break;
                        }
                    }
                }
                if (!IsDominated)
                {
                    alWind.Add(i);
                }
            }
        }
        catch (Exception ex)
        {
            SystemLog.WriteException(ex.ToString());
            return alWind;
        }
        return alWind;
    }
    /// <summary>
    /// Restruct: Basic Skyline query using BNL for presorted skyline query
    /// </summary>
    /// <param name="dt">Source Data Table</param>
    /// <param name="columnList">Columns on which Skyline query will be processed</param>
    /// <param name="TypeList">The list of comparation type on each columns in the columnList</param>
    /// <param name="objList">The list of the rows to be compared.</param>
    /// <returns>The list of index of all the skyline objects in source data table</returns>
    public static ArrayList BasicBNLSkyline(DataTable dt, ArrayList columnList, ArrayList TypeList, ArrayList objList)
    {
        ArrayList alWind = new ArrayList();
        alWind.Add(0);
        try
        {
            foreach(int i in objList)
            {
                bool IsDominated = false;
                int size = alWind.Count;
                for (int j = size - 1; j >= 0; j--)
                {
                    if (blnDominate(dt.Rows[i], dt.Rows[(int)alWind[j]], columnList, TypeList))
                    {
                        alWind.RemoveAt(j);
                    }
                    else
                    {
                        if (blnDominate(dt.Rows[(int)alWind[j]], dt.Rows[i], columnList, TypeList))
                        {
                            IsDominated = true;
                            break;
                        }
                    }
                }
                if (!IsDominated)
                {
                    alWind.Add(i);
                }
            }
        }
        catch (Exception ex)
        {
            SystemLog.WriteException(ex.ToString());
            return alWind;
        }
        return alWind;
    }
    /// <summary>
    /// Skyline operator: dominate
    /// </summary>
    /// <param name="dr1">Left data object of the operator.</param>
    /// <param name="dr2">Right data object of the operator.</param>
    /// <param name="columnList">Columns on which Skyline query will be processed</param>
    /// <param name="intType">Comparation type: 0-Larger better; 1-Smaller better.</param>
    /// <returns>Whether left data object dominate the right one.</returns>
    private static bool blnDominate(DataRow dr1, DataRow dr2, ArrayList columnList, int intType)
    {
        int count = 0;
        foreach (string strCol in columnList)
        {
            switch (intType)
            {
                case 0:
                    if (Convert.ToDouble(dr1[strCol].ToString()) < Convert.ToDouble(dr2[strCol].ToString())) return false;
                    else if (Convert.ToInt32(dr1[strCol].ToString()) > Convert.ToInt32(dr2[strCol].ToString()))
                        count++;
                    break;
                case 1:
                    if (Convert.ToDouble(dr1[strCol].ToString()) > Convert.ToDouble(dr2[strCol].ToString())) return false;
                    else if (Convert.ToDouble(dr1[strCol].ToString()) < Convert.ToDouble(dr2[strCol].ToString()))
                        count++;
                    break;
            }
        }
        if (count > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    /// <summary>
    /// Restruct: Skyline operator: dominate
    /// </summary>
    /// <param name="dr1">Left data object of the operator.</param>
    /// <param name="dr2">Right data object of the operator.</param>
    /// <param name="columnList">Columns on which Skyline query will be processed</param>
    /// <param name="intType">Comparation type: 0-Larger better; 1-Smaller better.</param>
    /// <param name="TypeList">The list of comparation type on each columns in the columnList</param>
    /// <returns>Whether left data object dominate the right one.</returns>
    private static bool blnDominate(DataRow dr1, DataRow dr2, ArrayList columnList, ArrayList TypeList)
    {
        if (columnList.Count != TypeList.Count)
            return blnDominate(dr1,dr2,columnList, 0);
        int count = 0;
        for(int i=0;i<columnList.Count;i++)
        {
            string strCol = columnList[i].ToString();
            switch ((int)TypeList[i])
            {
                case 0:
                    if (Convert.ToDouble(dr1[strCol].ToString()) < Convert.ToDouble(dr2[strCol].ToString())) return false;
                    else if (Convert.ToDouble(dr1[strCol].ToString()) > Convert.ToDouble(dr2[strCol].ToString()))
                        count++;
                    break;
                case 1:
                    if (Convert.ToDouble(dr1[strCol].ToString()) > Convert.ToDouble(dr2[strCol].ToString())) return false;
                    else if (Convert.ToDouble(dr1[strCol].ToString()) < Convert.ToDouble(dr2[strCol].ToString()))
                        count++;
                    break;
            }
        }
        if (count > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public static ArrayList DC_BNLSkyline(DataTable dt, ArrayList columnList, ArrayList TypeList)
    {
        int RowsCount = dt.Rows.Count;
        int intDivide = 1;
        while (RowsCount / intDivide > 2000)
        {
            intDivide *= 2;
        }
        int DCCount = RowsCount / intDivide;
        int intStart = 0;
        ArrayList alSkyline = new ArrayList();
        for (; intDivide > 0; intDivide--)
        {
            ArrayList alTempCounter = new ArrayList();
            int j = intStart;
            for (; j - intStart < DCCount && j <= RowsCount; j++)
            {
                alTempCounter.Add(j);
            }
            intStart += j;
            alTempCounter = BasicBNLSkyline(dt, columnList, TypeList, alTempCounter);
            foreach (object objSkyline in alTempCounter)
            {
                alSkyline.Add(objSkyline);
            }
        }
        ArrayList alCounter = new ArrayList();
        while (intStart < RowsCount - 1)
        {
            alCounter.Add(intStart);
        }
        alCounter = BasicBNLSkyline(dt, columnList, TypeList, alCounter);
        foreach (object objSkyline in alCounter)
        {
            alSkyline.Add(objSkyline);
        }
        alSkyline = BasicBNLSkyline(dt, columnList, TypeList, alSkyline);
        return alSkyline;
    }
}
