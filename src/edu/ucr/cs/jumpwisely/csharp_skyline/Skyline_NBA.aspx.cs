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

/// <summary>
/// Skyline query on NBA player data
/// </summary>
/// <author>Jarod Wen</author>
/// <Date>20:20pm, Nov 26th, 2006</Date>
public partial class Skyline_NBA : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        if (!IsPostBack)
            InitializeData();
    }

    /// <summary>
    /// Initializes the data.
    /// </summary>
    private void InitializeData()
    {
        //Clear the items in any controls containing data
        this.ddlFrom.Items.Clear();
        this.ddlTo.Items.Clear();
        this.cblAttri.Items.Clear();
        //Connection setup to database
        SqlConnection conn = new SqlConnection(System.Configuration.ConfigurationSettings.AppSettings["DB_NBAConnectionString"]);
        SqlDataAdapter adapter = new SqlDataAdapter("SELECT * FROM NBA_Player17 WHERE '1'='2'", conn);
        DataSet ds = new DataSet();
        try
        {
            conn.Open();
            adapter.Fill(ds);
            conn.Close();
        }
        catch (Exception ex)
        {
            SystemLog.WriteException(ex.ToString());
            conn.Close();
        }
        //Initialize the checklist of attributions
        for (int i = 4; i < ds.Tables[0].Columns.Count; i++)
        {
            ListItem lit = new ListItem(ds.Tables[0].Columns[i].ColumnName, i.ToString());
            this.cblAttri.Items.Add(lit);
        }
        adapter = new SqlDataAdapter("SELECT MIN(year),MAX(year) FROM NBA_Player17", conn);
        ds = new DataSet();
        try
        {
            conn.Open();
            adapter.Fill(ds);
            conn.Close();
        }
        catch (Exception ex)
        {
            SystemLog.WriteException(ex.ToString());
            conn.Close();
        }
        //Initialize the dropdownlist for year select
        for (int i = Convert.ToInt16(ds.Tables[0].Rows[0][0].ToString()); i <= Convert.ToInt16(ds.Tables[0].Rows[0][1].ToString()); i++)
        {
            this.ddlFrom.Items.Add(i.ToString());
            this.ddlTo.Items.Add(i.ToString());
        }
    }
    /// <summary>
    /// Handles the Click event of the btnQuery control.
    /// </summary>
    /// <param name="sender">The source of the event.</param>
    /// <param name="e">The <see cref="System.EventArgs"/> instance containing the event data.</param>
    protected void btnQuery_Click(object sender, EventArgs e)
    {
        if (Convert.ToInt16(this.ddlFrom.SelectedItem.Text) > Convert.ToInt16(this.ddlTo.SelectedItem.Text))
        {
            Response.Write("<script lang=js>alert('Please enter a validate time range!')</script>");
            return;
        }
        ArrayList altType = new ArrayList();
        string strSql = "SELECT id, firstname, lastname, year, ";
        ArrayList ColAlt = new ArrayList();
        for (int i = 0; i < this.cblAttri.Items.Count; i++)
        {
            if (cblAttri.Items[i].Selected)
            {
                strSql += cblAttri.Items[i].Text +",";
                ColAlt.Add(cblAttri.Items[i].Text);
                if (cblAttri.Items[i].Text == "turnover" || cblAttri.Items[i].Text == "pf" || cblAttri.Items[i].Text == "fgmiss" || cblAttri.Items[i].Text == "ftmiss" || cblAttri.Items[i].Text == "tpmiss")
                {
                    altType.Add(1);
                }
                else
                {
                    altType.Add(0);
                }
            }
        }
        if (ColAlt.Count == 0)
        {
            Response.Write("<script lang=js>alert('No attributions have been selected!')</script>");
            return;
        }
        strSql = strSql.Substring(0, strSql.Length - 1);
        //The starting time for testing
        DateTime dtTest = DateTime.Now;
        //Database query for raw data
        SqlConnection conn = new SqlConnection(System.Configuration.ConfigurationSettings.AppSettings["DB_NBAConnectionString"]);
        SqlDataAdapter adapter = new SqlDataAdapter(strSql + " FROM NBA_Player17 WHERE year>="
            + this.ddlFrom.SelectedItem.Text + " AND year<="
            + this.ddlTo.SelectedItem.Text, conn);
        DataSet ds = new DataSet();
        try
        {
            conn.Open();
            adapter.Fill(ds);
            conn.Close();
        }
        catch (Exception ex)
        {
            SystemLog.WriteException(ex.ToString());
            conn.Close();
        }
        //Database query end time
        DateTime dtTestDbQuery = DateTime.Now;
        //Skyline query using basic BNL
        ArrayList alSkyline = BasicSkyline.DC_BNLSkyline(ds.Tables[0], ColAlt, altType);
        //ArrayList alSkyline = Skyline_PreSort.SkylineQuery_Presorted(ds.Tables[0], ColAlt, altType);
        this.lblNumObj.Text = "Total Number of Skyline objects: " + alSkyline.Count;
        //Showing Results
        DateTime dtSkylineQuery = DateTime.Now;

        DataTable dtResult = new DataTable();
        foreach (DataColumn dcTemp in ds.Tables[0].Columns)
        {
            dtResult.Columns.Add(dcTemp.ColumnName, dcTemp.DataType);
        }
        foreach (object intRow in alSkyline)
        {
            dtResult.ImportRow(ds.Tables[0].Rows[(int)intRow]);
        }
        this.GridView1.DataSource = dtResult.DefaultView;
        this.GridView1.DataBind();
        //Epsilon Skyline Part
        ////////////////////////////////////
        
        ////////////////////////////////////
        DateTime dtEnd = DateTime.Now;
        this.lblTest.Text = @"#DataSet: " + ds.Tables[0].Rows.Count.ToString() + "\n"
            + @"#Attributes: " + ColAlt.Count.ToString() + "\n"
            + @"#Database Query Time: " + Convert.ToString(dtTestDbQuery - dtTest) + "\n"
            + @"#Skyline Query Time: " + Convert.ToString(dtSkylineQuery - dtTestDbQuery) + "\n"
            + @"#Showing Time: " + Convert.ToString(dtEnd - dtSkylineQuery);
    }
}
