using System;
using System.Data;
using System.Configuration;
using System.Collections;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.Data.SqlClient;
using System.Text;

/// <summary>
/// User Defined skyline query: Schemes upload, select and modify
/// </summary>
/// <author>Jarod Wen</author>
/// <Date>20:18pm, Nov 26th, 2006</Date>
public partial class UserDefine_1 : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
    }
    protected void GridView1_SelectedIndexChanged(object sender, EventArgs e)
    {
        Response.Redirect("UserDefine_2.aspx?SchemaID=" + this.GridView1.SelectedRow.Cells[1].Text);
    }
    protected void btnUpload_Click(object sender, EventArgs e)
    {
        if (this.TextBox1.Text == "")
        {
            Response.Write("<script lang=javascript>alert('Forget to specify the name of your schema?')</script>");
            return;
        }
        if (this.FileUpload1.FileName == "")
        {
            Response.Write("<script lang=javascript>alert('Forget to upload your file?')</script>");
            return;
        }
        string strColumnText = "";
        DataTable dtData = new DataTable();
        string[] strLine = Encoding.Default.GetString(this.FileUpload1.FileBytes).Split('\n');
        foreach (string strL in strLine)
        {
            if (strL.Trim() == "")
            {
                continue;
            }
            string[] strColumns = strL.Split('\t');
            foreach (string strTemp in strColumns)
            {
                dtData.Columns.Add(strTemp.Trim());
            }
            break;
        }

        dtData = ReadFile2Table(strLine, dtData);
        //strColumnText = strColumnText.Substring(0, strColumnText.Length-1);
        //SqlConnection conn = new SqlConnection(System.Configuration.ConfigurationSettings.AppSettings["DB_NBAConnectionString"]);
        //SqlCommand sqlComd = new SqlCommand("INSERT INTO Schemas (SchemaName, Columns) VALUES ('" + this.TextBox1.Text + "', '" + strColumnText + "')",conn);
        //try
        //{
        //    conn.Open();
        //    sqlComd.ExecuteNonQuery();
        //    conn.Close();
        //}
        //catch (Exception ex)
        //{
        //    conn.Close();
        //    SystemLog.WriteException(ex.ToString());
        //}
        Response.Redirect("UserDefine_1.aspx");
    }
    //////////////////////////////
    /// <summary>
    /// Read data from the user text file and also do the bulk upload if necessary
    /// </summary>
    /// <param name="strFileContent">Contents of the file user uploaded</param>
    /// <param name="dtData">The structure of the table for storing</param>
    /// <returns>Data table filled with data.</returns>
    private DataTable ReadFile2Table(string[] strFileContent, DataTable dtData)
    {

        foreach (string strDataRows in strFileContent)
        {
            DataRow dr = dtData.NewRow();
            string[] strTemp = strDataRows.Split('\t');
            for (int i = 0; i < strTemp.Length; i++)
            {
                if (dr[i].GetType() == System.Type.GetType("System.Double") && strTemp[i] == "")
                    dr[i] = 0;
                else
                    dr[i] = strTemp[i];
            }
            dtData.Rows.Add(dr);
        }
        //Bulk upload data into DB
            if (blnBulk2DB(dtData, this.TextBox1.Text.Trim()))
            {
                return dtData;
            }
            else
            {
                Response.Write("<script language=javascript>alert('Upload your data fail. Please see the system log.');</script>");
            }
        return dtData;
    }
    /// <summary>
    /// Bulk add data using SqlBulkCopy in .NET 2.0
    /// </summary>
    /// <param name="dtSource">Data Table containing data which will be added into database</param>
    /// <param name="strTblName">The name of table user want to store as.</param>
    /// <returns>Whether the bulk add succeeds</returns>
    private bool blnBulk2DB(DataTable dtSource, string strTblName)
    {
        SqlConnection conn = new SqlConnection(System.Configuration.ConfigurationManager.AppSettings["DB_NBAConnectionString"]);
        conn.Open();
        //Start transaction
        SqlTransaction trans = conn.BeginTransaction();
        //Construct the T-sql for Create new table
        string strCreateSql = "CREATE TABLE " + strTblName + " (";
        foreach (DataColumn dcIter in dtSource.Columns)
        {
            strCreateSql += dcIter.ColumnName + " ";
            if (dcIter.DataType == System.Type.GetType("System.Double"))
            {
                strCreateSql += "Float,";
            }
            else
            {
                strCreateSql += "VARCHAR(MAX),";
            }
        }
        if (strCreateSql.EndsWith(","))
        {
            strCreateSql = strCreateSql.Substring(0, strCreateSql.Length - 1);
        }
        strCreateSql += ");";
        try
        {
            //Try to create the new table
            SqlCommand sqlCmd = new SqlCommand(strCreateSql, conn, trans);
            int intResult = sqlCmd.ExecuteNonQuery();
            //Start to bulk add data into new table
            SqlBulkCopy sqlBulkObj = new SqlBulkCopy(conn, SqlBulkCopyOptions.KeepIdentity, trans);
            sqlBulkObj.DestinationTableName = strTblName;
            sqlBulkObj.WriteToServer(dtSource);
            trans.Commit();
            conn.Close();
            return true;
        }
        catch (Exception ex)
        {
            trans.Rollback();
            conn.Close();
            SystemLog.WriteException(ex.ToString());
            return false;
        }
    }
}
