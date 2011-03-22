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
/// Skyline_PreSort: Skyline computation with combination of SFS and BNL 
/// </summary>
 /// <author>Jarod Wen</author>
/// <Date>20:18pm, Nov 26th, 2006</Date>
public class Skyline_PreSort
{
    private static Skyline_PreSort m_instance = null;
	public Skyline_PreSort()
	{
	}
    public static Skyline_PreSort Instance()
    {
        if (m_instance == null)
        {
            try
            {
                m_instance = new Skyline_PreSort();
            }
            catch (Exception ex)
            {
                m_instance = null;
            }
        }
        return m_instance;
    }

    /// <summary>
    /// Presort all the values in each column of the data table, according to the sorting type specified in TyoeList.
    /// </summary>
    /// <param name="dtSource">Source data table</param>
    /// <param name="columnList">All the columns in the skyline query</param>
    /// <param name="TypeList">Sorting type: for each item, 0-AESC, 1-DESC</param>
    /// <returns>The table contains sorted index of the source data objects</returns>
    private static DataTable PreSort(DataTable dtSource, ArrayList columnList, ArrayList TypeList)
    {
        DataTable dtRtn = new DataTable();
        //Build the column due to the column list
        foreach (string strColumn in columnList)
        {
            DataColumn dc = new DataColumn(strColumn, System.Type.GetType("System.Double"));
            dtRtn.Columns.Add(dc);
        }
        //Build rows according to the total elements in dt
        for (int i = 0; i < dtSource.Rows.Count; i++)
        {
            DataRow dr = dtRtn.NewRow();
            dtRtn.Rows.Add(dr);
        }
        //Pre-sorting the data in every column
        foreach (string strCol in columnList)
        {
            string SortSeq = "";
            if(TypeList.IndexOf(strCol)>=0&&(int)TypeList[TypeList.IndexOf(strCol)]==1)
            {
                SortSeq = "DESC";
            }
            DataRow[] drTemp = dtSource.Select("", strCol + " " + SortSeq);
            for (int i = 0; i < drTemp.Length; i++)
            {
                //Here the default key is the first column.
                dtRtn.Rows[i][strCol] = dtSource.Rows.IndexOf(drTemp[i]);
            }
        }
        return dtRtn;
    }

    /// <summary>
    /// Skylines the query_ presorted.
    /// </summary>
    /// <param name="dt">The dt.</param>
    /// <param name="columnList">The column list.</param>
    /// <param name="TypeList">The type list.</param>
    /// <returns></returns>
    public static ArrayList SkylineQuery_Presorted(DataTable dt, ArrayList columnList, ArrayList TypeList)
    {
        //Initalize the count array for each objects in the Datatable dt.
        ArrayList alObjCount = new ArrayList();
        for (int i = 0; i < dt.Rows.Count; i++)
        {
            alObjCount.Add(0);
        }
        //Get the presorted data table
        DataTable dtPresorted = PreSort(dt, columnList, TypeList);
        int intColumnNum = dtPresorted.Columns.Count;
        //Select the skyline candidates from the presorted data
        for (int i = 0; i < dtPresorted.Rows.Count; i++)
        {
            for (int j = 0; j < dtPresorted.Columns.Count; j++)
            {
                int dataIndex = Convert.ToInt32(dtPresorted.Rows[i][j]);
                alObjCount[dataIndex] = Convert.ToInt32(alObjCount[dataIndex])+1;
                if (Convert.ToInt32(alObjCount[dataIndex]) == intColumnNum)
                {
                    for (int jj = 0; jj <= j; jj++)
                    {
                        string strColumnName = dtPresorted.Columns[jj].ColumnName;
                        int iii = i+1;
                        while (iii<dtPresorted.Rows.Count&&Convert.ToDouble(dt.Rows[Convert.ToInt32(dtPresorted.Rows[iii][jj])][strColumnName]) == Convert.ToDouble(dt.Rows[Convert.ToInt32(dtPresorted.Rows[i][jj])][strColumnName]))
                        {
                            alObjCount[Convert.ToInt32(dtPresorted.Rows[iii][jj])] = Convert.ToInt32(alObjCount[Convert.ToInt32(dtPresorted.Rows[iii][jj])]) + 1;
                            iii++;
                        }
                    }
                    for (int jj = j + 1; jj < intColumnNum; jj++)
                    {
                        string strColumnName = dtPresorted.Columns[jj].ColumnName;
                        int iii = i;
                        while (iii < dtPresorted.Rows.Count && Convert.ToDouble(dt.Rows[Convert.ToInt32(dtPresorted.Rows[iii][jj])][strColumnName]) == Convert.ToDouble(dt.Rows[Convert.ToInt32(dtPresorted.Rows[i - 1][jj])][strColumnName]))
                        {
                            alObjCount[Convert.ToInt32(dtPresorted.Rows[iii][jj])] = Convert.ToInt32(alObjCount[Convert.ToInt32(dtPresorted.Rows[iii][jj])]) + 1;
                            iii++;
                        }
                    }
                    break;
                }
            }
        }
        ArrayList objList = new ArrayList();
        for (int i = 0; i < dt.Rows.Count; i++)
        {
            if (Convert.ToInt32(alObjCount[i]) > 0)
                objList.Add(i);
        }
        return BasicSkyline.BasicBNLSkyline(dt, columnList, TypeList, objList);
    }
}
