 
package controller;

import model.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.util.*;
import java.io.*;
import controller.Utility;
import com.github.jknack.handlebars.*;

public class AgentSearch{
	private DataSource datasource;
	private model.User user;
    private int searchId;
    private Boolean emailSent;
    private String ajaxToken, serverName;
	
	public AgentSearch(DataSource datasource, model.User user, String searchId, String emailSent, String ajaxToken, String serverName){
		this.user = user;
		this.datasource = datasource;
        this.searchId = (searchId == null) ? 0: new Integer(searchId).intValue();
        this.emailSent = (emailSent == null) ? new Boolean(false) : new Boolean(emailSent);
        this.ajaxToken = ajaxToken;
        this.serverName = serverName;
	}
	
	private synchronized Connection getConnection() throws SQLException {
		return this.datasource.getConnection();
	}
    
    public String getHTML() throws Exception{
        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compile("templates/portal");
        
        Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
        
        HashMap<String, Object> h = new HashMap<String, Object>();
        
		try {
			connection = getConnection();
            
            h.put("count", "0");
            h.put("googleAnalytics", controller.Utility.getGA(serverName));
            ArrayList<HashMap<String, Object>> extraCSS = new ArrayList<HashMap<String, Object>>(1);
            HashMap<String, Object> eC = new HashMap<String, Object>();
            eC.put("name", "agent-search");
            extraCSS.add(eC);
            
            eC = new HashMap<String, Object>();
            eC.put("name", "nouislider.min");
            extraCSS.add(eC);//semantic-ui/semantic.min
            
            eC = new HashMap<String, Object>();
            eC.put("name", "../js/semantic-ui/semantic.min");
            extraCSS.add(eC);
            
            h.put("extraCSS",extraCSS);
            
            extraCSS = new ArrayList<HashMap<String, Object>>(5);
            eC = new HashMap<String, Object>();
            eC.put("name", "handlebars.runtime.min-v4.7.6");
            extraCSS.add(eC);
            eC = new HashMap<String, Object>();
            eC.put("name", "templates/agent-search.tpl");
            extraCSS.add(eC);
            eC = new HashMap<String, Object>();
            eC.put("name", "nouislider.min");
            extraCSS.add(eC);
            eC = new HashMap<String, Object>();
            eC.put("name", "wNumb.min");
            extraCSS.add(eC);
            eC = new HashMap<String, Object>();
            eC.put("name", "semantic-ui/semantic.min");
            extraCSS.add(eC);
            eC = new HashMap<String, Object>();
            eC.put("name", "agent-search");
            extraCSS.add(eC);
            
            h.put("extraJS",extraCSS);
            h.put("dynamicPartial", "templates/agent-search-partial");
            
            if(searchId != 0){
                pstmt = connection.prepareStatement("select `lead`.lead_id, `search`.filters, `search`.`count` from `lead` inner join `search` on `search`.lead_id=`lead`.id where `lead`.agent_id=? and search.id=?");
                pstmt.setInt(1, user.getUserId());
                pstmt.setInt(2, searchId);
                rs=pstmt.executeQuery();
                rs.next();
                
                h.put("leadUserId", new Integer(rs.getInt(1)));
                h.put("criteria",rs.getString(2));
                
                pstmt = connection.prepareStatement("select MemberFirstName, MemberLastName from `user` where id=?");
                pstmt.setInt(1, rs.getInt(1));
                rs=pstmt.executeQuery();
                rs.next();

                h.put("leadFirstName", rs.getString(1));
                h.put("leadLastName", rs.getString(2));
            }
            
            
            h.put("agentFirstName", user.getFirstName());
            h.put("agentLastName", user.getLastName());
            h.put("searchId", searchId+"");
            h.put("ajaxToken", ajaxToken);
            h.put("serverName", serverName);
            h.put("pageTitle", "Search");
            
            return template.apply(h);
		}catch (Exception e) {
            throw new Exception(e.getMessage());
		}finally {
		  try {if (rs != null) rs.close();} catch (SQLException e) {}
		  try {if (pstmt != null) pstmt.close();} catch (SQLException e) {}
		  try {if (connection != null) connection.close();} catch (SQLException e) {}
		}
    }
}