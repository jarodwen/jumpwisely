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
/// User Defined skyline query: data upload, bilk add and computation
/// </summary>
/// <author>Jarod Wen</author>
/// <Date>20:18pm, Nov 26th, 2006</Date>
public partial class UserDefine_2 : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        if (!IsPostBack)
        {
            string strSchemaID = Request.QueryString["SchemaID"];
            InitializeData(strSchemaID);
            InitializeDataSetDdl();
        }
    }
    /// <summary>
    /// Initialize the drop down list on UserDefine_2.aspx with name of data table in DB.
    /// </summary>
    private void InitializeDataSetDdl()
    {
        SqlConnection conn = new SqlConnection(System.Configuration.ConfigurationManager.AppSettings["DB_NBAConnectionString"]);
        SqlDataAdapter adapter = new SqlDataAdapter("SELECT name From sysobjects WHERE xtype = 'u' AND name <> 'Schemas'", conn);
        DataTable dt = new DataTable();
        try
        {
            conn.Open();
            adapter.Fill(dt);
            conn.Close();
        }
        catch (Exception ex)
        {
            conn.Close();
            SystemLog.WriteException(ex.ToString());
        }
        //Insert a blank row into the data table.
        DataRow drNew = dt.NewRow();
        drNew["name"] = "";
        dt.Rows.InsertAt(drNew, 0);
        this.dddlDataSets.DataSource = dt;
        this.dddlDataSets.DataTextField = "name";
        this.dddlDataSets.DataValueField = "name";
        this.dddlDataSets.DataBind();
    }
    /// <summary>
    /// Read data schema from DB, according the ID of schema user selected in the previous page. 
    /// </summary>
    /// <param name="strSchemaID">Schema ID</param>
    private void InitializeData(string strSchemaID)
    {
        this.cblType.Items.Clear();
        this.cblAttri.Items.Clear();
        SqlConnection conn = new SqlConnection(System.Configuration.ConfigurationManager.AppSettings["DB_NBAConnectionString"]);
        SqlDataAdapter adapter = new SqlDataAdapter("SELECT * FROM Schemas WHERE SchemaID='" + strSchemaID + "'", conn);
        DataTable dt = new DataTable();
        try
        {
            conn.Open();
            adapter.Fill(dt);
            conn.Close();
        }
        catch (Exception ex)
        {
            conn.Close();
            SystemLog.WriteException(ex.ToString());
        }
        string[] strColumns = dt.Rows[0]["Columns"].ToString().Split(',');
        foreach (string strCol in strColumns)
        {
            ListItem lti = new ListItem(strCol);
            this.cblAttri.Items.Add(lti);
            this.cblType.Items.Add(lti);
        }
        this.lblSchema.Text = dt.Rows[0]["SchemaName"].ToString();
    }
    protected void Button1_Click(object sender, EventArgs e)
    {
        if (this.FileUpload1.FileName == ""&&this.dddlDataSets.SelectedValue=="")
        {
            Response.Write("<script language=javascript>alert('Forget to upload your data file?');</script>");
            return;
        }
        //Get query information from user perference for skyline query
        ArrayList alQueryCol = new ArrayList();
        ArrayList alQueryType = new ArrayList();
        DataTable dtData = new DataTable();
        //This is a prefix to avoid same column names in the schema
        int intRandomperfix = 1;
        for (int i = 0; i < this.cblAttri.Items.Count; i++)
        {
            //If there are some same column names, add prefix to it
            string strColumnName = "";
            if (dtData.Columns.Contains(this.cblAttri.Items[i].Text))
            {
                strColumnName = this.cblAttri.Items[i].Text + "_" + intRandomperfix.ToString();
                intRandomperfix++;
            }
            else
            {
                strColumnName = this.cblAttri.Items[i].Text;
            }
            //Add attributions into the data table
            if (this.cblAttri.Items[i].Selected)
            {
                dtData.Columns.Add(strColumnName, System.Type.GetType("System.Double"));
                alQueryCol.Add(strColumnName);
                if (this.cblType.Items[i].Selected)
                {
                    alQueryType.Add(1);
                }
                else
                {
                    alQueryType.Add(0);
                }
            }
            else
            {
                dtData.Columns.Add(strColumnName);
            }
        }
        //User upload their own data file
        if (this.dddlDataSets.SelectedValue == "")
        {
            if (this.cbxSave2Db.Checked&&this.tbxDtName.Text.Trim() == "")
            {
                Response.Write("<script language=javascript>alert('No name for your table?');</script>");
                return;
            }
            string[] strFileContent = Encoding.Default.GetString(this.FileUpload1.FileBytes).Split('\n');
            dtData = ReadFile2Table(strFileContent, dtData);
        }
        else //Read data from database
        {
            dtData = GetDataSet(this.dddlDataSets.SelectedValue);
        }
        //if (alQueryType.Count != alQueryCol.Count)
        //{
        //    Response.Write("<script language=javascript>alert('Type Selection doesn't match the Attribution selection?');</script>");
        //    return;
        //}
        //ArrayList alSkyline = BasicSkyline.BasicBNLSkyline(dtData, alQueryCol, alQueryType);
        //this.lblNumObj.Text = "Total number of skyline objects: " + alSkyline.Count;
        //DataTable dtResult = new DataTable();
        //foreach (DataColumn dcTemp in dtData.Columns)
        //{
        //    dtResult.Columns.Add(dcTemp.ColumnName, dcTemp.DataType);
        //}
        //foreach (object intRow in alSkyline)
        //{
        //    dtResult.ImportRow(dtData.Rows[(int)intRow]);
        //}
        //this.GridView1.DataSource = dtResult.DefaultView;
        //this.GridView1.DataBind();
        //this.GridView1.AllowSorting = true;
        //this.GridView1.AllowPaging = true;
    }
    /// <summary>
    /// Read data from DB according to the user's selection on data table in the drop down list.
    /// </summary>
    /// <param name="strDataTableName"></param>
    /// <returns></returns>
    private DataTable GetDataSet(string strDataTableName)
    {
        SqlConnection conn = new SqlConnection(System.Configuration.ConfigurationManager.AppSettings["DB_NBAConnectionString"]);
        SqlDataAdapter adapter = new SqlDataAdapter("SELECT * FROM " + strDataTableName, conn);
        DataTable dtReturn = new DataTable();
        try
        {
            conn.Open();
            adapter.Fill(dtReturn);
            conn.Close();
        }
        catch (Exception ex)
        {
            conn.Close();
            SystemLog.WriteException(ex.ToString());
        }
        return dtReturn;
    }
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
        if (cbxSave2Db.Checked)
        {
            if (blnBulk2DB(dtData, this.tbxDtName.Text.Trim()))
            {
                return dtData;
            }
            else
            {
                Response.Write("<script language=javascript>alert('Upload your data fail. Please see the system log.');</script>");
            }
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
