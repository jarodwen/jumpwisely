<%@ Page Language="C#" AutoEventWireup="true"  CodeFile="Default.aspx.cs" Inherits="_Default" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>Skyline WebSite</title>
</head>
<body>
    <form id="form1" runat="server">
        <h1>
            <img src="Images/Skyline_Title.JPG" style="width: 199px; height: 138px" />Skyline Query Web Site</h1><div>
    <hr />
</div>
        <div>
        <br />
        <br />
        This site is for the research in Skyline Query.&nbsp;<br />
        <br />
        </div>
        <h2>
            <img src="Images/star.jpg" />
            &nbsp;&nbsp;
            <a href=Skyline_NBA.aspx>Use the data set of NBA players</a></h2><div>
        The NBA players Data set is downloaded from <a href="http://www.databasebasketball.com">
                    databaseBasketball.com</a> in September, 2006, in which there are NBA player
                statistics from 1946 to 2004. The data inculdes:<br />
            </div>
        <div>
            <ul>
                <li>id </li>
                <li>firstname </li>
                <li>lastname </li>
                <li>year </li>
                <li>gp: game played </li>
                <li>minutes: minutes played (not recorded until 1951) </li>
                <li>pts: total points </li>
                <li>oreb: offensive rebounds </li>
                <li>dreb: defensive rebounds </li>
                <li>reb: total rebounds = oreb+dreb </li>
                <li>asts: assists </li>
                <li>stl: steals (not recorded until 1973) </li>
                <li>blk: blocks (not recorded until 1973) </li>
                <li>turnover: turnover (not recorded until 1973) </li>
                <li>pf: personal fouls </li>
                <li>fgm: field goals made </li>
                <li>ftm: free throw made </li>
                <li>tpm: three points made (not recorded until 1979) </li>
                <li>fgmiss: field goals missed </li>
                <li>ftmiss: free throw missed </li>
                <li>tpmiss: three points missed (not recorded until 1979) </li>
            </ul>
        </div>
        <div>
        <h2>
            <img src="Images/star.jpg" />
            &nbsp;&nbsp;
            <a href=UserDefine_1.aspx>Use User defined data schema and data set... &nbsp;</a></h2>
            <p>
                This part the system will allow you to upload your own schema and use your own data
                for skyline query. All features in the NBA player skyline query can be used on your
                own data except for the performance evaluation.
            </p>
            <p>
                &nbsp;</p>
            <p>
                &nbsp;</p>
            <p>
                &nbsp;</p>
            <hr />
    
    </div>
    <div class=footer>
        Skyline Query Website<br />
        Last modified:<script language="JavaScript">
            <!--hide script from old browsers
            document.write(document.lastModified + "")// end hiding -->
</script></div>
    </form>
</body>
</html>
