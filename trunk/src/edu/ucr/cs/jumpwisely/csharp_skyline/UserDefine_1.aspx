<%@ Page Language="C#" AutoEventWireup="true" CodeFile="UserDefine_1.aspx.cs" Inherits="UserDefine_1" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head runat="server">
    <title>User Defined Skyline Query</title>
</head>
<body>
    <form id="form1" runat="server">
    <div class=header>
        <h1>
            <img src="Images/UserDefine.png" />
            &nbsp;&nbsp;
            User Defined Skyline Query<br />
            
        </h1><hr /><p align=right><a href=Default.aspx>Return to Default Page...            </a></p><h2>
            Step1: Select your data schema:<br />
            <br />
        </h2>
    </div>
    <div><table width=100% height=100% cellpadding=0 cellspacing=0 border=0>
        <tr width=98%>
            <td width=25% align=center valign=top bgcolor="#ccffff">
                <br />
                <br />
                Upload your new schema throught a file:<br />
                <br />
        <asp:FileUpload ID="FileUpload1" runat="server" />&nbsp;<br />
                <br />
        Name your new schema here:
                <br />
                <br />
        <asp:TextBox ID="TextBox1" runat="server"></asp:TextBox>
                <br />
                <br />
        <asp:Button ID="btnUpload"
            runat="server" BackColor="#80FF80" BorderStyle="None" Text="Upload" Width="79px" OnClick="btnUpload_Click" /></td>
            <td width=75% align=center valign=middle><div class = attributions>
        Here you can select from the existing data schemas(Press "Select" to go to the next
        step):<br />
        <br />
        Existing schemas:<br />
        <asp:GridView ID="GridView1" runat="server" DataSourceID="ObjectDataSource1" OnSelectedIndexChanged="GridView1_SelectedIndexChanged"
            Width="650px" AutoGenerateColumns="False" DataKeyNames="SchemaID">
            <AlternatingRowStyle BackColor="#FFE0C0" />
            <Columns>
                <asp:CommandField ShowEditButton="True" ShowSelectButton="True" />
                <asp:BoundField DataField="SchemaID" HeaderText="SchemaID" InsertVisible="False"
                    ReadOnly="True" SortExpression="SchemaID" />
                <asp:BoundField DataField="SchemaName" HeaderText="SchemaName" SortExpression="SchemaName" />
                <asp:BoundField DataField="Columns" HeaderText="Columns" SortExpression="Columns" />
            </Columns>
        </asp:GridView>
        <asp:ObjectDataSource ID="ObjectDataSource1" runat="server" DeleteMethod="Delete"
            InsertMethod="Insert" OldValuesParameterFormatString="original_{0}" SelectMethod="GetSchemaData"
            TypeName="dsSchemaTableAdapters.SchemasTableAdapter" UpdateMethod="Update">
            <DeleteParameters>
                <asp:Parameter Name="Original_SchemaID" Type="Int32" />
            </DeleteParameters>
            <UpdateParameters>
                <asp:Parameter Name="SchemaName" Type="String" />
                <asp:Parameter Name="Columns" Type="String" />
                <asp:Parameter Name="Original_SchemaID" Type="Int32" />
            </UpdateParameters>
            <InsertParameters>
                <asp:Parameter Name="SchemaName" Type="String" />
                <asp:Parameter Name="Columns" Type="String" />
            </InsertParameters>
        </asp:ObjectDataSource>
        <br />
        <br />
        Add new schemas:&nbsp;<asp:DetailsView ID="DetailsView1" runat="server" AutoGenerateRows="False"
            DataSourceID="ObjectDataSource1" Height="50px" Width="649px" DataKeyNames="SchemaID" DefaultMode="Insert">
            <Fields>
                <asp:BoundField DataField="SchemaID" HeaderText="SchemaID" InsertVisible="False"
                    ReadOnly="True" SortExpression="SchemaID" />
                <asp:BoundField DataField="SchemaName" HeaderText="SchemaName" SortExpression="SchemaName" />
                <asp:BoundField DataField="Columns" HeaderText="Columns" SortExpression="Columns" />
                <asp:CommandField ShowInsertButton="True" />
            </Fields>
        </asp:DetailsView>
        <br />
        <br />
        </div></td></tr></table></div><hr /><div class=footer>
        Skyline Query Website<br />
        Last modified:<script language="JavaScript">
            <!--hide script from old browsers
            document.write(document.lastModified + "")// end hiding -->
</script></div>
    
    </form>
</body>
</html>
