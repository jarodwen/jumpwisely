<%@ Page Language="C#" AutoEventWireup="true" CodeFile="UserDefine_2.aspx.cs" Inherits="UserDefine_2" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>User Defined Skyline Query</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <h1>
            <img src="Images/UserDefine.png" />
            &nbsp;&nbsp;
            User Defined Skyline Query
        </h1>
<hr />
        <p align=right><a href=Default.aspx>Return to Default Page...            </a></p>
        <h2>
            Step2: Upload your data and Query!</h2></div>
            <div>
                <table width=100% height=100% cellpadding=0 cellspacing=0 border=0>
                    <tr width=98% align=center valign=middle>
                        <td align=center width=30% bgcolor="#ccffff"><p>
                            &nbsp;</p>
                            <p>
            You have selected the schema:
                                <asp:Label ID="lblSchema" runat="server" BackColor="#FFFFC0" Width="146px"></asp:Label>&nbsp;</p>
        <p>
            Please select the attributions you want to compare in the skyline query:</p>
        <p>
            <asp:CheckBoxList ID="cblAttri" runat="server" RepeatColumns="6">
            </asp:CheckBoxList>&nbsp;</p>
        <p>
            Select the attributions which will be "smaller-better"(default setup will be "larger-better"):</p>
        <p>
            <asp:CheckBoxList ID="cblType" runat="server" RepeatColumns="6">
            </asp:CheckBoxList>&nbsp;</p>
            <div style="visibility:hidden">How many objects do you want to see?<br />
            <asp:TextBox ID="TextBox1" runat="server"></asp:TextBox></div>
        <p>
            Select existing dataset in our database:</p>
                            <p>
                                <asp:DropDownList ID="dddlDataSets" runat="server" Width="251px">
                                </asp:DropDownList>&nbsp;</p>
                            <p>
                                OR Upload your new data file:
            <asp:FileUpload ID="FileUpload1" runat="server" /></p>
        <p>
            <asp:CheckBox ID="cbxSave2Db" runat="server" Text="Save my data into database with name:" />
            <asp:TextBox ID="tbxDtName" runat="server" Width="159px"></asp:TextBox>&nbsp;</p>
                <p>
            <asp:Button ID="Button1" runat="server" BackColor="#FFFF80" BorderStyle="None" Text="Query the Skyline!"
                Width="166px" OnClick="Button1_Click" />&nbsp;</p>
                            <p>
                                &nbsp;</p>
                        </td>
                        <td align=center valign=top width=70%><div>
        
        <p>
            &nbsp;</p>
                            <p>
                                <asp:Label ID="lblNumObj" runat="server"></asp:Label>&nbsp;</p>
                            <p>
            <asp:GridView ID="GridView1" runat="server" CellPadding="3" GridLines="Horizontal"
                PageSize="20" BackColor="White" BorderColor="#E7E7FF" BorderStyle="None" BorderWidth="1px">
                <FooterStyle BackColor="#B5C7DE" ForeColor="#4A3C8C" />
                <RowStyle BackColor="#E7E7FF" ForeColor="#4A3C8C" />
                <SelectedRowStyle BackColor="#738A9C" Font-Bold="True" ForeColor="#F7F7F7" />
                <PagerStyle BackColor="#E7E7FF" ForeColor="#4A3C8C" HorizontalAlign="Right" />
                <HeaderStyle BackColor="#4A3C8C" Font-Bold="True" ForeColor="#F7F7F7" />
                <AlternatingRowStyle BackColor="#F7F7F7" />
            </asp:GridView>
            &nbsp;</p>
    
    </div></td></tr></table></div><hr /><div class=footer>
        Skyline Query Website<br />
        Last modified:<script language="JavaScript">
            <!--hide script from old browsers
            document.write(document.lastModified + "")// end hiding -->
</script></div>
    </form>
</body>
</html>
