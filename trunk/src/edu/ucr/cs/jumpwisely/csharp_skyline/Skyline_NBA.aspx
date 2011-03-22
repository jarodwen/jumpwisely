<%@ Page Language="C#" AutoEventWireup="true" CodeFile="Skyline_NBA.aspx.cs" Inherits="Skyline_NBA" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>NBA Skyline Test</title>
</head>
<body>
    <form id="form1" runat="server">
    <div class="header">
        <h1>
            <img src="Images/NBA.jpg" alt="NBA Logo" />
            &nbsp; Skyline Query on NBA Player data set</h1>
        <hr />
    </div><p align=right><a href="Default.aspx">Return to Default Page...            </a></p>
    <table border=0 cellpadding=0 cellspacing=0>
    <tr width="100%" height="100%">
    <td width=25% style="height: 100%" bgcolor="lightcyan">
        <div><div class="attributions">
        <br />
        Please Select attributions to query on:<br />
        <p align=center><asp:CheckBoxList ID="cblAttri" runat="server" RepeatColumns="3" Height="49px" Width="280px" BackColor="#FFFFC0">
        </asp:CheckBoxList></p>
        Please select the time period:<br />
        FROM<asp:DropDownList ID="ddlFrom" runat="server">
        </asp:DropDownList>
        TO<asp:DropDownList ID="ddlTo" runat="server">
        </asp:DropDownList><br />
    </div>
    <div class=Numlimitation>
        <br />
        Number of players wish to see<br />(Leave it blank if you don't want any limitation):<br />
        <asp:TextBox ID="tbxNum" runat="server"></asp:TextBox></div>
    <div>
        <br />
        <asp:Button ID="btnQuery" runat="server" Text="Query!" OnClick="btnQuery_Click" /><br />
        <br /></div></div></td>
    <td width=75% valign=top align=center style="height: 100%">
        <div><br />Query Result:<br />
            <asp:Label ID="lblNumObj" runat="server"></asp:Label><br /><div class=results>
        <asp:GridView ID="GridView1" runat="server" CellPadding="4" ForeColor="#333333" GridLines="None"
            Width="90%">
            <FooterStyle BackColor="#507CD1" Font-Bold="True" ForeColor="White" />
            <RowStyle BackColor="#EFF3FB" />
            <EditRowStyle BackColor="#2461BF" />
            <SelectedRowStyle BackColor="#D1DDF1" Font-Bold="True" ForeColor="#333333" />
            <PagerStyle BackColor="#2461BF" ForeColor="White" HorizontalAlign="Center" />
            <HeaderStyle BackColor="#507CD1" Font-Bold="True" ForeColor="White" />
            <AlternatingRowStyle BackColor="White" />
        </asp:GridView>
        &nbsp;</div><div class=test id=testdata>
        Analysis on the running of algorithms:
        <asp:Label ID="lblTest" runat="server" Text=""></asp:Label></div></div></td></tr></table>
        <br />
        <hr />
        <div class=footer>
        Skyline Query Website<br />
        Last modified:<script language="JavaScript">
            <!--hide script from old browsers
            document.write(document.lastModified + "")// end hiding -->
</script></div>
    
    </form>
    
</body>
</html>
