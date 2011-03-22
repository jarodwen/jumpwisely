using System;
using System.Data;
using System.Configuration;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Web.UI.HtmlControls;
using System.IO;

/// <summary>
/// Manage all the errors directed to the methods in this class
/// </summary>
/// <author>Jarod Wen</author>
/// <Date>20:18pm, Nov 26th, 2006</Date>
public class SystemLog
{
    private static SystemLog m_instance = null;
    public static SystemLog Instance()
    {
        if (m_instance == null)
        {
            try
            {
                m_instance = new SystemLog();
            }
            catch (Exception ex)
            {
                m_instance = null;
            }
        }
        return m_instance;
    }
	public SystemLog()
	{
		//
		// TODO: Add constructor logic here
		//
	}
    /// <summary>
    /// Write Error Log into ErrLog.txt.
    /// </summary>
    /// <param name="strException">The content of Error.</param>
    public static void WriteException(string strException)
    {
        string FilePath = System.Configuration.ConfigurationManager.AppSettings["ProjectPath"] + "ErrLog.txt";
        //Check whether the log file is existing.
        if (!File.Exists(FilePath))
        {
            FileStream fs = new FileStream(FilePath, FileMode.CreateNew);
            fs.Close();
        }
        //Write error log.
        if (File.ReadAllBytes(FilePath).Length >= 240000)
        {
            File.WriteAllText(FilePath, DateTime.Now + "\t" + strException + "\n");
        }
        else
        {
            File.AppendAllText(FilePath, DateTime.Now + "\t" + strException + "\n");
        }
    }
}
